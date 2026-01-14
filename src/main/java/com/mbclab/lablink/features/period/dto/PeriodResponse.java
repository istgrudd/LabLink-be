package com.mbclab.lablink.features.period.dto;

import com.mbclab.lablink.shared.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PeriodResponse extends BaseResponse {
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private boolean isArchived;
    private int totalMembers;
    private int totalProjects;
    private int totalEvents;
}
