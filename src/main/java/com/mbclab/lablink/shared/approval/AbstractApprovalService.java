package com.mbclab.lablink.shared.approval;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.shared.exception.BusinessValidationException;
import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base service untuk approval workflow.
 * 
 * Menyediakan implementasi standar untuk:
 * - getPending() - list entity dengan status PENDING
 * - approve(id, approver) - set status APPROVED  
 * - reject(id, reason, rejector) - set status REJECTED
 * 
 * Subclass harus implement:
 * - getRepository() - return domain-specific repository
 * - toResponse(entity) - convert entity ke response DTO
 * - getEntityType() - return nama entity untuk audit log (e.g., "PROJECT")
 * - getNotFoundMessage() - custom error message
 * 
 * @param <E> Entity type yang implements Approvable
 * @param <R> Response DTO type
 */
@RequiredArgsConstructor
public abstract class AbstractApprovalService<E extends Approvable, R> {
    
    protected final ApplicationEventPublisher eventPublisher;
    
    // ========== ABSTRACT METHODS ==========
    
    /**
     * Get the domain-specific repository.
     */
    protected abstract ApprovalRepository<E, String> getRepository();
    
    /**
     * Convert entity to response DTO.
     */
    protected abstract R toResponse(E entity);
    
    /**
     * Get entity type name for audit logging (e.g., "PROJECT", "EVENT").
     */
    protected abstract String getEntityType();
    
    /**
     * Get not found error message.
     */
    protected abstract String getNotFoundMessage();
    
    /**
     * Hook for custom permission verification.
     * Override this in subclass for RBAC checks.
     * 
     * @param entity The entity being approved/rejected
     * @param username The user performing the action
     * @throws org.springframework.security.access.AccessDeniedException if not authorized
     */
    protected void verifyApprovalPermission(E entity, String username) {
        // Default: no additional checks (controller-level @PreAuthorize is sufficient)
    }
    
    // ========== PUBLIC METHODS ==========
    
    /**
     * Get all entities with PENDING approval status.
     */
    public List<R> getPending() {
        return getRepository().findByApprovalStatus(ApprovalStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Approve an entity.
     * 
     * @param id Entity ID
     * @param approverUsername Username of the approver
     * @return Response DTO
     * @throws ResourceNotFoundException if entity not found
     * @throws BusinessValidationException if not in PENDING status
     */
    public R approve(String id, String approverUsername) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getNotFoundMessage()));
        
        validatePendingStatus(entity);
        verifyApprovalPermission(entity, approverUsername);
        
        entity.setApprovalStatus(ApprovalStatus.APPROVED);
        entity.setApprovedAt(LocalDate.now());
        entity.setApprovedBy(approverUsername);
        
        E saved = getRepository().save(entity);
        
        publishAuditEvent(saved, "Approved " + getEntityType().toLowerCase() + ": " + saved.getDisplayName());
        
        return toResponse(saved);
    }
    
    /**
     * Reject an entity.
     * 
     * @param id Entity ID
     * @param reason Rejection reason
     * @param rejectorUsername Username of the rejector
     * @return Response DTO
     * @throws ResourceNotFoundException if entity not found
     * @throws BusinessValidationException if not in PENDING status
     */
    public R reject(String id, String reason, String rejectorUsername) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getNotFoundMessage()));
        
        validatePendingStatus(entity);
        verifyApprovalPermission(entity, rejectorUsername);
        
        entity.setApprovalStatus(ApprovalStatus.REJECTED);
        entity.setRejectionReason(reason);
        entity.setApprovedBy(rejectorUsername);
        
        E saved = getRepository().save(entity);
        
        publishAuditEvent(saved, "Rejected " + getEntityType().toLowerCase() + ": " + saved.getDisplayName() + " - Reason: " + reason);
        
        return toResponse(saved);
    }
    
    // ========== PROTECTED HELPERS ==========
    
    protected void validatePendingStatus(E entity) {
        if (!ApprovalStatus.isPending(entity.getApprovalStatus())) {
            throw new BusinessValidationException(
                    getEntityType() + " sudah diproses sebelumnya (Status: " + entity.getApprovalStatus() + ")");
        }
    }
    
    protected void publishAuditEvent(E entity, String description) {
        eventPublisher.publishEvent(AuditEvent.update(
                getEntityType(),
                entity.getId(),
                entity.getDisplayName(),
                description));
    }
}
