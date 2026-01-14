package com.mbclab.lablink.features.activitylog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
    List<ActivityLog> findByTargetType(String targetType);
    List<ActivityLog> findByUserId(String userId);
    List<ActivityLog> findByUserName(String userName);
    List<ActivityLog> findByAction(String action);
    
    // With pagination (for large datasets)
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<ActivityLog> findByTargetTypeOrderByCreatedAtDesc(String targetType, Pageable pageable);
}
