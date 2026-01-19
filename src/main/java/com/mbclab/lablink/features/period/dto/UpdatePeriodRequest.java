package com.mbclab.lablink.features.period.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePeriodRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
