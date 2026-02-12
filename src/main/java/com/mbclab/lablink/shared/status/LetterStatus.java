package com.mbclab.lablink.shared.status;

/**
 * Status surat keluar.
 * Flow: PENDING → REVIEWED → APPROVED → SIGNED
 *                           → REJECTED
 */
public enum LetterStatus {
    PENDING,
    REVIEWED,
    APPROVED,
    SIGNED,
    REJECTED,
    DOWNLOADED;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isReviewed() {
        return this == REVIEWED;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isSigned() {
        return this == SIGNED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean canDownload() {
        return this == APPROVED || this == SIGNED || this == DOWNLOADED;
    }
}
