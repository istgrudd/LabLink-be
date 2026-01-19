package com.mbclab.lablink.features.period;

import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.mbclab.lablink.features.administration.Letter;
import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.finance.FinanceTransaction;
import com.mbclab.lablink.features.project.Project;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    // === CASCADE DELETE CONFIGURATION ===
    // deleting period -> deletes all children

    @OneToMany(mappedBy = "period", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MemberPeriod> memberPeriods = new HashSet<>();

    @OneToMany(mappedBy = "period", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Project> projects = new HashSet<>();

    @OneToMany(mappedBy = "period", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "period", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Letter> letters = new HashSet<>();

    @OneToMany(mappedBy = "period", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FinanceTransaction> financeTransactions = new HashSet<>();
}
