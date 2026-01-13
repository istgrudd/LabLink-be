package com.mbclab.lablink.features.project.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateProjectRequest {
    private String name;
    private String description;
    private String division;
    private String activityType;
    private String status;        // NOT_STARTED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    private String leaderId;
    private Set<String> teamMemberIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progressPercent;  // 0-100
}
