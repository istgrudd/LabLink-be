package com.mbclab.lablink.features.period.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreatePeriodRequest {
    private String code;  // "2025-2026"
    private String name;  // "Periode 2025/2026"
    private LocalDate startDate;
    private LocalDate endDate;
}
