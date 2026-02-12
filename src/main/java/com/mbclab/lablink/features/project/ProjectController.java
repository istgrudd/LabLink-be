package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.project.dto.AddMemberRequest;
import com.mbclab.lablink.features.project.dto.CreateProjectRequest;
import com.mbclab.lablink.features.project.dto.ProjectResponse;
import com.mbclab.lablink.features.project.dto.RejectProjectRequest;
import com.mbclab.lablink.features.project.dto.UpdateProjectRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ========== CREATE ==========
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse created = projectService.createProject(request);
        return ResponseEntity.ok(created);
    }

    // ========== READ ==========
    
    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String periodId) {
        if (periodId != null && !periodId.isBlank()) {
            List<ProjectResponse> all = projectService.getProjectsByPeriod(periodId);
            org.springframework.data.domain.Pageable pageableReq = org.springframework.data.domain.PageRequest.of(page, size);
            int start = Math.min((int)pageableReq.getOffset(), all.size());
            int end = Math.min((start + size), all.size());
            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(all.subList(start, end), pageableReq, all.size()));
        }
        return ResponseEntity.ok(projectService.getAllProjects(page, size));
    }
    
    @GetMapping("/by-period/{periodId}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByPeriod(@PathVariable String periodId) {
        return ResponseEntity.ok(projectService.getProjectsByPeriod(periodId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/code/{projectCode}")
    public ResponseEntity<ProjectResponse> getProjectByCode(@PathVariable String projectCode) {
        return ResponseEntity.ok(projectService.getProjectByCode(projectCode));
    }



    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectResponse updated = projectService.updateProject(id, request, username);
        return ResponseEntity.ok(updated);
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // ========== MEMBER MANAGEMENT ==========
    
    @PostMapping("/{projectId}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable String projectId,
            @RequestBody AddMemberRequest request) {
        ProjectResponse updated = projectService.addMember(projectId, request.getMemberId());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectResponse.MemberSummary>> getProjectMembers(
            @PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getProjectMembers(projectId));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> removeMember(
            @PathVariable String projectId,
            @PathVariable String memberId) {
        ProjectResponse updated = projectService.removeMember(projectId, memberId);
        return ResponseEntity.ok(updated);
    }

    // ========== APPROVAL WORKFLOW ==========
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjectResponse>> getPendingProjects() {
        return ResponseEntity.ok(projectService.getPendingProjects());
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESEARCH_COORD', 'DIVISION_HEAD')")
    public ResponseEntity<ProjectResponse> approveProject(
            @PathVariable String id,
            Authentication authentication) {
        String approvedBy = authentication.getName();
        ProjectResponse approved = projectService.approveProject(id, approvedBy);
        return ResponseEntity.ok(approved);
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESEARCH_COORD', 'DIVISION_HEAD')")
    public ResponseEntity<ProjectResponse> rejectProject(
            @PathVariable String id,
            @RequestBody RejectProjectRequest request,
            Authentication authentication) {
        String rejectedBy = authentication.getName();
        ProjectResponse rejected = projectService.rejectProject(id, request.getRejectionReason(), rejectedBy);
        return ResponseEntity.ok(rejected);
    }
}
