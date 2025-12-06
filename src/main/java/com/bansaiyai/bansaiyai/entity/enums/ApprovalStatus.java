package com.bansaiyai.bansaiyai.entity.enums;

/**
 * Enum representing the approval status of transactions and other business
 * operations.
 */
public enum ApprovalStatus {
  PENDING("Pending Approval"),
  APPROVED("Approved"),
  REJECTED("Rejected"),
  CANCELLED("Cancelled");

  private final String description;

  ApprovalStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
