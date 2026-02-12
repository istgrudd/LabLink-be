package com.mbclab.lablink.features.finance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcurementRequestDto {
    @NotBlank(message = "Nama item wajib diisi")
    @Size(max = 200, message = "Nama item maksimal 200 karakter")
    private String itemName;

    @Size(max = 500, message = "Deskripsi maksimal 500 karakter")
    private String description;

    @NotBlank(message = "Alasan pengadaan wajib diisi")
    private String reason;

    @NotNull(message = "Estimasi harga wajib diisi")
    @Positive(message = "Estimasi harga harus positif")
    private BigDecimal estimatedPrice;

    private String priority;      // LOW, MEDIUM, HIGH
    private String purchaseLink;  // Optional
}
