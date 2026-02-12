package com.mbclab.lablink.features.finance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Nama kategori wajib diisi")
    @Size(max = 100, message = "Nama kategori maksimal 100 karakter")
    private String name;

    @NotBlank(message = "Tipe kategori wajib diisi (INCOME/EXPENSE/BOTH)")
    private String type;

    @Size(max = 255, message = "Deskripsi maksimal 255 karakter")
    private String description;
}
