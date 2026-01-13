package com.mbclab.lablink.features.event.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEventRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String picId;  // Person In Charge ID
}
