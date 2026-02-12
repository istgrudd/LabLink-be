package com.mbclab.lablink.features.event.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectEventRequest {
    
    @Size(max = 500, message = "Alasan penolakan maksimal 500 karakter")
    private String rejectionReason;
}
