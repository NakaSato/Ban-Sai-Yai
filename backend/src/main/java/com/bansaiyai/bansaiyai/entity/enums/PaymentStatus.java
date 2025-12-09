package com.bansaiyai.bansaiyai.entity.enums;

/**
 * Enumeration for payment status in cooperative banking system.
 * Tracks the lifecycle of payments from initiation to completion.
 */
public enum PaymentStatus {

  PENDING("Pending", "Payment initiated but not yet processed"),
  PROCESSING("Processing", "Payment is being processed"),
  COMPLETED("Completed", "Payment successfully processed"),
  FAILED("Failed", "Payment processing failed"),
  CANCELLED("Cancelled", "Payment was cancelled"),
  REVERSED("Reversed", "Payment was reversed"),
  PARTIAL("Partial", "Partial payment received"),
  OVERDUE("Overdue", "Payment is past due date"),
  SCHEDULED("Scheduled", "Payment scheduled for future processing"),
  HOLD("Hold", "Payment placed on hold"),
  VERIFIED("Verified", "Payment verified and ready for processing");

  private final String displayName;
  private final String description;

  PaymentStatus(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Check if payment is in an active state
   */
  public boolean isActive() {
    return this == PENDING || this == PROCESSING || this == SCHEDULED || this == HOLD;
  }

  /**
   * Check if payment is completed successfully
   */
  public boolean isCompleted() {
    return this == COMPLETED || this == VERIFIED;
  }

  /**
   * Check if payment has failed or been cancelled
   */
  public boolean isFailed() {
    return this == FAILED || this == CANCELLED || this == REVERSED;
  }

  /**
   * Check if payment can be modified
   */
  public boolean isModifiable() {
    return this == PENDING || this == SCHEDULED || this == HOLD;
  }

  /**
   * Check if payment requires attention
   */
  public boolean requiresAttention() {
    return this == FAILED || this == OVERDUE || this == HOLD;
  }
}
