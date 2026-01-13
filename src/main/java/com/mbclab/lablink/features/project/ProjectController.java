package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.project.dto.AssignMemberRequest;
import com.mbclab.lablink.features.project.dto.CreateProjectRequest;
import com.mbclab.lablink.features.project.dto.UpdateProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ========== CRUD Project ==========

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest request) {
        Project created = projectService.createProject(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable String id,
            @RequestBody UpdateProjectRequest request) {
        Project updated = projectService.updateProject(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Member Assignment ==========

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectMember> assignMember(
            @PathVariable String id,
            @RequestBody AssignMemberRequest request) {
        ProjectMember assigned = projectService.assignMember(id, request);
        return ResponseEntity.ok(assigned);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ProjectMember>> getProjectMembers(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getProjectMembers(id));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String id,
            @PathVariable String memberId) {
        projectService.removeMember(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
