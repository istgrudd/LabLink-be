package com.mbclab.lablink.features.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response untuk daftar role yang tersedia.
 * Digunakan frontend untuk menampilkan dropdown role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private String role;        // ADMIN, SECRETARY, etc.
    private String displayName; // Administrator, Sekretaris, etc.
    private String description; // Full description
}
