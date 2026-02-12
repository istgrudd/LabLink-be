package com.mbclab.lablink.features.finance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DuesPaymentRequest {
    @NotNull(message = "Bulan pembayaran wajib diisi")
    @Min(value = 1, message = "Bulan harus antara 1-12")
    @Max(value = 12, message = "Bulan harus antara 1-12")
    private Integer paymentMonth;

    @NotNull(message = "Tahun pembayaran wajib diisi")
    @Min(value = 2020, message = "Tahun minimal 2020")
    private Integer paymentYear;

    @NotNull(message = "Jumlah pembayaran wajib diisi")
    @Positive(message = "Jumlah pembayaran harus positif")
    private BigDecimal amount;
}
