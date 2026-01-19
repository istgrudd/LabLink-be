package com.mbclab.lablink.features.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    private String type;          // INCOME, EXPENSE
    private String categoryId;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    
    // Cost center (optional)
    private String eventId;
    private String projectId;
    
    // receiptPath akan diisi dari file upload
}
