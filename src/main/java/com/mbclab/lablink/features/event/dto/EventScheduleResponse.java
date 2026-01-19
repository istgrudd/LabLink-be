package com.mbclab.lablink.features.event.dto;

import com.mbclab.lablink.shared.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EventScheduleResponse extends BaseResponse {
    
    private String eventId;
    private String eventCode;
    private String eventName;
    
    private LocalDate activityDate;
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
}
