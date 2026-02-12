package com.mbclab.lablink.shared.status;

/**
 * Status constants for Letter entities.
 */
public final class LetterStatus {
    
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String DOWNLOADED = "DOWNLOADED";
    
    private LetterStatus() {
        // Prevent instantiation
    }
    
    public static boolean isPending(String status) {
        return PENDING.equals(status);
    }
    
    public static boolean isApproved(String status) {
        return APPROVED.equals(status);
    }
    
    public static boolean canDownload(String status) {
        return APPROVED.equals(status) || DOWNLOADED.equals(status);
    }
}
