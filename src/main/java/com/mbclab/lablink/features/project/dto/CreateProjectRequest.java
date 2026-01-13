package com.mbclab.lablink.features.project.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProjectRequest {
    private String name;
    private String division;      // CYBER_SECURITY, BIG_DATA, GIS, GAME_TECH, CROSS_DIVISION
    private String activityType;  // RISET, HKI, PENGABDIAN
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
