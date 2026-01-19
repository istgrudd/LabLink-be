package com.mbclab.lablink.features.member;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * Tabel relasi member-role untuk mendukung multiple roles per member.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "member_roles", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "role"}),
    indexes = {
        @Index(name = "idx_member_role_member_id", columnList = "member_id"),
        @Index(name = "idx_member_role_role", columnList = "role")
    }
)
public class MemberRole {

    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private ResearchAssistant member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime assignedAt;
    
    private String assignedBy;  // Admin username

    public MemberRole(ResearchAssistant member, Role role, String assignedBy) {
        this.member = member;
        this.role = role;
        this.assignedAt = LocalDateTime.now();
        this.assignedBy = assignedBy;
    }
}
