package com.mbclab.lablink.features.activitylog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event class untuk Spring Events.
 * Di-publish oleh Service saat ada action, di-listen oleh AuditEventListener.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String action;       // CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    private String targetType;   // PROJECT, MEMBER, EVENT, LETTER, etc
    private String targetId;
    private String targetName;
    private String description;
    
    // Helper static methods for common actions
    public static AuditEvent create(String targetType, String targetId, String targetName, String description) {
        return AuditEvent.builder()
                .action("CREATE")
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .description(description)
                .build();
    }
    
    public static AuditEvent update(String targetType, String targetId, String targetName, String description) {
        return AuditEvent.builder()
                .action("UPDATE")
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .description(description)
                .build();
    }
    
    public static AuditEvent delete(String targetType, String targetId, String targetName, String description) {
        return AuditEvent.builder()
                .action("DELETE")
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .description(description)
                .build();
    }
    
    public static AuditEvent login(String userId, String userName) {
        return AuditEvent.builder()
                .action("LOGIN")
                .targetType("AUTH")
                .targetId(userId)
                .targetName(userName)
                .description("User logged in: " + userName)
                .build();
    }
    
    public static AuditEvent logout(String userId, String userName) {
        return AuditEvent.builder()
                .action("LOGOUT")
                .targetType("AUTH")
                .targetId(userId)
                .targetName(userName)
                .description("User logged out: " + userName)
                .build();
    }
}
