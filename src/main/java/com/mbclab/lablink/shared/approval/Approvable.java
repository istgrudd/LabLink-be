package com.mbclab.lablink.shared.approval;

import java.time.LocalDate;

/**
 * Interface marker untuk entity yang memiliki approval workflow.
 * 
 * Implementasi: Project, Event
 * 
 * Flow: PENDING → APPROVED / REJECTED
 * 
 * @param <ID> Tipe ID entity (biasanya String untuk UUID)
 */
public interface Approvable {
    
    // ========== GETTERS ==========
    
    String getId();
    
    String getApprovalStatus();
    
    String getRejectionReason();
    
    String getApprovedBy();
    
    LocalDate getApprovedAt();
    
    /**
     * Display name untuk audit logging.
     * Contoh: Project.getName(), Event.getName()
     */
    String getDisplayName();
    
    // ========== SETTERS ==========
    
    void setApprovalStatus(String status);
    
    void setRejectionReason(String reason);
    
    void setApprovedBy(String username);
    
    void setApprovedAt(LocalDate date);
}
