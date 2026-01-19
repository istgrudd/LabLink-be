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
public class TransactionResponse extends BaseResponse {
    private String type;
    private String categoryId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    private String receiptUrl;
    
    // Cost center
    private String eventId;
    private String eventName;
    private String projectId;
    private String projectName;
    
    private String createdBy;
}
