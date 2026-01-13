package com.mbclab.lablink.features.project.dto;

import lombok.Data;

@Data
public class AssignMemberRequest {
    private String memberId;
    private String role;  // KETUA, ANGGOTA
}
