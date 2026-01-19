package com.mbclab.lablink.features.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcurementRequestDto {
    private String itemName;
    private String description;
    private String reason;
    private BigDecimal estimatedPrice;
    private String priority;      // LOW, MEDIUM, HIGH
    private String purchaseLink;  // Optional
}
