package com.mbclab.lablink.features.activitylog;

import com.mbclab.lablink.features.activitylog.dto.ActivityLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * Get all logs with pagination (latest first)
     */
    public Page<ActivityLogResponse> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return activityLogRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Get recent logs (default: last 100)
     */
    public List<ActivityLogResponse> getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return activityLogRepository.findAll(pageable).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get logs by target type
     */
    public List<ActivityLogResponse> getLogsByTargetType(String targetType) {
        return activityLogRepository.findByTargetType(targetType.toUpperCase()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get logs by user
     */
    public List<ActivityLogResponse> getLogsByUser(String userName) {
        return activityLogRepository.findByUserName(userName).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get logs by action type
     */
    public List<ActivityLogResponse> getLogsByAction(String action) {
        return activityLogRepository.findByAction(action.toUpperCase()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .targetName(log.getTargetName())
                .description(log.getDescription())
                .userId(log.getUserId())
                .userName(log.getUserName())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}
