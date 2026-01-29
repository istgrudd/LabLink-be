package com.mbclab.lablink.features.archive;

import com.mbclab.lablink.features.archive.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    // ========== CREATE ==========
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECH_OPS')")
    public ResponseEntity<ArchiveResponse> createArchive(@Valid @RequestBody CreateArchiveRequest request) {
        ArchiveResponse created = archiveService.createArchive(request);
        return ResponseEntity.ok(created);
    }

    // ========== READ ==========
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ArchiveResponse>> getAllArchives(
            @RequestParam(required = false) String periodId) {
        if (periodId != null && !periodId.isBlank()) {
            return ResponseEntity.ok(archiveService.getArchivesByPeriod(periodId));
        }
        return ResponseEntity.ok(archiveService.getAllArchives());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArchiveResponse> getArchiveById(@PathVariable String id) {
        return ResponseEntity.ok(archiveService.getArchiveById(id));
    }

    @GetMapping("/code/{archiveCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArchiveResponse> getArchiveByCode(@PathVariable String archiveCode) {
        return ResponseEntity.ok(archiveService.getArchiveByCode(archiveCode));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ArchiveResponse>> getArchivesByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(archiveService.getArchivesByProject(projectId));
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ArchiveResponse>> getArchivesByEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(archiveService.getArchivesByEvent(eventId));
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ArchiveResponse>> getArchivesByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(archiveService.getArchivesByDepartment(department));
    }



    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECH_OPS')")
    public ResponseEntity<ArchiveResponse> updateArchive(
            @PathVariable String id,
            @Valid @RequestBody UpdateArchiveRequest request) {
        ArchiveResponse updated = archiveService.updateArchive(id, request);
        return ResponseEntity.ok(updated);
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECH_OPS')")
    public ResponseEntity<Void> deleteArchive(@PathVariable String id) {
        archiveService.deleteArchive(id);
        return ResponseEntity.noContent().build();
    }
}
