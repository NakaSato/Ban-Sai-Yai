package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment notification response (System -> Client)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotificationResponse {

    // Use UUIDs for security (prevent ID enumeration)
    private String notificationUuid;
    private String memberUuid;
    private String loanUuid;

    // Internal ID (needed for some operations)
    private Long notificationId;

    // Business IDs (safe to expose)
    private String memberNumber;
    private String loanNumber;

    // Member details
    private String memberName;

    // Loan details
    private BigDecimal outstandingBalance;

    // Payment details
    private BigDecimal payAmount;
    private LocalDateTime payDate;
    private String slipImageUrl;
    private String notes;

    // Status
    private NotificationStatus status;
    private String statusDisplay;

    // Approval details
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime rejectedAt;
    private String rejectedBy;
    private String officerComment;

    // Audit
    private LocalDateTime createdAt;
    private Integer daysPending;
    private Boolean isOverdue;
}
