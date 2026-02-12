package com.mbclab.lablink.features.project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateProjectRequest {

    @Size(min = 3, max = 200, message = "Nama proyek harus 3-200 karakter")
    private String name;

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    private String description;

    private String division;
    private String activityType;
    private String status;        // NOT_STARTED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    private String leaderId;
    private Set<String> teamMemberIds;
    private LocalDate startDate;
    private LocalDate endDate;

    @Min(value = 0, message = "Progress minimal 0%")
    @Max(value = 100, message = "Progress maksimal 100%")
    private Integer progressPercent;  // 0-100
}
