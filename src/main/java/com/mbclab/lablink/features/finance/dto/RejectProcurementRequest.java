package com.mbclab.lablink.features.finance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectProcurementRequest {
    @NotBlank(message = "Alasan penolakan wajib diisi")
    private String rejectionReason;
}
