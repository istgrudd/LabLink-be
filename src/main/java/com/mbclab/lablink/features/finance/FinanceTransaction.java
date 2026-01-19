package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.project.Project;
import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transaksi keuangan (pemasukan/pengeluaran).
 * Optional: link ke Event atau Project sebagai cost center.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "finance_transactions", indexes = {
    @Index(name = "idx_transaction_type", columnList = "type"),
    @Index(name = "idx_transaction_category_id", columnList = "category_id"),
    @Index(name = "idx_transaction_date", columnList = "transactionDate"),
    @Index(name = "idx_transaction_event_id", columnList = "event_id"),
    @Index(name = "idx_transaction_project_id", columnList = "project_id")
})
public class FinanceTransaction extends BaseEntity {

    /**
     * INCOME = Pemasukan
     * EXPENSE = Pengeluaran
     */
    @Column(nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private FinanceCategory category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String receiptPath;  // Bukti nota/invoice

    // === Cost Center (optional) ===
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // === Future: Budget tracking ===
    
    @Column(precision = 15, scale = 2)
    private BigDecimal budgetAmount;  // Nullable, untuk development nanti

    // Siapa yang input transaksi
    private String createdBy;
}
