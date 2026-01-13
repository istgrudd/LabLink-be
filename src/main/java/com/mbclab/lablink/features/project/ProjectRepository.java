package com.mbclab.lablink.features.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByStatus(String status);
    List<Project> findByDivision(String division);
    List<Project> findByActivityType(String activityType);
}
