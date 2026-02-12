package com.mbclab.lablink.features.finance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    @NotBlank(message = "Tipe transaksi wajib diisi (INCOME/EXPENSE)")
    private String type;

    @NotBlank(message = "Kategori wajib dipilih")
    private String categoryId;

    @NotNull(message = "Jumlah transaksi wajib diisi")
    @Positive(message = "Jumlah transaksi harus positif")
    private BigDecimal amount;

    private LocalDate transactionDate;

    @Size(max = 500, message = "Deskripsi maksimal 500 karakter")
    private String description;

    // Cost center (optional)
    private String eventId;
    private String projectId;
}
