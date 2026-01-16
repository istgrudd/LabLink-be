package com.mbclab.lablink.features.administration;

import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Entity untuk Surat Keluar dengan Workflow Approval.
 * 
 * Format Nomor: 001/PMJ/EXT/MBC/XII/2025
 * Status Flow: PENDING -> APPROVED/REJECTED -> DOWNLOADED
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "letters")
public class Letter extends BaseEntity {

    // Generated on approval (nullable until approved)
    @Column(unique = true)
    private String letterNumber;

    // Jenis Surat: PMJ, IZN, STF, SP, UND
    @Column(nullable = false)
    private String letterType;

    // Kategori: RK, INT, EXT, WSH
    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String subject;  // Perihal

    @Column(nullable = false)
    private String recipient;  // Tujuan

    @Column(columnDefinition = "TEXT")
    private String content;  // Isi surat (optional)

    private String attachment;  // Lampiran

    // ==== REQUESTER INFO (auto-filled from user) ====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private ResearchAssistant requester;

    private String requesterName;  // From user.fullName
    private String requesterNim;   // From user.username

    // ==== BORROW DATE/TIME (for PMJ letters) ====
    private LocalDate borrowDate;
    private LocalDate borrowReturnDate;

    // ==== DATES ====
    // issueDate set on APPROVAL (tanggal surat = tanggal disetujui)
    private LocalDate issueDate;
    
    // ==== STATUS: PENDING, APPROVED, REJECTED, DOWNLOADED ====
    private String status = "PENDING";

    // Periode kepengurusan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private AcademicPeriod period;

    // Link ke Event terkait (required for context)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // Who approved/rejected
    private String approvedBy;
    private String rejectionReason;
}
