package com.mbclab.lablink.features.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    
    List<Project> findByStatus(String status);
    List<Project> findByDivision(String division);
    List<Project> findByActivityType(String activityType);
    List<Project> findByPeriodId(String periodId);
    
    Optional<Project> findByProjectCode(String projectCode);
    
    // Untuk generate project code
    long countByActivityType(String activityType);
    
    // Untuk period summary
    int countByPeriodId(String periodId);
    
    // For cascade delete
    // For cascade delete
    void deleteByPeriodId(String periodId);
    
    // For orphan filter
    List<Project> findByPeriodIsNull();
    
    // For approval workflow
    List<Project> findByApprovalStatus(String approvalStatus);
}
