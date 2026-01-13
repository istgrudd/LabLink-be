package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.shared.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    // Kode proyek untuk display (RST-0001, PKM-0001, dll)
    @Column(nullable = false, unique = true)
    private String projectCode;

    @Column(nullable = false)
    private String name;

    // Divisi: CYBER_SECURITY, BIG_DATA, GIS, GAME_TECH, CROSS_DIVISION
    @Column(nullable = false)
    private String division;

    // Tipe aktivitas: RISET, HKI, PENGABDIAN
    @Column(nullable = false)
    private String activityType;

    // Status: NOT_STARTED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    @Column(nullable = false)
    private String status = "NOT_STARTED";

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(length = 1000)
    private String description;

    // Progress tracker (0-100%)
    @Column(nullable = false)
    private Integer progressPercent = 0;

    // ========== RELASI ==========

    // Ketua Proyek (1 orang)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private ResearchAssistant leader;

    // Anggota Tim (Many-to-Many)
    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    @JsonIgnore
    private Set<ResearchAssistant> teamMembers = new HashSet<>();
}
