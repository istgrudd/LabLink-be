package com.mbclab.lablink.features.member.dto;

import lombok.Data;

@Data
public class CreateMemberRequest {

    private String nim;           // Username / NIM Mahasiswa
    private String fullName;      // Nama Lengkap
    private String expertDivision; // Misal: Big Data, Cyber Security
    private String department;     // Misal: Informatika
    private String role;           // Optional: ADMIN, ASSISTANT (default: ASSISTANT)
}