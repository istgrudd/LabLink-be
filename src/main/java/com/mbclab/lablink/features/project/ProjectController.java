package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.project.dto.AddMemberRequest;
import com.mbclab.lablink.features.project.dto.CreateProjectRequest;
import com.mbclab.lablink.features.project.dto.ProjectResponse;
import com.mbclab.lablink.features.project.dto.UpdateProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ========== CREATE ==========
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(@RequestBody CreateProjectRequest request) {
        ProjectResponse created = projectService.createProject(request);
        return ResponseEntity.ok(created);
    }

    // ========== READ ==========
    
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) String periodId) {
        if (periodId != null && !periodId.isBlank()) {
            return ResponseEntity.ok(projectService.getProjectsByPeriod(periodId));
        }
        return ResponseEntity.ok(projectService.getAllProjects());
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String id,
            @RequestBody UpdateProjectRequest request) {
        ProjectResponse updated = projectService.updateProject(id, request);
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
}
