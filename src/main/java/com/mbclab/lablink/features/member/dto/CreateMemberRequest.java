package com.mbclab.lablink.features.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMemberRequest {

    @NotBlank(message = "NIM wajib diisi")
    @Size(min = 8, max = 15, message = "NIM harus 8-15 karakter")
    private String nim;           // Username / NIM Mahasiswa

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(min = 2, max = 100, message = "Nama harus 2-100 karakter")
    private String fullName;      // Nama Lengkap

    private String expertDivision; // Misal: Big Data, Cyber Security
    private String department;     // Misal: Informatika
    private String role;           // Optional: ADMIN, ASSISTANT (default: ASSISTANT)
    private String position;       // Jabatan dalam periode (Anggota, Koordinator, dll)
}