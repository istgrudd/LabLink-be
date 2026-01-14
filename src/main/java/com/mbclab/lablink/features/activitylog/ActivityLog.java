package com.mbclab.lablink.features.activitylog;

import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity untuk menyimpan audit trail / activity log.
 * Merekam: siapa melakukan apa, kapan, terhadap data yang mana.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_activity_log_target_type", columnList = "targetType"),
    @Index(name = "idx_activity_log_user_id", columnList = "userId"),
    @Index(name = "idx_activity_log_created_at", columnList = "createdAt")
})
public class ActivityLog extends BaseEntity {

    // Action: CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    @Column(nullable = false)
    private String action;

    // Target type: PROJECT, MEMBER, EVENT, LETTER, ARCHIVE, etc
    @Column(nullable = false)
    private String targetType;

    // ID of the affected entity
    private String targetId;

    // Name of the affected entity (stored for reference even if entity is deleted)
    private String targetName;

    // Detailed description of the activity
    @Column(columnDefinition = "TEXT")
    private String description;

    // User who performed the action
    private String userId;
    private String userName;

    // IP address (optional)
    private String ipAddress;
}
