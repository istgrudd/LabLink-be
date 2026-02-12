package com.mbclab.lablink.shared.status;

/**
 * Status pembayaran iuran.
 * Flow: UNPAID → PENDING → VERIFIED
 *                        → REJECTED → PENDING (re-upload)
 */
public enum PaymentStatus {
    UNPAID,
    PENDING,
    VERIFIED,
    REJECTED;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isVerified() {
        return this == VERIFIED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }
}
