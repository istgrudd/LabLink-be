package com.mbclab.lablink.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Password lama wajib diisi")
    private String currentPassword;

    @NotBlank(message = "Password baru wajib diisi")
    @Size(min = 8, message = "Password baru minimal 8 karakter")
    private String newPassword;
}
