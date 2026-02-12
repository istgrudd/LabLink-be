package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.finance.dto.DuesPaymentRequest;
import com.mbclab.lablink.features.finance.dto.DuesPaymentResponse;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import com.mbclab.lablink.shared.FileStorageService;
import com.mbclab.lablink.shared.exception.BusinessValidationException;
import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import com.mbclab.lablink.shared.status.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DuesPaymentService {

    private final DuesPaymentRepository duesRepository;
    private final MemberRepository memberRepository;
    private final AcademicPeriodRepository periodRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DuesPaymentResponse submitDuesPayment(String memberId, DuesPaymentRequest request, MultipartFile file) {
        ResearchAssistant member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member tidak ditemukan"));
        
        AcademicPeriod period = periodRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Tidak ada periode aktif"));
        
        // Check if already exists for this month — allow re-upload if REJECTED
        var existing = duesRepository.findByMemberIdAndPaymentMonthAndPaymentYear(
                memberId, request.getPaymentMonth(), request.getPaymentYear());
        
        DuesPayment dues;
        if (existing.isPresent()) {
            dues = existing.get();
            if (!PaymentStatus.REJECTED.equals(dues.getStatus())) {
                throw new BusinessValidationException("Pembayaran untuk bulan ini sudah ada");
            }
            // Re-upload: update existing REJECTED record with new proof
        } else {
            dues = new DuesPayment();
            dues.setMember(member);
            dues.setPeriod(period);
            dues.setPaymentMonth(request.getPaymentMonth());
            dues.setPaymentYear(request.getPaymentYear());
        }
        
        // Store file in service layer (SoC — not in controller)
        String proofPath = fileStorageService.storeFile(file);
        
        dues.setAmount(request.getAmount());
        dues.setPaidAt(LocalDate.now());  // Upload time
        dues.setPaymentProofPath(proofPath);
        dues.setStatus(PaymentStatus.PENDING);
        dues.setVerifiedBy(null);  // Clear previous rejection info
        
        DuesPayment saved = duesRepository.save(dues);
        
        eventPublisher.publishEvent(AuditEvent.create(
                "DUES_PAYMENT", saved.getId(), member.getFullName(),
                "Submitted dues payment for " + request.getPaymentMonth() + "/" + request.getPaymentYear()));
        
        return toDuesResponse(saved);
    }

    public List<DuesPaymentResponse> getMyDuesHistory(String memberId) {
        return duesRepository.findByMemberIdOrderByPaymentYearDescPaymentMonthDesc(memberId).stream()
                .map(this::toDuesResponse)
                .collect(Collectors.toList());
    }

    public Page<DuesPaymentResponse> getAllDues(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return duesRepository.findAll(pageable)
                .map(this::toDuesResponse);
    }

    public List<DuesPaymentResponse> getPendingVerification() {
        return duesRepository.findPendingVerification().stream()
                .map(this::toDuesResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DuesPaymentResponse verifyDuesPayment(String id, String adminUsername) {
        DuesPayment dues = duesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pembayaran tidak ditemukan"));
        
        if (!PaymentStatus.PENDING.equals(dues.getStatus())) {
            throw new BusinessValidationException("Hanya pembayaran dengan status PENDING yang bisa diverifikasi");
        }
        
        dues.setStatus(PaymentStatus.VERIFIED);
        dues.setPaidAt(LocalDate.now());  // Confirm time (overwrite upload time)
        dues.setVerifiedBy(adminUsername);
        
        DuesPayment saved = duesRepository.save(dues);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "DUES_PAYMENT", saved.getId(), saved.getMember().getFullName(),
                "Verified dues payment"));
        
        return toDuesResponse(saved);
    }

    @Transactional
    public DuesPaymentResponse rejectDuesPayment(String id, String adminUsername) {
        DuesPayment dues = duesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pembayaran tidak ditemukan"));
        
        if (!PaymentStatus.PENDING.equals(dues.getStatus())) {
            throw new BusinessValidationException("Hanya pembayaran dengan status PENDING yang bisa ditolak");
        }
        
        dues.setStatus(PaymentStatus.REJECTED);
        dues.setVerifiedBy(adminUsername);
        // Keep paidAt as-is (record of when user uploaded)
        
        DuesPayment saved = duesRepository.save(dues);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "DUES_PAYMENT", saved.getId(), saved.getMember().getFullName(),
                "Rejected dues payment by " + adminUsername));
        
        return toDuesResponse(saved);
    }

    // ==================== HELPER ====================

    private DuesPaymentResponse toDuesResponse(DuesPayment d) {
        String proofUrl = d.getPaymentProofPath() != null ?
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(d.getPaymentProofPath())
                        .toUriString() : null;
        
        return DuesPaymentResponse.builder()
                .id(d.getId())
                .memberId(d.getMember().getId())
                .memberName(d.getMember().getFullName())
                .memberNim(d.getMember().getUsername())
                .periodId(d.getPeriod().getId())
                .periodName(d.getPeriod().getName())
                .paymentMonth(d.getPaymentMonth())
                .paymentYear(d.getPaymentYear())
                .amount(d.getAmount())
                .paidAt(d.getPaidAt())
                .paymentProofUrl(proofUrl)
                .status(d.getStatus().name())
                .verifiedBy(d.getVerifiedBy())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
