package com.mbclab.lablink.features.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMemberRequest {
    // Semua field optional untuk partial update
    // Service akan cek null untuk menentukan apakah perlu diupdate
    
    @Size(min = 2, max = 100, message = "Nama harus 2-100 karakter")
    private String fullName;

    @Size(max = 50, message = "Divisi maksimal 50 karakter")
    private String expertDivision;

    @Size(max = 50, message = "Departemen maksimal 50 karakter")
    private String department;

    @Email(message = "Format email tidak valid")
    private String email;

    @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
    private String phoneNumber;

    @Size(max = 200, message = "Link media sosial maksimal 200 karakter")
    private String socialMediaLink;
}