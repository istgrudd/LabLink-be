package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.event.EventRepository;
import com.mbclab.lablink.features.finance.dto.TransactionRequest;
import com.mbclab.lablink.features.finance.dto.TransactionResponse;
import com.mbclab.lablink.features.finance.dto.TransactionSummaryResponse;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import com.mbclab.lablink.features.project.Project;
import com.mbclab.lablink.features.project.ProjectRepository;
import com.mbclab.lablink.shared.FileStorageService;
import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceTransactionService {

    private final FinanceTransactionRepository transactionRepository;
    private final FinanceCategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;
    private final AcademicPeriodRepository periodRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, MultipartFile receiptFile, String createdBy) {
        FinanceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategori tidak ditemukan"));
        
        AcademicPeriod activePeriod = periodRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Tidak ada periode aktif. Transaksi harus tercatat dalam periode aktif."));

        // Store receipt file in service layer (SoC — not in controller)
        String receiptPath = receiptFile != null ? fileStorageService.storeFile(receiptFile) : null;

        FinanceTransaction tx = new FinanceTransaction();
        tx.setType(request.getType());
        tx.setCategory(category);
        tx.setAmount(request.getAmount());
        tx.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        tx.setDescription(request.getDescription());
        tx.setReceiptPath(receiptPath);
        tx.setCreatedBy(createdBy);
        tx.setPeriod(activePeriod);
        
        // Cost center
        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event tidak ditemukan"));
            tx.setEvent(event);
        }
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project tidak ditemukan"));
            tx.setProject(project);
        }
        
        FinanceTransaction saved = transactionRepository.save(tx);
        
        eventPublisher.publishEvent(AuditEvent.create(
                "FINANCE_TRANSACTION", saved.getId(), category.getName(),
                "Created " + saved.getType() + " transaction: Rp " + saved.getAmount()));
        
        return toTransactionResponse(saved);
    }

    /**
     * Overloaded method for simple transaction creation (no file upload).
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, String createdBy) {
        return createTransaction(request, null, createdBy);
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
                .orElseThrow(() -> new ResourceNotFoundException("Transaksi tidak ditemukan"));
        
        if (request.getType() != null) tx.setType(request.getType());
        if (request.getCategoryId() != null) {
            FinanceCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategori tidak ditemukan"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Transaksi tidak ditemukan"));
        
        transactionRepository.delete(tx);
        
        eventPublisher.publishEvent(AuditEvent.delete(
                "FINANCE_TRANSACTION", id, tx.getCategory().getName(),
                "Deleted transaction: Rp " + tx.getAmount()));
    }

    // ==================== HELPER ====================

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
}
