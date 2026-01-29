package com.mbclab.lablink.shared.approval;

/**
 * Enum standar untuk approval status.
 * Digunakan oleh semua entity yang implement Approvable.
 */
public final class ApprovalStatus {
    
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    
    private ApprovalStatus() {
        // Prevent instantiation
    }
    
    public static boolean isPending(String status) {
        return PENDING.equals(status);
    }
    
    public static boolean isApproved(String status) {
        return APPROVED.equals(status);
    }
    
    public static boolean isRejected(String status) {
        return REJECTED.equals(status);
    }
}
