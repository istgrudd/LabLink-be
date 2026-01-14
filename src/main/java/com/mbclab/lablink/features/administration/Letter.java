package com.mbclab.lablink.features.administration;

import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Entity untuk Surat Keluar.
 * 
 * Format Nomor: 001/PMJ/EXT/MBC/XII/2025
 * - 001: Nomor urut
 * - PMJ: Jenis surat (PMJ, IZN, STF, SP, UND)
 * - EXT: Kategori (RK, INT, EXT, WSH)
 * - MBC: Organisasi
 * - XII: Bulan (Romawi)
 * - 2025: Tahun
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "letters")
public class Letter extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String letterNumber;  // Auto-generated: 001/PMJ/EXT/MBC/I/2026

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

    @Column(nullable = false)
    private LocalDate issueDate;

    private String attachment;  // Lampiran

    // Status: DRAFT, SENT
    private String status = "DRAFT";

    // Periode kepengurusan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private AcademicPeriod period;

    // Optional: Link ke Event terkait
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // Siapa yang membuat
    private String createdBy;
}
