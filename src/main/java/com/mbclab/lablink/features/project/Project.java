package com.mbclab.lablink.features.project;

import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @Column(nullable = false)
    private String name;

    // Divisi keahlian: CYBER_SECURITY, BIG_DATA, GIS, GAME_TECH, CROSS_DIVISION
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

    // Relasi ke ProjectMember
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ProjectMember> members = new ArrayList<>();
}
