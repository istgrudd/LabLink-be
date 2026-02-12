package com.mbclab.lablink.shared.status;

/**
 * Status constants for Letter entities.
 * Flow: PENDING → REVIEWED → APPROVED → SIGNED
 * Rejection allowed from: PENDING or REVIEWED
 */
public final class LetterStatus {
    
    public static final String PENDING = "PENDING";
    public static final String REVIEWED = "REVIEWED";
    public static final String APPROVED = "APPROVED";
    public static final String SIGNED = "SIGNED";
    public static final String REJECTED = "REJECTED";
    public static final String DOWNLOADED = "DOWNLOADED";
    
    private LetterStatus() {
        // Prevent instantiation
    }
    
    public static boolean isPending(String status) {
        return PENDING.equals(status);
    }
    
    public static boolean isReviewed(String status) {
        return REVIEWED.equals(status);
    }
    
    public static boolean isApproved(String status) {
        return APPROVED.equals(status);
    }
    
    public static boolean isSigned(String status) {
        return SIGNED.equals(status);
    }
    
    public static boolean canDownload(String status) {
        return APPROVED.equals(status) || SIGNED.equals(status) || DOWNLOADED.equals(status);
    }
}
