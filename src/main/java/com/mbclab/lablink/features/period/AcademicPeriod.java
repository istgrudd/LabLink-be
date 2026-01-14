package com.mbclab.lablink.features.period;

import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Entity untuk periode kepengurusan.
 * Saat isActive=false dan isArchived=true, semua data dalam periode ini read-only.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "academic_periods")
public class AcademicPeriod extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;  // "2025-2026"

    @Column(nullable = false)
    private String name;  // "Periode 2025/2026"

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // Hanya 1 periode yang boleh active
    private boolean isActive = false;

    // True = data sudah di-freeze, read-only
    private boolean isArchived = false;
}
