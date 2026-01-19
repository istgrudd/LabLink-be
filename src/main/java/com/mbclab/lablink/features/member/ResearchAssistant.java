package com.mbclab.lablink.features.member;

import com.mbclab.lablink.features.auth.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "research_assistants", indexes = {
    @Index(name = "idx_member_is_active", columnList = "isActive"),
    @Index(name = "idx_member_expert_division", columnList = "expertDivision")
})
public class ResearchAssistant extends AppUser {

    // Field Spesifik Bisnis Lab
    @Column(nullable = false)
    private String expertDivision; // Misal: BIG_DATA, CYBER_SECURITY, GAME_TECH, SDI

    @Column(nullable = false)
    private String department;     // Misal: Internal atau Eksternal

    // Field Kontak (Diisi nanti oleh User saat Aktivasi)
    private String email;
    private String phoneNumber;
    private String socialMediaLink;
    
    // Status Keanggotaan (Opsional, buat jaga-jaga kalau ada alumni)
    private boolean isActive = true;
    
    // Multiple roles support
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberRole> memberRoles = new ArrayList<>();
}
