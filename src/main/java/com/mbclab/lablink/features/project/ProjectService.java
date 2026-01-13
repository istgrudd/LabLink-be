package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.project.dto.CreateProjectRequest;
import com.mbclab.lablink.features.project.dto.ProjectResponse;
import com.mbclab.lablink.features.project.dto.UpdateProjectRequest;
import lombok.RequiredArgsConstructor;
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
    private final ProjectCodeGenerator projectCodeGenerator;

    // ========== CREATE ==========
    
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        // 1. Generate project code
        String projectCode = projectCodeGenerator.generate(request.getActivityType());
        
        // 2. Get leader
        ResearchAssistant leader = memberRepository.findById(request.getLeaderId())
                .orElseThrow(() -> new RuntimeException("Leader tidak ditemukan"));
        
        // 3. Get team members
        Set<ResearchAssistant> teamMembers = new HashSet<>();
        if (request.getTeamMemberIds() != null && !request.getTeamMemberIds().isEmpty()) {
            teamMembers = request.getTeamMemberIds().stream()
                    .map(id -> memberRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Member " + id + " tidak ditemukan")))
                    .collect(Collectors.toSet());
        }
        
        // 4. Create project
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
        
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    // ========== READ ==========
    
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
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
        
        // Partial update
        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getDivision() != null && !request.getDivision().isBlank()) {
            project.setDivision(request.getDivision());
        }
        if (request.getActivityType() != null && !request.getActivityType().isBlank()) {
            project.setActivityType(request.getActivityType().toUpperCase());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            project.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getProgressPercent() != null) {
            int progress = Math.max(0, Math.min(100, request.getProgressPercent()));
            project.setProgressPercent(progress);
        }
        
        // Update leader
        if (request.getLeaderId() != null && !request.getLeaderId().isBlank()) {
            ResearchAssistant leader = memberRepository.findById(request.getLeaderId())
                    .orElseThrow(() -> new RuntimeException("Leader tidak ditemukan"));
            project.setLeader(leader);
        }
        
        // Update team members (replace all)
        if (request.getTeamMemberIds() != null) {
            Set<ResearchAssistant> teamMembers = request.getTeamMemberIds().stream()
                    .map(memberId -> memberRepository.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("Member tidak ditemukan")))
                    .collect(Collectors.toSet());
            project.setTeamMembers(teamMembers);
        }
        
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    // ========== DELETE ==========
    
    @Transactional
    public void deleteProject(String id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project tidak ditemukan");
        }
        projectRepository.deleteById(id);
    }

    // ========== MEMBER MANAGEMENT ==========
    
    @Transactional
    public ProjectResponse addMember(String projectId, String memberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
        
        ResearchAssistant member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        
        // Check if already a member
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
        
        // Check if member exists in project
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
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .leader(leaderSummary)
                .teamMembers(teamSummary)
                .build();
    }
}
