package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.project.dto.AssignMemberRequest;
import com.mbclab.lablink.features.project.dto.CreateProjectRequest;
import com.mbclab.lablink.features.project.dto.UpdateProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberRepository memberRepository;

    // ========== CRUD Project ==========

    @Transactional
    public Project createProject(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDivision(request.getDivision());
        project.setActivityType(request.getActivityType());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setDescription(request.getDescription());
        project.setStatus("NOT_STARTED");
        project.setProgressPercent(0);

        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(String id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project dengan ID " + id + " tidak ditemukan"));

        // Partial update - hanya update jika tidak null
        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName());
        }
        if (request.getDivision() != null && !request.getDivision().isBlank()) {
            project.setDivision(request.getDivision());
        }
        if (request.getActivityType() != null && !request.getActivityType().isBlank()) {
            project.setActivityType(request.getActivityType());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            project.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getProgressPercent() != null) {
            // Validasi range 0-100
            int progress = Math.max(0, Math.min(100, request.getProgressPercent()));
            project.setProgressPercent(progress);
        }

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(String id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project ID " + id + " tidak ditemukan");
        }
        projectRepository.deleteById(id);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project dengan ID " + id + " tidak ditemukan"));
    }

    // ========== Member Assignment ==========

    @Transactional
    public ProjectMember assignMember(String projectId, AssignMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));

        ResearchAssistant member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));

        // Cek apakah sudah ada assignment
        if (projectMemberRepository.existsByProjectIdAndMemberId(projectId, request.getMemberId())) {
            throw new RuntimeException("Member sudah terdaftar di proyek ini");
        }

        ProjectMember projectMember = new ProjectMember(project, member, request.getRole());
        return projectMemberRepository.save(projectMember);
    }

    @Transactional
    public void removeMember(String projectId, String memberId) {
        if (!projectMemberRepository.existsByProjectIdAndMemberId(projectId, memberId)) {
            throw new RuntimeException("Member tidak terdaftar di proyek ini");
        }
        projectMemberRepository.deleteByProjectIdAndMemberId(projectId, memberId);
    }

    public List<ProjectMember> getProjectMembers(String projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }
}
