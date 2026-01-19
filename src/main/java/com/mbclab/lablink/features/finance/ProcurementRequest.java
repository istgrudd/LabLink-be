package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pengajuan barang oleh member.
 * Admin melakukan approval/reject.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "procurement_requests", indexes = {
    @Index(name = "idx_procurement_status", columnList = "status"),
    @Index(name = "idx_procurement_requester_id", columnList = "requester_id"),
    @Index(name = "idx_procurement_priority", columnList = "priority")
})
public class ProcurementRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private ResearchAssistant requester;

    @Column(nullable = false)
    private String itemName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String reason;  // Alasan perlu barang ini

    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedPrice;

    /**
     * LOW, MEDIUM, HIGH
     */
    @Column(nullable = false)
    private String priority = "MEDIUM";

    private String purchaseLink;  // Link marketplace (optional)

    /**
     * PENDING = Menunggu approval
     * APPROVED = Disetujui, menunggu pembelian
     * REJECTED = Ditolak
     * PURCHASED = Sudah dibeli
     */
    @Column(nullable = false)
    private String status = "PENDING";

    private String processedBy;     // Admin username

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private LocalDate processedAt;

    // Link ke transaksi pengeluaran ketika sudah dibeli
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private FinanceTransaction transaction;
}
