package com.mbclab.lablink.shared.status;

/**
 * Status constants for Project entities.
 */
public final class ProjectStatus {
    
    public static final String NOT_STARTED = "NOT_STARTED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";
    
    private ProjectStatus() {
        // Prevent instantiation
    }
    
    public static boolean isCompleted(String status) {
        return COMPLETED.equals(status);
    }
    
    public static boolean isActive(String status) {
        return IN_PROGRESS.equals(status) || ON_HOLD.equals(status);
    }
}
