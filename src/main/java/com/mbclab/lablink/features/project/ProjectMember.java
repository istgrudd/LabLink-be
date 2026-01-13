package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.member.ResearchAssistant;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "project_members")
@IdClass(ProjectMemberId.class)
public class ProjectMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Project project;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private ResearchAssistant member;

    // Role dalam proyek: KETUA, ANGGOTA
    @Column(nullable = false)
    private String role;

    public ProjectMember(Project project, ResearchAssistant member, String role) {
        this.project = project;
        this.member = member;
        this.role = role;
    }
}
