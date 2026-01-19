package com.mbclab.lablink.features.finance.dto;

import com.mbclab.lablink.shared.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProcurementResponse extends BaseResponse {
    private String requesterId;
    private String requesterName;
    private String requesterNim;
    
    private String itemName;
    private String description;
    private String reason;
    private BigDecimal estimatedPrice;
    private String priority;
    private String purchaseLink;
    
    private String status;
    private String processedBy;
    private String rejectionReason;
    private LocalDate processedAt;
    
    private String transactionId;  // Link ke transaksi jika sudah dibeli
}
