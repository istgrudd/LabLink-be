package com.mbclab.lablink.features.member;

import com.mbclab.lablink.features.auth.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "research_assistants")
public class ResearchAssistant extends AppUser {

    // Field Spesifik Bisnis Lab
    @Column(nullable = false)
    private String expertDivision; // Misal: Big Data, Cyber Security

    @Column(nullable = false)
    private String department;     // Misal: Internal atau Eksternal

    // Field Kontak (Diisi nanti oleh User saat Aktivasi)
    private String email;
    private String phoneNumber;
    private String socialMediaLink;
    
    // Status Keanggotaan (Opsional, buat jaga-jaga kalau ada alumni)
    private boolean isActive = true;
}
