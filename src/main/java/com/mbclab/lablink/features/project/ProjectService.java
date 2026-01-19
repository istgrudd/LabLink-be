package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.MemberRoleRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.member.Role;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import com.mbclab.lablink.features.project.dto.CreateProjectRequest;
import com.mbclab.lablink.features.project.dto.ProjectResponse;
import com.mbclab.lablink.features.project.dto.UpdateProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository; // Added for RBAC
    private final ProjectCodeGenerator projectCodeGenerator;
    private final AcademicPeriodRepository periodRepository;
    private final com.mbclab.lablink.features.archive.ArchiveRepository archiveRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ... (CREATE, READ, UPDATE, DELETE, MEMBER MANAGEMENT methods remain same) ...
    // Note: I will copy them below to ensure file integrity, but main changes are in APPROVAL WORKFLOW

    // ========== CREATE ==========
    
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        String projectCode = projectCodeGenerator.generate(request.getActivityType());
        
        ResearchAssistant leader = memberRepository.findById(request.getLeaderId())
                .orElseThrow(() -> new RuntimeException("Leader tidak ditemukan"));
        
        Set<ResearchAssistant> teamMembers = new HashSet<>();
        if (request.getTeamMemberIds() != null && !request.getTeamMemberIds().isEmpty()) {
            teamMembers = request.getTeamMemberIds().stream()
                    .map(id -> memberRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Member " + id + " tidak ditemukan")))
                    .collect(Collectors.toSet());
        }
        
        Project project = new Project();
        project.setProjectCode(projectCode);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setDivision(request.getDivision());
        project.setActivityType(request.getActivityType().toUpperCase());
        project.setStatus("NOT_STARTED");
        project.setProgressPercent(0);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setLeader(leader);
        project.setTeamMembers(teamMembers);
        
        periodRepository.findByIsActiveTrue().ifPresent(project::setPeriod);
        
        Project saved = projectRepository.save(project);
        
        eventPublisher.publishEvent(AuditEvent.create(
                "PROJECT", saved.getId(), saved.getName(),
                "Created project: " + saved.getProjectCode()));
        
        return toResponse(saved);
    }

    // ========== READ ==========
    
    public Page<ProjectResponse> getAllProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return projectRepository.findAll(pageable).map(this::toResponse);
    }
    
    public List<ProjectResponse> getAllProjectsUnpaginated() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getProjectsByPeriod(String periodId) {
        return projectRepository.findByPeriodId(periodId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getOrphanProjects() {
        return projectRepository.findByPeriodIsNull().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getPendingProjects() {
        return projectRepository.findByApprovalStatus("PENDING").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        return toResponse(project);
    }

    public ProjectResponse getProjectByCode(String projectCode) {
        Project project = projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        return toResponse(project);
    }

    // ========== UPDATE ==========
    
    @Transactional
    public ProjectResponse updateProject(String id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        
        if (request.getName() != null && !request.getName().isBlank()) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getDivision() != null && !request.getDivision().isBlank()) project.setDivision(request.getDivision());
        if (request.getActivityType() != null && !request.getActivityType().isBlank()) project.setActivityType(request.getActivityType().toUpperCase());
        if (request.getStatus() != null && !request.getStatus().isBlank()) project.setStatus(request.getStatus().toUpperCase());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
        if (request.getProgressPercent() != null) {
            int progress = Math.max(0, Math.min(100, request.getProgressPercent()));
            project.setProgressPercent(progress);
        }
        
        if (request.getLeaderId() != null && !request.getLeaderId().isBlank()) {
            ResearchAssistant leader = memberRepository.findById(request.getLeaderId())
                    .orElseThrow(() -> new RuntimeException("Leader tidak ditemukan"));
            project.setLeader(leader);
        }
        
        if (request.getTeamMemberIds() != null) {
            Set<ResearchAssistant> teamMembers = request.getTeamMemberIds().stream()
                    .map(memberId -> memberRepository.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("Member tidak ditemukan")))
                    .collect(Collectors.toSet());
            project.setTeamMembers(teamMembers);
        }
        
        Project saved = projectRepository.save(project);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PROJECT", saved.getId(), saved.getName(),
                "Updated project: " + saved.getProjectCode()));
        
        return toResponse(saved);
    }

    // ========== DELETE ==========
    
    @Transactional
    public void deleteProject(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        String projectName = project.getName();
        String projectCode = project.getProjectCode();
        
        if (!archiveRepository.findByProjectId(id).isEmpty()) {
            throw new RuntimeException("Gagal menghapus: Proyek ini memiliki arsip (dokumen/publikasi) yang terhubung. Hapus arsip terkait terlebih dahulu.");
        }
        
        project.getTeamMembers().clear();
        projectRepository.saveAndFlush(project);
        projectRepository.delete(project);
        projectRepository.flush();
        
        eventPublisher.publishEvent(AuditEvent.delete(
                "PROJECT", id, projectName,
                "Deleted project: " + projectCode));
    }

    // ========== MEMBER MANAGEMENT ==========
    
    @Transactional
    public ProjectResponse addMember(String projectId, String memberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        
        ResearchAssistant member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        
        if (project.getTeamMembers().contains(member)) {
            throw new RuntimeException("Member sudah terdaftar di project ini");
        }
        
        project.getTeamMembers().add(member);
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }
    
    @Transactional
    public ProjectResponse removeMember(String projectId, String memberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        
        ResearchAssistant member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        
        if (!project.getTeamMembers().contains(member)) {
            throw new RuntimeException("Member tidak terdaftar di project ini");
        }
        
        project.getTeamMembers().remove(member);
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }
    
    public List<ProjectResponse.MemberSummary> getProjectMembers(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        
        return project.getTeamMembers().stream()
                .map(member -> ProjectResponse.MemberSummary.builder()
                        .id(member.getId())
                        .username(member.getUsername())
                        .fullName(member.getFullName())
                        .expertDivision(member.getExpertDivision())
                        .build())
                .collect(Collectors.toList());
    }

    // ========== APPROVAL WORKFLOW ==========
    
    @Transactional
    public ProjectResponse approveProject(String id, String approvedByUsername) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        
        if (!"PENDING".equals(project.getApprovalStatus())) {
            throw new RuntimeException("Project sudah diproses sebelumnya");
        }
        
        verifyApprovalPermission(project, approvedByUsername);
        
        project.setApprovalStatus("APPROVED");
        project.setApprovedAt(java.time.LocalDate.now());
        project.setApprovedBy(approvedByUsername);
        
        Project saved = projectRepository.save(project);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PROJECT", saved.getId(), saved.getName(),
                "Approved project: " + saved.getProjectCode()));
        
        return toResponse(saved);
    }
    
    @Transactional
    public ProjectResponse rejectProject(String id, String rejectionReason, String rejectedByUsername) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        
        if (!"PENDING".equals(project.getApprovalStatus())) {
            throw new RuntimeException("Project sudah diproses sebelumnya");
        }
        
        verifyApprovalPermission(project, rejectedByUsername);
        
        project.setApprovalStatus("REJECTED");
        project.setRejectionReason(rejectionReason);
        project.setApprovedBy(rejectedByUsername);
        
        Project saved = projectRepository.save(project);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PROJECT", saved.getId(), saved.getName(),
                "Rejected project: " + saved.getProjectCode() + " - Reason: " + rejectionReason));
        
        return toResponse(saved);
    }
    
    private void verifyApprovalPermission(Project project, String username) {
        ResearchAssistant approver = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User approval tidak ditemukan"));
        
        // ADMIN & RESEARCH_COORD can approve everything
        if (memberRoleRepository.existsByMemberIdAndRole(approver.getId(), Role.ADMIN) ||
            memberRoleRepository.existsByMemberIdAndRole(approver.getId(), Role.RESEARCH_COORD)) {
            return;
        }
        
        // DIVISION_HEAD can only approve projects in their division
        if (memberRoleRepository.existsByMemberIdAndRole(approver.getId(), Role.DIVISION_HEAD)) {
            // Check division match
            // Note: project.getDivision() should ideally match approver.getExpertDivision()
            // We use case-insensitive check to be safe
            String projectDiv = project.getDivision() != null ? project.getDivision().trim() : "";
            String approverDiv = approver.getExpertDivision() != null ? approver.getExpertDivision().trim() : "";
            
            if (!projectDiv.equalsIgnoreCase(approverDiv)) {
                throw new RuntimeException("Anda hanya dapat menyetujui proyek di divisi Anda (" + approverDiv + ")");
            }
            return;
        }
        
        // If not one of the above roles
        throw new RuntimeException("Anda tidak memiliki akses untuk menyetujui proyek ini");
    }

    // ========== HELPER: Convert to Response DTO ==========
    
    private ProjectResponse toResponse(Project project) {
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
}
