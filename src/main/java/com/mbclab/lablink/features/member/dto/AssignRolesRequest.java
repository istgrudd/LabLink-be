package com.mbclab.lablink.features.member.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignRolesRequest {
    private List<String> roles;  // List of role names: "ADMIN", "DIVISION_HEAD", etc.
}
