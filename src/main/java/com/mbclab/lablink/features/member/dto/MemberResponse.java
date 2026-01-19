package com.mbclab.lablink.features.member.dto;

import com.mbclab.lablink.shared.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Response DTO untuk Member API.
 * Tidak mengekspos password dan field internal.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemberResponse extends BaseResponse {

    private String username;  // NIM
    private String fullName;
    private List<RoleInfo> roles;  // List of roles with metadata
    private String expertDivision;
    private String department;
    
    // Contact info
    private String email;
    private String phoneNumber;
    private String socialMediaLink;
    
    private boolean isActive;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isPasswordChanged")
    private boolean isPasswordChanged;
    
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private String role;
        private String displayName;
        private String description;
    }
}
