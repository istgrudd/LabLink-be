package com.mbclab.lablink.features.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByProjectId(String projectId);
    List<ProjectMember> findByMemberId(String memberId);
    void deleteByProjectIdAndMemberId(String projectId, String memberId);
    boolean existsByProjectIdAndMemberId(String projectId, String memberId);
}
