package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.MemberRoleRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.member.Role;
import com.mbclab.lablink.features.project.dto.ProjectResponse;
import com.mbclab.lablink.shared.approval.AbstractApprovalService;
import com.mbclab.lablink.shared.approval.ApprovalRepository;
import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Project Approval Service - menangani approval workflow untuk Project.
 * 
 * Extends AbstractApprovalService untuk reuse logic approve/reject/getPending.
 * Override verifyApprovalPermission untuk RBAC berbasis divisi.
 */
@Service
@Transactional
public class ProjectApprovalService extends AbstractApprovalService<Project, ProjectResponse> {
    
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    
    public ProjectApprovalService(
            ApplicationEventPublisher eventPublisher,
            ProjectRepository projectRepository,
            MemberRepository memberRepository,
            MemberRoleRepository memberRoleRepository) {
        super(eventPublisher);
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.memberRoleRepository = memberRoleRepository;
    }
    
    @Override
    protected ApprovalRepository<Project, String> getRepository() {
        return projectRepository;
    }
    
    @Override
    protected String getEntityType() {
        return "PROJECT";
    }
    
    @Override
    protected String getNotFoundMessage() {
        return "Project tidak ditemukan";
    }
    
    @Override
    protected ProjectResponse toResponse(Project project) {
        // Leader summary
        ProjectResponse.MemberSummary leaderSummary = null;
        if (project.getLeader() != null) {
            leaderSummary = ProjectResponse.MemberSummary.builder()
                    .id(project.getLeader().getId())
                    .username(project.getLeader().getUsername())
                    .fullName(project.getLeader().getFullName())
                    .expertDivision(project.getLeader().getExpertDivision())
                    .build();
        }
        
        // Team members summary
        List<ProjectResponse.MemberSummary> teamSummary = project.getTeamMembers().stream()
                .map(member -> ProjectResponse.MemberSummary.builder()
                        .id(member.getId())
                        .username(member.getUsername())
                        .fullName(member.getFullName())
                        .expertDivision(member.getExpertDivision())
                        .build())
                .collect(Collectors.toList());
        
        return ProjectResponse.builder()
                .id(project.getId())
                .projectCode(project.getProjectCode())
                .name(project.getName())
                .description(project.getDescription())
                .division(project.getDivision())
                .activityType(project.getActivityType())
                .status(project.getStatus())
                .progressPercent(project.getProgressPercent())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .approvalStatus(project.getApprovalStatus())
                .rejectionReason(project.getRejectionReason())
                .approvedAt(project.getApprovedAt())
                .approvedBy(project.getApprovedBy())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .leader(leaderSummary)
                .teamMembers(teamSummary)
                .build();
    }
    
    /**
     * RBAC: DIVISION_HEAD hanya bisa approve project di divisinya.
     */
    @Override
    protected void verifyApprovalPermission(Project project, String username) {
        ResearchAssistant approver = memberRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User approval tidak ditemukan"));
        
        String userRole = approver.getRole().toUpperCase();
        
        // ADMIN & RESEARCH_COORD can approve everything
        if ("ADMIN".equals(userRole) || 
            "RESEARCH_COORD".equals(userRole) ||
            memberRoleRepository.existsByMemberIdAndRole(approver.getId(), Role.ADMIN) ||
            memberRoleRepository.existsByMemberIdAndRole(approver.getId(), Role.RESEARCH_COORD)) {
            return;
        }
        
        // DIVISION_HEAD can only approve projects in their division
        if (memberRoleRepository.existsByMemberIdAndRole(approver.getId(), Role.DIVISION_HEAD)) {
            String projectDiv = project.getDivision() != null ? project.getDivision().trim() : "";
            String approverDiv = approver.getExpertDivision() != null ? approver.getExpertDivision().trim() : "";
            
            if (!projectDiv.equalsIgnoreCase(approverDiv)) {
                throw new AccessDeniedException("Anda hanya dapat menyetujui proyek di divisi Anda (" + approverDiv + ")");
            }
            return;
        }
        
        throw new AccessDeniedException("Anda tidak memiliki akses untuk menyetujui proyek ini");
    }
}
