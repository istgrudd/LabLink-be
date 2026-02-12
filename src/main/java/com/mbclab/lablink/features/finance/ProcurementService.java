package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.finance.dto.ProcurementRequestDto;
import com.mbclab.lablink.features.finance.dto.ProcurementResponse;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.shared.exception.BusinessValidationException;
import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcurementService {

    private final ProcurementRequestRepository procurementRepository;
    private final FinanceTransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProcurementResponse createProcurementRequest(String requesterId, ProcurementRequestDto request) {
        ResearchAssistant requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Member tidak ditemukan"));
        
        ProcurementRequest pr = new ProcurementRequest();
        pr.setRequester(requester);
        pr.setItemName(request.getItemName());
        pr.setDescription(request.getDescription());
        pr.setReason(request.getReason());
        pr.setEstimatedPrice(request.getEstimatedPrice());
        pr.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        pr.setPurchaseLink(request.getPurchaseLink());
        
        ProcurementRequest saved = procurementRepository.save(pr);
        
        eventPublisher.publishEvent(AuditEvent.create(
                "PROCUREMENT", saved.getId(), saved.getItemName(),
                "Created procurement request by " + requester.getFullName()));
        
        return toProcurementResponse(saved);
    }

    public List<ProcurementResponse> getMyProcurementRequests(String requesterId) {
        return procurementRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId).stream()
                .map(this::toProcurementResponse)
                .collect(Collectors.toList());
    }

    public Page<ProcurementResponse> getAllProcurementRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return procurementRepository.findAll(pageable).map(this::toProcurementResponse);
    }

    public List<ProcurementResponse> getPendingProcurements() {
        return procurementRepository.findByStatusOrderByPriorityDescCreatedAtAsc("PENDING").stream()
                .map(this::toProcurementResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProcurementResponse approveProcurement(String id, String adminUsername) {
        ProcurementRequest pr = procurementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan tidak ditemukan"));
        
        if (!"PENDING".equals(pr.getStatus())) {
            throw new BusinessValidationException("Pengajuan sudah diproses sebelumnya");
        }
        
        pr.setStatus("APPROVED");
        pr.setProcessedBy(adminUsername);
        pr.setProcessedAt(LocalDate.now());
        
        ProcurementRequest saved = procurementRepository.save(pr);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PROCUREMENT", saved.getId(), saved.getItemName(),
                "Approved procurement request"));
        
        return toProcurementResponse(saved);
    }

    @Transactional
    public ProcurementResponse rejectProcurement(String id, String adminUsername, String reason) {
        ProcurementRequest pr = procurementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan tidak ditemukan"));
        
        if (!"PENDING".equals(pr.getStatus())) {
            throw new BusinessValidationException("Pengajuan sudah diproses sebelumnya");
        }
        
        pr.setStatus("REJECTED");
        pr.setProcessedBy(adminUsername);
        pr.setProcessedAt(LocalDate.now());
        pr.setRejectionReason(reason);
        
        ProcurementRequest saved = procurementRepository.save(pr);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PROCUREMENT", saved.getId(), saved.getItemName(),
                "Rejected procurement request: " + reason));
        
        return toProcurementResponse(saved);
    }

    @Transactional
    public ProcurementResponse markPurchased(String id, String transactionId) {
        ProcurementRequest pr = procurementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan tidak ditemukan"));
        
        if (!"APPROVED".equals(pr.getStatus())) {
            throw new BusinessValidationException("Pengajuan harus disetujui terlebih dahulu");
        }
        
        if (transactionId != null) {
            FinanceTransaction tx = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaksi tidak ditemukan"));
            pr.setTransaction(tx);
        }
        
        pr.setStatus("PURCHASED");
        
        ProcurementRequest saved = procurementRepository.save(pr);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PROCUREMENT", saved.getId(), saved.getItemName(),
                "Marked procurement as purchased"));
        
        return toProcurementResponse(saved);
    }

    // ==================== HELPER ====================

    private ProcurementResponse toProcurementResponse(ProcurementRequest p) {
        return ProcurementResponse.builder()
                .id(p.getId())
                .requesterId(p.getRequester().getId())
                .requesterName(p.getRequester().getFullName())
                .requesterNim(p.getRequester().getUsername())
                .itemName(p.getItemName())
                .description(p.getDescription())
                .reason(p.getReason())
                .estimatedPrice(p.getEstimatedPrice())
                .priority(p.getPriority())
                .purchaseLink(p.getPurchaseLink())
                .status(p.getStatus())
                .processedBy(p.getProcessedBy())
                .rejectionReason(p.getRejectionReason())
                .processedAt(p.getProcessedAt())
                .transactionId(p.getTransaction() != null ? p.getTransaction().getId() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
