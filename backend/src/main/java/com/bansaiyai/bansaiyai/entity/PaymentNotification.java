package com.bansaiyai.bansaiyai.entity;

import com.bansaiyai.bansaiyai.entity.enums.NotificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentNotification entity representing member-submitted payment
 * notifications.
 * This is a temporary holding table for payments that require officer
 * verification.
 * Once approved, the payment is migrated to the permanent payments table.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Entity
@Table(name = "payment_notification", indexes = {
        @Index(name = "idx_payment_notification_status_created", columnList = "status,createdAt"),
        @Index(name = "idx_payment_notification_member_id", columnList = "memberId"),
        @Index(name = "idx_payment_notification_loan_id", columnList = "loanId"),
        @Index(name = "idx_payment_notification_pay_date", columnList = "payDate")
})
@EntityListeners(AuditingEntityListener.class)
public class PaymentNotification extends BaseEntity {

    @Column(name = "noti_id", insertable = false, updatable = false)
    private Long notiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @NotNull(message = "Member is required")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @NotNull(message = "Loan is required")
    private Loan loan;

    @Column(name = "pay_amount", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    private BigDecimal payAmount;

    @Column(name = "pay_date", nullable = false)
    @NotNull(message = "Payment date is required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDateTime payDate;

    @Column(name = "slip_image", length = 255)
    @Size(max = 255, message = "Slip image path must not exceed 255 characters")
    private String slipImage;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_user_id")
    private User rejectedByUser;

    @Column(name = "officer_comment", columnDefinition = "TEXT")
    private String officerComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private Receipt receipt;

    @Version
    @Column(name = "version")
    private Long version;

    // Business logic methods

    /**
     * Check if notification can be approved or rejected
     */
    public boolean canBeProcessed() {
        return status == NotificationStatus.PENDING;
    }

    /**
     * Check if notification is pending
     */
    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }

    /**
     * Check if notification has been approved
     */
    public boolean isApproved() {
        return status == NotificationStatus.APPROVED;
    }

    /**
     * Check if notification has been rejected
     */
    public boolean isRejected() {
        return status == NotificationStatus.REJECTED;
    }

    /**
     * Get days since notification was created
     */
    public long getDaysPending() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Check if notification is overdue (pending for more than 3 days)
     */
    public boolean isOverdue() {
        return isPending() && getDaysPending() > 3;
    }

    /**
     * Approve the notification
     */
    public void approve(User approver, Payment payment, Receipt receipt) {
        if (!canBeProcessed()) {
            throw new IllegalStateException("Notification cannot be approved in current status: " + status);
        }
        this.status = NotificationStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.approvedByUser = approver;
        this.payment = payment;
        this.receipt = receipt;
    }

    /**
     * Reject the notification
     */
    public void reject(User rejector, String comment) {
        if (!canBeProcessed()) {
            throw new IllegalStateException("Notification cannot be rejected in current status: " + status);
        }
        this.status = NotificationStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.rejectedByUser = rejector;
        this.officerComment = comment;
    }

    /**
     * Get display status in Thai
     */
    public String getStatusDisplayThai() {
        return switch (status) {
            case PENDING -> "รอตรวจสอบ";
            case APPROVED -> "อนุมัติแล้ว";
            case REJECTED -> "ไม่อนุมัติ";
        };
    }

    /**
     * Get slip image URL for display
     */
    public String getSlipImageUrl() {
        if (slipImage == null || slipImage.isEmpty()) {
            return null;
        }
        return "/api/files/slips/" + slipImage;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters (Lombok will generate these, but adding some for clarity)

    public Long getNotiId() {
        return notiId != null ? notiId : getId();
    }

    public Member getMember() {
        return member;
    }

    public Loan getLoan() {
        return loan;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public LocalDateTime getPayDate() {
        return payDate;
    }

    public String getSlipImage() {
        return slipImage;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public User getApprovedByUser() {
        return approvedByUser;
    }

    public User getRejectedByUser() {
        return rejectedByUser;
    }

    public String getOfficerComment() {
        return officerComment;
    }

    public Payment getPayment() {
        return payment;
    }

    public Receipt getReceipt() {
        return receipt;
    }
}
