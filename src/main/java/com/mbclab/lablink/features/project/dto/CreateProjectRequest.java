package com.mbclab.lablink.features.project.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateProjectRequest {
    private String name;
    private String description;
    private String division;      // CYBER_SECURITY, BIG_DATA, GIS, GAME_TECH, CROSS_DIVISION
    private String activityType;  // RISET, HKI, PENGABDIAN
    private String leaderId;      // ID Ketua Proyek
    private Set<String> teamMemberIds;  // ID Anggota Tim
    private LocalDate startDate;
    private LocalDate endDate;
}
