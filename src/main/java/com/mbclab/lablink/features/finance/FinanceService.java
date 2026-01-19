package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.event.EventRepository;
import com.mbclab.lablink.features.finance.dto.*;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import com.mbclab.lablink.features.project.Project;
import com.mbclab.lablink.features.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceService {

    private final FinanceCategoryRepository categoryRepository;
    private final DuesPaymentRepository duesRepository;
    private final FinanceTransactionRepository transactionRepository;
    private final ProcurementRequestRepository procurementRepository;
    private final MemberRepository memberRepository;
    private final AcademicPeriodRepository periodRepository;
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== CATEGORY ====================

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Kategori dengan nama '" + request.getName() + "' sudah ada");
        }
        
        FinanceCategory category = new FinanceCategory();
        category.setName(request.getName());
        category.setType(request.getType() != null ? request.getType() : "BOTH");
        category.setDescription(request.getDescription());
        
        FinanceCategory saved = categoryRepository.save(category);
        
        eventPublisher.publishEvent(AuditEvent.create(
                "FINANCE_CATEGORY", saved.getId(), saved.getName(),
                "Created finance category: " + saved.getName()));
        
        return toCategoryResponse(saved);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getCategoriesByType(String type) {
        return categoryRepository.findByTypeAndIsActiveTrue(type).stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        FinanceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
        
        if (request.getName() != null) category.setName(request.getName());
        if (request.getType() != null) category.setType(request.getType());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        
        FinanceCategory saved = categoryRepository.save(category);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "FINANCE_CATEGORY", saved.getId(), saved.getName(),
                "Updated finance category"));
        
        return toCategoryResponse(saved);
    }

    @Transactional
    public void deleteCategory(String id) {
        FinanceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
        
        category.setActive(false);
        categoryRepository.save(category);
        
        eventPublisher.publishEvent(AuditEvent.delete(
                "FINANCE_CATEGORY", id, category.getName(),
                "Deactivated finance category"));
    }

    // ==================== DUES PAYMENT ====================

    @Transactional
    public DuesPaymentResponse submitDuesPayment(String memberId, DuesPaymentRequest request, String proofPath) {
        ResearchAssistant member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        
        AcademicPeriod period = periodRepository.findByIsActiveTrue()
                .orElseThrow(() -> new RuntimeException("Tidak ada periode aktif"));
        
        // Check if already paid for this month
        if (duesRepository.findByMemberIdAndPaymentMonthAndPaymentYear(
                memberId, request.getPaymentMonth(), request.getPaymentYear()).isPresent()) {
            throw new RuntimeException("Pembayaran untuk bulan ini sudah ada");
        }
        
        DuesPayment dues = new DuesPayment();
        dues.setMember(member);
        dues.setPeriod(period);
        dues.setPaymentMonth(request.getPaymentMonth());
        dues.setPaymentYear(request.getPaymentYear());
        dues.setAmount(request.getAmount());
        dues.setPaidAt(LocalDate.now());
        dues.setPaymentProofPath(proofPath);
        dues.setStatus("PENDING");
        
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

    public List<DuesPaymentResponse> getAllDues() {
        return duesRepository.findAll().stream()
                .map(this::toDuesResponse)
                .collect(Collectors.toList());
    }

    public List<DuesPaymentResponse> getPendingVerification() {
        return duesRepository.findPendingVerification().stream()
                .map(this::toDuesResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DuesPaymentResponse verifyDuesPayment(String id, String adminUsername) {
        DuesPayment dues = duesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pembayaran tidak ditemukan"));
        
        dues.setStatus("VERIFIED");
        dues.setVerifiedBy(adminUsername);
        
        DuesPayment saved = duesRepository.save(dues);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "DUES_PAYMENT", saved.getId(), saved.getMember().getFullName(),
                "Verified dues payment"));
        
        return toDuesResponse(saved);
    }

    // ==================== TRANSACTIONS ====================

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, String receiptPath, String createdBy) {
        FinanceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
        
        AcademicPeriod activePeriod = periodRepository.findByIsActiveTrue()
                .orElseThrow(() -> new RuntimeException("Tidak ada periode aktif. Transaksi harus tercatat dalam periode aktif."));

        FinanceTransaction tx = new FinanceTransaction();
        tx.setType(request.getType());
        tx.setCategory(category);
        tx.setAmount(request.getAmount());
        tx.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        tx.setDescription(request.getDescription());
        tx.setReceiptPath(receiptPath);
        tx.setCreatedBy(createdBy);
        tx.setPeriod(activePeriod); // Enforce Period-Centric logic
        
        // Cost center
        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
            tx.setEvent(event);
        }
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
            tx.setProject(project);
        }
        
        FinanceTransaction saved = transactionRepository.save(tx);
        
        eventPublisher.publishEvent(AuditEvent.create(
                "FINANCE_TRANSACTION", saved.getId(), category.getName(),
                "Created " + saved.getType() + " transaction: Rp " + saved.getAmount()));
        
        return toTransactionResponse(saved);
    }

    public Page<TransactionResponse> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return transactionRepository.findAll(pageable).map(this::toTransactionResponse);
    }

    public TransactionSummaryResponse getTransactionSummary() {
        BigDecimal totalIncome = transactionRepository.getTotalIncome();
        BigDecimal totalExpense = transactionRepository.getTotalExpense();
        BigDecimal balance = totalIncome.subtract(totalExpense);
        
        List<Object[]> incomeByCategory = transactionRepository.getSummaryByCategory("INCOME");
        List<Object[]> expenseByCategory = transactionRepository.getSummaryByCategory("EXPENSE");
        
        return TransactionSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .incomeByCategory(incomeByCategory.stream()
                        .map(arr -> TransactionSummaryResponse.CategorySummary.builder()
                                .categoryName((String) arr[0])
                                .total((BigDecimal) arr[1])
                                .build())
                        .collect(Collectors.toList()))
                .expenseByCategory(expenseByCategory.stream()
                        .map(arr -> TransactionSummaryResponse.CategorySummary.builder()
                                .categoryName((String) arr[0])
                                .total((BigDecimal) arr[1])
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public TransactionResponse updateTransaction(String id, TransactionRequest request) {
        FinanceTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));
        
        if (request.getType() != null) tx.setType(request.getType());
        if (request.getCategoryId() != null) {
            FinanceCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
            tx.setCategory(category);
        }
        if (request.getAmount() != null) tx.setAmount(request.getAmount());
        if (request.getTransactionDate() != null) tx.setTransactionDate(request.getTransactionDate());
        if (request.getDescription() != null) tx.setDescription(request.getDescription());
        
        FinanceTransaction saved = transactionRepository.save(tx);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "FINANCE_TRANSACTION", saved.getId(), saved.getCategory().getName(),
                "Updated transaction"));
        
        return toTransactionResponse(saved);
    }

    @Transactional
    public void deleteTransaction(String id) {
        FinanceTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));
        
        transactionRepository.delete(tx);
        
        eventPublisher.publishEvent(AuditEvent.delete(
                "FINANCE_TRANSACTION", id, tx.getCategory().getName(),
                "Deleted transaction: Rp " + tx.getAmount()));
    }

    // ==================== PROCUREMENT ====================

    @Transactional
    public ProcurementResponse createProcurementRequest(String requesterId, ProcurementRequestDto request) {
        ResearchAssistant requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        
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
                .orElseThrow(() -> new RuntimeException("Pengajuan tidak ditemukan"));
        
        if (!"PENDING".equals(pr.getStatus())) {
            throw new RuntimeException("Pengajuan sudah diproses sebelumnya");
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
                .orElseThrow(() -> new RuntimeException("Pengajuan tidak ditemukan"));
        
        if (!"PENDING".equals(pr.getStatus())) {
            throw new RuntimeException("Pengajuan sudah diproses sebelumnya");
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
                .orElseThrow(() -> new RuntimeException("Pengajuan tidak ditemukan"));
        
        if (!"APPROVED".equals(pr.getStatus())) {
            throw new RuntimeException("Pengajuan harus disetujui terlebih dahulu");
        }
        
        if (transactionId != null) {
            FinanceTransaction tx = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));
            pr.setTransaction(tx);
        }
        
        pr.setStatus("PURCHASED");
        
        ProcurementRequest saved = procurementRepository.save(pr);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PROCUREMENT", saved.getId(), saved.getItemName(),
                "Marked procurement as purchased"));
        
        return toProcurementResponse(saved);
    }

    // ==================== HELPERS ====================

    private CategoryResponse toCategoryResponse(FinanceCategory c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType())
                .description(c.getDescription())
                .isActive(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

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
                .status(d.getStatus())
                .verifiedBy(d.getVerifiedBy())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private TransactionResponse toTransactionResponse(FinanceTransaction t) {
        String receiptUrl = t.getReceiptPath() != null ?
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(t.getReceiptPath())
                        .toUriString() : null;
        
        return TransactionResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .categoryId(t.getCategory().getId())
                .categoryName(t.getCategory().getName())
                .amount(t.getAmount())
                .transactionDate(t.getTransactionDate())
                .description(t.getDescription())
                .receiptUrl(receiptUrl)
                .eventId(t.getEvent() != null ? t.getEvent().getId() : null)
                .eventName(t.getEvent() != null ? t.getEvent().getName() : null)
                .projectId(t.getProject() != null ? t.getProject().getId() : null)
                .projectName(t.getProject() != null ? t.getProject().getName() : null)
                .createdBy(t.getCreatedBy())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

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
