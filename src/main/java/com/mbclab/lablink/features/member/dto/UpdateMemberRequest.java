package com.mbclab.lablink.features.member.dto;

import lombok.Data;

@Data
public class UpdateMemberRequest {
    // Kita buat semua field ini, tapi nanti di Service kita cek
    // Kalau null (kosong), berarti gak usah diubah.
    
    private String fullName;
    private String expertDivision;
    private String department;
    private String email;
    private String phoneNumber;
    private String socialMediaLink;
}