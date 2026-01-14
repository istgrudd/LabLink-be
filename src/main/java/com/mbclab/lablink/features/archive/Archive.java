package com.mbclab.lablink.features.archive;

import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.project.Project;
import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Entity untuk menyimpan arsip/output dari Project atau Event.
 * 
 * Archive Types by Source:
 * - Project (INTERNAL): PUBLIKASI, HKI, PKM
 * - Event (EKSTERNAL): LAPORAN, SERTIFIKAT
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "archives")
public class Archive extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String archiveCode;  // PUB-0001, HKI-0001, LPR-0001, dll

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Archive type based on source:
    // PROJECT: PUBLIKASI, HKI, PKM
    // EVENT: LAPORAN, SERTIFIKAT
    @Column(nullable = false)
    private String archiveType;

    // Department: INTERNAL (Project), EKSTERNAL (Event)
    @Column(nullable = false)
    private String department;

    // Source type: PROJECT atau EVENT
    @Column(nullable = false)
    private String sourceType;

    // Polymorphic association - one of these will be set
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // Publication details
    private String publishLocation;  // Nama jurnal, konferensi, lembaga
    private String referenceNumber;  // DOI, No. Registrasi HKI, No. Sertifikat
    private LocalDate publishDate;
}
