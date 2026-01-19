package com.mbclab.lablink.features.finance.dto;

import lombok.Data;

@Data
public class DuesPaymentRequest {
    private Integer paymentMonth;  // 1-12
    private Integer paymentYear;   // 2025
    // paymentProofPath akan diisi dari file upload
}
