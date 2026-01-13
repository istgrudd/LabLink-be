package com.mbclab.lablink.features.project.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProjectRequest {
    private String name;
    private String division;
    private String activityType;
    private String status;        // NOT_STARTED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Integer progressPercent;  // 0-100
}
