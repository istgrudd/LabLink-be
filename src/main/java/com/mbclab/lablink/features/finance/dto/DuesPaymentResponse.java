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
public class DuesPaymentResponse extends BaseResponse {
    private String memberId;
    private String memberName;
    private String memberNim;
    
    private String periodId;
    private String periodName;
    
    private Integer paymentMonth;
    private Integer paymentYear;
    private BigDecimal amount;
    
    private LocalDate paidAt;
    private String paymentProofUrl;
    private String status;
    private String verifiedBy;
}
