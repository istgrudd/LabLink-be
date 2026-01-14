package com.mbclab.lablink.features.archive;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.archive.dto.*;
import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.event.EventRepository;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import com.mbclab.lablink.features.project.Project;
import com.mbclab.lablink.features.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;
    private final ArchiveCodeGenerator archiveCodeGenerator;
    private final AcademicPeriodRepository periodRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Valid archive types per source
    private static final Set<String> PROJECT_TYPES = Set.of("PUBLIKASI", "HKI", "PKM");
    private static final Set<String> EVENT_TYPES = Set.of("LAPORAN", "SERTIFIKAT");

    // ========== CREATE ==========
    
    @Transactional
    public ArchiveResponse createArchive(CreateArchiveRequest request) {
        String sourceType = request.getSourceType().toUpperCase();
        String archiveType = request.getArchiveType().toUpperCase();
        
        // Validate archive type matches source type
        validateArchiveType(sourceType, archiveType);
        
        // Generate code
        String archiveCode = archiveCodeGenerator.generate(archiveType);
        
        // Create archive
        Archive archive = new Archive();
        archive.setArchiveCode(archiveCode);
        archive.setTitle(request.getTitle());
        archive.setDescription(request.getDescription());
        archive.setArchiveType(archiveType);
        archive.setSourceType(sourceType);
        archive.setPublishLocation(request.getPublishLocation());
        archive.setReferenceNumber(request.getReferenceNumber());
        archive.setPublishDate(request.getPublishDate());
        
        // Set source and department
        if ("PROJECT".equals(sourceType)) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project tidak ditemukan"));
            
            // Validate project is completed
            if (!"COMPLETED".equals(project.getStatus())) {
                throw new RuntimeException("Project harus berstatus COMPLETED untuk membuat arsip");
            }
            
            archive.setProject(project);
            archive.setDepartment("INTERNAL");
        } else if ("EVENT".equals(sourceType)) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
            
            // Validate event is completed
            if (!"COMPLETED".equals(event.getStatus())) {
                throw new RuntimeException("Event harus berstatus COMPLETED untuk membuat arsip");
            }
            
            archive.setEvent(event);
            archive.setDepartment("EKSTERNAL");
        } else {
            throw new RuntimeException("Source type tidak valid: " + sourceType);
        }
        
        // Auto-assign to active period
        periodRepository.findByIsActiveTrue().ifPresent(archive::setPeriod);
        
        Archive saved = archiveRepository.save(archive);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.create(
                "ARCHIVE", saved.getId(), saved.getTitle(),
                "Created archive: " + saved.getArchiveCode()));
        
        return toResponse(saved);
    }

    // ========== READ ==========
    
    public List<ArchiveResponse> getAllArchives() {
        return archiveRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ArchiveResponse> getArchivesByPeriod(String periodId) {
        return archiveRepository.findByPeriodId(periodId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ArchiveResponse getArchiveById(String id) {
        Archive archive = archiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Archive tidak ditemukan"));
        return toResponse(archive);
    }

    public ArchiveResponse getArchiveByCode(String archiveCode) {
        Archive archive = archiveRepository.findByArchiveCode(archiveCode)
                .orElseThrow(() -> new RuntimeException("Archive tidak ditemukan"));
        return toResponse(archive);
    }

    public List<ArchiveResponse> getArchivesByProject(String projectId) {
        return archiveRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ArchiveResponse> getArchivesByEvent(String eventId) {
        return archiveRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ArchiveResponse> getArchivesByDepartment(String department) {
        return archiveRepository.findByDepartment(department.toUpperCase()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ========== UPDATE ==========
    
    @Transactional
    public ArchiveResponse updateArchive(String id, UpdateArchiveRequest request) {
        Archive archive = archiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Archive tidak ditemukan"));
        
        // Partial update
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            archive.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            archive.setDescription(request.getDescription());
        }
        if (request.getPublishLocation() != null) {
            archive.setPublishLocation(request.getPublishLocation());
        }
        if (request.getReferenceNumber() != null) {
            archive.setReferenceNumber(request.getReferenceNumber());
        }
        if (request.getPublishDate() != null) {
            archive.setPublishDate(request.getPublishDate());
        }
        
        Archive saved = archiveRepository.save(archive);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.update(
                "ARCHIVE", saved.getId(), saved.getTitle(),
                "Updated archive: " + saved.getArchiveCode()));
        
        return toResponse(saved);
    }

    // ========== DELETE ==========
    
    @Transactional
    public void deleteArchive(String id) {
        Archive archive = archiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Archive tidak ditemukan"));
        String title = archive.getTitle();
        String code = archive.getArchiveCode();
        
        archiveRepository.deleteById(id);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.delete(
                "ARCHIVE", id, title,
                "Deleted archive: " + code));
    }

    // ========== HELPER ==========
    
    private void validateArchiveType(String sourceType, String archiveType) {
        if ("PROJECT".equals(sourceType) && !PROJECT_TYPES.contains(archiveType)) {
            throw new RuntimeException("Archive type untuk Project harus: PUBLIKASI, HKI, atau PKM");
        }
        if ("EVENT".equals(sourceType) && !EVENT_TYPES.contains(archiveType)) {
            throw new RuntimeException("Archive type untuk Event harus: LAPORAN atau SERTIFIKAT");
        }
    }

    private ArchiveResponse toResponse(Archive archive) {
        // Build source info
        ArchiveResponse.SourceInfo sourceInfo = null;
        
        if (archive.getProject() != null) {
            Project project = archive.getProject();
            sourceInfo = ArchiveResponse.SourceInfo.builder()
                    .id(project.getId())
                    .code(project.getProjectCode())
                    .name(project.getName())
                    .leader(project.getLeader() != null ? project.getLeader().getFullName() : null)
                    .build();
        } else if (archive.getEvent() != null) {
            Event event = archive.getEvent();
            sourceInfo = ArchiveResponse.SourceInfo.builder()
                    .id(event.getId())
                    .code(event.getEventCode())
                    .name(event.getName())
                    .leader(event.getPic() != null ? event.getPic().getFullName() : null)
                    .build();
        }
        
        return ArchiveResponse.builder()
                .id(archive.getId())
                .archiveCode(archive.getArchiveCode())
                .title(archive.getTitle())
                .description(archive.getDescription())
                .archiveType(archive.getArchiveType())
                .department(archive.getDepartment())
                .sourceType(archive.getSourceType())
                .source(sourceInfo)
                .publishLocation(archive.getPublishLocation())
                .referenceNumber(archive.getReferenceNumber())
                .publishDate(archive.getPublishDate())
                .createdAt(archive.getCreatedAt())
                .updatedAt(archive.getUpdatedAt())
                .build();
    }
}
