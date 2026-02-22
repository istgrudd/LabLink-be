package com.mbclab.lablink.features.archive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveRepository extends JpaRepository<Archive, String> {
    Optional<Archive> findByArchiveCode(String archiveCode);
    List<Archive> findByProjectId(String projectId);
    List<Archive> findByEventId(String eventId);
    List<Archive> findByDepartment(String department);
    List<Archive> findByArchiveType(String archiveType);
    long countByArchiveCodeStartingWith(String prefix);
}
