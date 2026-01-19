package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pembayaran kas bulanan member.
 * Fixed Rp 20.000/bulan.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dues_payments", indexes = {
    @Index(name = "idx_dues_member_id", columnList = "member_id"),
    @Index(name = "idx_dues_period_id", columnList = "period_id"),
    @Index(name = "idx_dues_payment_month", columnList = "paymentMonth, paymentYear"),
    @Index(name = "idx_dues_status", columnList = "status")
})
public class DuesPayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private ResearchAssistant member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private AcademicPeriod period;

    @Column(nullable = false)
    private Integer paymentMonth;  // 1-12

    @Column(nullable = false)
    private Integer paymentYear;   // 2025

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = new BigDecimal("20000");

    private LocalDate paidAt;

    private String paymentProofPath;  // Bukti transfer

    /**
     * UNPAID = Belum bayar
     * PENDING = Sudah submit bukti, menunggu verifikasi
     * VERIFIED = Sudah diverifikasi admin
     */
    @Column(nullable = false)
    private String status = "UNPAID";

    private String verifiedBy;  // Admin username yang verifikasi
}
