package com.mbclab.lablink.features.period.dto;

import lombok.Data;

@Data
public class AddMemberToPeriodRequest {
    private String memberId;
    private String position;  // Jabatan
}
