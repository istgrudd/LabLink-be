package com.mbclab.lablink.features.activitylog;

import com.mbclab.lablink.features.activitylog.dto.ActivityLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    /**
     * Get all logs with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ActivityLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(activityLogService.getAllLogs(page, size));
    }

    /**
     * Get recent logs (default: last 100)
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getRecentLogs(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(activityLogService.getRecentLogs(limit));
    }

    /**
     * Get logs by target type (PROJECT, MEMBER, EVENT, etc)
     */
    @GetMapping("/target/{targetType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByTargetType(@PathVariable String targetType) {
        return ResponseEntity.ok(activityLogService.getLogsByTargetType(targetType));
    }

    /**
     * Get logs by user
     */
    @GetMapping("/user/{userName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByUser(@PathVariable String userName) {
        return ResponseEntity.ok(activityLogService.getLogsByUser(userName));
    }

    /**
     * Get logs by action (CREATE, UPDATE, DELETE, etc)
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByAction(@PathVariable String action) {
        return ResponseEntity.ok(activityLogService.getLogsByAction(action));
    }
}
