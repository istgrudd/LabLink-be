package com.mbclab.lablink.features.project;

import com.mbclab.lablink.shared.approval.ApprovalRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends ApprovalRepository<Project, String> {
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"leader", "teamMembers"})
    org.springframework.data.domain.Page<Project> findAll(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"leader", "teamMembers"})
    List<Project> findAll();

    List<Project> findByStatus(String status);
    List<Project> findByDivision(String division);
    List<Project> findByActivityType(String activityType);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"leader", "teamMembers"})
    List<Project> findByPeriodId(String periodId);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"leader", "teamMembers"})
    Optional<Project> findByProjectCode(String projectCode);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"leader", "teamMembers"})
    Optional<Project> findById(String id);
    
    // Untuk generate project code
    long countByActivityType(String activityType);
    
    // Untuk period summary
    int countByPeriodId(String periodId);
    
    // For cascade delete
    void deleteByPeriodId(String periodId);
    
    // For orphan filter
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"leader", "teamMembers"})
    List<Project> findByPeriodIsNull();
    
    // For approval workflow
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"leader", "teamMembers"})
    List<Project> findByApprovalStatus(String approvalStatus);
}
