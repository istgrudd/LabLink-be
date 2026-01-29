package com.mbclab.lablink.shared.status;

/**
 * Status constants for Event entities.
 */
public final class EventStatus {
    
    public static final String PLANNED = "PLANNED";
    public static final String ONGOING = "ONGOING";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";
    
    private EventStatus() {
        // Prevent instantiation
    }
    
    public static boolean isCompleted(String status) {
        return COMPLETED.equals(status);
    }
    
    public static boolean isActive(String status) {
        return PLANNED.equals(status) || ONGOING.equals(status);
    }
}
