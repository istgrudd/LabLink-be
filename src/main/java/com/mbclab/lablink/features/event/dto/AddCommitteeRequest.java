package com.mbclab.lablink.features.event.dto;

import lombok.Data;

@Data
public class AddCommitteeRequest {
    private String memberId;
    private String role;  // "Humas", "Acara", "Bendahara", dll
}
