package com.bansaiyai.bansaiyai.entity.enums;

/**
 * Enum representing the status of a payment notification.
 * Used in the mobile payment notification workflow.
 */
public enum NotificationStatus {
    /**
     * Payment notification has been submitted by member and is awaiting officer
     * verification
     */
    PENDING,

    /**
     * Payment notification has been verified and approved by officer.
     * Payment record has been created and receipt generated.
     */
    APPROVED,

    /**
     * Payment notification has been rejected by officer.
     * Member needs to resubmit with corrections.
     */
    REJECTED;

    /**
     * Check if notification can be modified
     */
    public boolean isModifiable() {
        return this == PENDING;
    }

    /**
     * Check if notification has been processed
     */
    public boolean isProcessed() {
        return this == APPROVED || this == REJECTED;
    }

    /**
     * Check if notification is final (cannot be changed)
     */
    public boolean isFinal() {
        return this == APPROVED;
    }
}
