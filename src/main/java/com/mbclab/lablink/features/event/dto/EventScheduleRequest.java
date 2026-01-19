package com.mbclab.lablink.features.event.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EventScheduleRequest {
    private LocalDate activityDate;
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
}
