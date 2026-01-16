package com.mbclab.lablink.features.presence.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreatePresenceRequest {
    private String type;
    private LocalDate date;
    private String title;
    private String notes;
}
