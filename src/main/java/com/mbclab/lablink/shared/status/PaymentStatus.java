package com.mbclab.lablink.shared.status;

/**
 * Status constants for Payment/Dues entities.
 */
public final class PaymentStatus {
    
    public static final String UNPAID = "UNPAID";
    public static final String PENDING = "PENDING";      // Submitted, waiting verification
    public static final String VERIFIED = "VERIFIED";
    public static final String REJECTED = "REJECTED";
    
    private PaymentStatus() {
        // Prevent instantiation
    }
    
    public static boolean isPending(String status) {
        return PENDING.equals(status);
    }
    
    public static boolean isVerified(String status) {
        return VERIFIED.equals(status);
    }
}
