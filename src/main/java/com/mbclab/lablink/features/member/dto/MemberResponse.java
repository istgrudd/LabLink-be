package com.mbclab.lablink.features.member.dto;

import com.mbclab.lablink.shared.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
    private String role;
    private String expertDivision;
    private String department;
    
    // Contact info
    private String email;
    private String phoneNumber;
    private String socialMediaLink;
    
    private boolean isActive;
    private boolean isPasswordChanged;
}
