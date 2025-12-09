package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning payment data.
 * Contains payment information with calculated fields and formatted dates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

  private Long id;
  private String paymentNumber;
  private Long memberId;
  private String memberName;
  private String memberCode;
  private Long loanId;
  private String loanNumber;
  private Long savingAccountId;
  private String savingAccountNumber;
  private PaymentType paymentType;
  private String paymentTypeDisplay;
  private PaymentStatus paymentStatus;
  private String paymentStatusDisplay;
  private BigDecimal amount;
  private BigDecimal principalAmount;
  private BigDecimal interestAmount;
  private BigDecimal penaltyAmount;
  private BigDecimal feeAmount;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
  private LocalDate paymentDate;
  private LocalDate dueDate;
  private LocalDateTime processedDate;
  private LocalDateTime completedDate;
  private String paymentMethod;
  private String referenceNumber;
  private String transactionId;
  private String bankAccount;
  private String receiptNumber;
  private String description;
  private String notes;
  private Boolean isRecurring;
  private String recurringFrequency;
  private LocalDate recurringEndDate;
  private Boolean autoDebit;
  private Boolean isVerified;
  private String verifiedBy;
  private LocalDateTime verifiedDate;
  private String failedReason;
  private String reversalReason;
  private Long originalPaymentId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;

  // Calculated fields
  private Boolean isOverdue;
  private Boolean canModify;
  private Boolean requiresProcessing;
  private Integer daysOverdue;
  private String amountBreakdown;
  private String statusDescription;

  // Convenience methods for display
  public String getFormattedAmount() {
    if (amount == null)
      return "0.00";
    return String.format("%,.2f", amount);
  }

  public String getFormattedTotalAmount() {
    if (totalAmount == null)
      return "0.00";
    return String.format("%,.2f", totalAmount);
  }

  public String getFormattedPaymentDate() {
    if (paymentDate == null)
      return "";
    return paymentDate.toString();
  }

  public String getFormattedDueDate() {
    if (dueDate == null)
      return "";
    return dueDate.toString();
  }

  public String getFormattedProcessedDate() {
    if (processedDate == null)
      return "";
    return processedDate.toString();
  }

  public String getPaymentTypeDescription() {
    return paymentType != null ? paymentType.getDisplayName() : "";
  }

  public String getPaymentStatusDescription() {
    return paymentStatus != null ? paymentStatus.getDisplayName() : "";
  }

  public Boolean isLoanPayment() {
    return paymentType != null && paymentType.isLoanRelated();
  }

  public Boolean isSavingsPayment() {
    return paymentType != null && paymentType.isSavingsRelated();
  }

  public Boolean isFeePayment() {
    return paymentType != null && paymentType.isFee();
  }

  // Static factory method from entity
  public static PaymentResponse fromEntity(com.bansaiyai.bansaiyai.entity.Payment payment) {
    if (payment == null)
      return null;

    return PaymentResponse.builder()
        .id(payment.getId())
        .paymentNumber(payment.getPaymentNumber())
        .memberId(payment.getMember() != null ? payment.getMember().getId() : null)
        .memberName(payment.getMember() != null ? payment.getMember().getName() : null)
        .loanId(payment.getLoan() != null ? payment.getLoan().getId() : null)
        .loanNumber(payment.getLoan() != null ? payment.getLoan().getLoanNumber() : null)
        .savingAccountId(payment.getSavingAccount() != null ? payment.getSavingAccount().getId() : null)
        .savingAccountNumber(payment.getSavingAccount() != null ? payment.getSavingAccount().getAccountNumber() : null)
        .paymentType(payment.getPaymentType())
        .paymentTypeDisplay(payment.getPaymentType() != null ? payment.getPaymentType().getDisplayName() : null)
        .paymentStatus(payment.getPaymentStatus())
        .paymentStatusDisplay(payment.getPaymentStatus() != null ? payment.getPaymentStatus().getDisplayName() : null)
        .amount(payment.getAmount())
        .principalAmount(payment.getPrincipalAmount())
        .interestAmount(payment.getInterestAmount())
        .penaltyAmount(payment.getPenaltyAmount())
        .feeAmount(payment.getFeeAmount())
        .taxAmount(payment.getTaxAmount())
        .totalAmount(payment.getTotalAmount())
        .paymentDate(payment.getPaymentDate())
        .dueDate(payment.getDueDate())
        .processedDate(payment.getProcessedDate())
        .completedDate(payment.getCompletedDate())
        .paymentMethod(payment.getPaymentMethod())
        .referenceNumber(payment.getReferenceNumber())
        .transactionId(payment.getTransactionId())
        .bankAccount(payment.getBankAccount())
        .receiptNumber(payment.getReceiptNumber())
        .description(payment.getDescription())
        .notes(payment.getNotes())
        .isRecurring(payment.getIsRecurring())
        .recurringFrequency(payment.getRecurringFrequency())
        .recurringEndDate(payment.getRecurringEndDate())
        .autoDebit(payment.getAutoDebit())
        .isVerified(payment.getIsVerified())
        .verifiedBy(payment.getVerifiedBy())
        .verifiedDate(payment.getVerifiedDate())
        .failedReason(payment.getFailedReason())
        .reversalReason(payment.getReversalReason())
        .originalPaymentId(payment.getOriginalPaymentId())
        .createdAt(payment.getCreatedAt())
        .updatedAt(payment.getUpdatedAt())
        .createdBy(payment.getCreatedBy())
        .isOverdue(payment.isOverdue())
        .canModify(payment.canModify())
        .requiresProcessing(payment.requiresProcessing())
        .daysOverdue(calculateDaysOverdue(payment))
        .amountBreakdown(payment.getAmountBreakdown())
        .statusDescription(getStatusDescription(payment))
        .build();
  }

  private static Integer calculateDaysOverdue(com.bansaiyai.bansaiyai.entity.Payment payment) {
    if (payment.getDueDate() == null || payment.getPaymentStatus() == null) {
      return null;
    }

    if (payment.getPaymentStatus().isCompleted()) {
      return 0;
    }

    LocalDate today = LocalDate.now();
    if (today.isBefore(payment.getDueDate())) {
      return 0;
    }

    return (int) java.time.temporal.ChronoUnit.DAYS.between(payment.getDueDate(), today);
  }

  private static String getStatusDescription(com.bansaiyai.bansaiyai.entity.Payment payment) {
    if (payment.getPaymentStatus() == null) {
      return "Unknown";
    }

    StringBuilder description = new StringBuilder(payment.getPaymentStatus().getDescription());

    if (payment.isOverdue()) {
      Integer daysOverdue = calculateDaysOverdue(payment);
      description.append(" (").append(daysOverdue).append(" days overdue)");
    }

    if (payment.getFailedReason() != null && !payment.getFailedReason().trim().isEmpty()) {
      description.append(" - ").append(payment.getFailedReason());
    }

    return description.toString();
  }

  // Manual builder for Lombok compatibility
  public static PaymentResponseBuilder builder() {
    return new PaymentResponseBuilder();
  }

  public static class PaymentResponseBuilder {
    private Long id;
    private String paymentNumber;
    private Long memberId;
    private String memberName;
    private String memberCode;
    private Long loanId;
    private String loanNumber;
    private Long savingAccountId;
    private String savingAccountNumber;
    private PaymentType paymentType;
    private String paymentTypeDisplay;
    private PaymentStatus paymentStatus;
    private String paymentStatusDisplay;
    private java.math.BigDecimal amount;
    private java.math.BigDecimal principalAmount;
    private java.math.BigDecimal interestAmount;
    private java.math.BigDecimal penaltyAmount;
    private java.math.BigDecimal feeAmount;
    private java.math.BigDecimal taxAmount;
    private java.math.BigDecimal totalAmount;
    private java.time.LocalDate paymentDate;
    private java.time.LocalDate dueDate;
    private java.time.LocalDateTime processedDate;
    private java.time.LocalDateTime completedDate;
    private String paymentMethod;
    private String referenceNumber;
    private String transactionId;
    private String bankAccount;
    private String receiptNumber;
    private String description;
    private String notes;
    private Boolean isRecurring;
    private String recurringFrequency;
    private java.time.LocalDate recurringEndDate;
    private Boolean autoDebit;
    private Boolean isVerified;
    private String verifiedBy;
    private java.time.LocalDateTime verifiedDate;
    private String failedReason;
    private String reversalReason;
    private Long originalPaymentId;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String createdBy;
    private Boolean isOverdue;
    private Boolean canModify;
    private Boolean requiresProcessing;
    private Integer daysOverdue;
    private String amountBreakdown;
    private String statusDescription;

    public PaymentResponseBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public PaymentResponseBuilder paymentNumber(String paymentNumber) {
      this.paymentNumber = paymentNumber;
      return this;
    }

    public PaymentResponseBuilder memberId(Long memberId) {
      this.memberId = memberId;
      return this;
    }

    public PaymentResponseBuilder memberName(String memberName) {
      this.memberName = memberName;
      return this;
    }

    public PaymentResponseBuilder memberCode(String memberCode) {
      this.memberCode = memberCode;
      return this;
    }

    public PaymentResponseBuilder loanId(Long loanId) {
      this.loanId = loanId;
      return this;
    }

    public PaymentResponseBuilder loanNumber(String loanNumber) {
      this.loanNumber = loanNumber;
      return this;
    }

    public PaymentResponseBuilder savingAccountId(Long savingAccountId) {
      this.savingAccountId = savingAccountId;
      return this;
    }

    public PaymentResponseBuilder savingAccountNumber(String savingAccountNumber) {
      this.savingAccountNumber = savingAccountNumber;
      return this;
    }

    public PaymentResponseBuilder paymentType(PaymentType paymentType) {
      this.paymentType = paymentType;
      return this;
    }

    public PaymentResponseBuilder paymentTypeDisplay(String paymentTypeDisplay) {
      this.paymentTypeDisplay = paymentTypeDisplay;
      return this;
    }

    public PaymentResponseBuilder paymentStatus(PaymentStatus paymentStatus) {
      this.paymentStatus = paymentStatus;
      return this;
    }

    public PaymentResponseBuilder paymentStatusDisplay(String paymentStatusDisplay) {
      this.paymentStatusDisplay = paymentStatusDisplay;
      return this;
    }

    public PaymentResponseBuilder amount(java.math.BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public PaymentResponseBuilder principalAmount(java.math.BigDecimal principalAmount) {
      this.principalAmount = principalAmount;
      return this;
    }

    public PaymentResponseBuilder interestAmount(java.math.BigDecimal interestAmount) {
      this.interestAmount = interestAmount;
      return this;
    }

    public PaymentResponseBuilder penaltyAmount(java.math.BigDecimal penaltyAmount) {
      this.penaltyAmount = penaltyAmount;
      return this;
    }

    public PaymentResponseBuilder feeAmount(java.math.BigDecimal feeAmount) {
      this.feeAmount = feeAmount;
      return this;
    }

    public PaymentResponseBuilder taxAmount(java.math.BigDecimal taxAmount) {
      this.taxAmount = taxAmount;
      return this;
    }

    public PaymentResponseBuilder totalAmount(java.math.BigDecimal totalAmount) {
      this.totalAmount = totalAmount;
      return this;
    }

    public PaymentResponseBuilder paymentDate(java.time.LocalDate paymentDate) {
      this.paymentDate = paymentDate;
      return this;
    }

    public PaymentResponseBuilder dueDate(java.time.LocalDate dueDate) {
      this.dueDate = dueDate;
      return this;
    }

    public PaymentResponseBuilder processedDate(java.time.LocalDateTime processedDate) {
      this.processedDate = processedDate;
      return this;
    }

    public PaymentResponseBuilder completedDate(java.time.LocalDateTime completedDate) {
      this.completedDate = completedDate;
      return this;
    }

    public PaymentResponseBuilder paymentMethod(String paymentMethod) {
      this.paymentMethod = paymentMethod;
      return this;
    }

    public PaymentResponseBuilder referenceNumber(String referenceNumber) {
      this.referenceNumber = referenceNumber;
      return this;
    }

    public PaymentResponseBuilder transactionId(String transactionId) {
      this.transactionId = transactionId;
      return this;
    }

    public PaymentResponseBuilder bankAccount(String bankAccount) {
      this.bankAccount = bankAccount;
      return this;
    }

    public PaymentResponseBuilder receiptNumber(String receiptNumber) {
      this.receiptNumber = receiptNumber;
      return this;
    }

    public PaymentResponseBuilder description(String description) {
      this.description = description;
      return this;
    }

    public PaymentResponseBuilder notes(String notes) {
      this.notes = notes;
      return this;
    }

    public PaymentResponseBuilder isRecurring(Boolean isRecurring) {
      this.isRecurring = isRecurring;
      return this;
    }

    public PaymentResponseBuilder recurringFrequency(String recurringFrequency) {
      this.recurringFrequency = recurringFrequency;
      return this;
    }

    public PaymentResponseBuilder recurringEndDate(java.time.LocalDate recurringEndDate) {
      this.recurringEndDate = recurringEndDate;
      return this;
    }

    public PaymentResponseBuilder autoDebit(Boolean autoDebit) {
      this.autoDebit = autoDebit;
      return this;
    }

    public PaymentResponseBuilder isVerified(Boolean isVerified) {
      this.isVerified = isVerified;
      return this;
    }

    public PaymentResponseBuilder verifiedBy(String verifiedBy) {
      this.verifiedBy = verifiedBy;
      return this;
    }

    public PaymentResponseBuilder verifiedDate(java.time.LocalDateTime verifiedDate) {
      this.verifiedDate = verifiedDate;
      return this;
    }

    public PaymentResponseBuilder failedReason(String failedReason) {
      this.failedReason = failedReason;
      return this;
    }

    public PaymentResponseBuilder reversalReason(String reversalReason) {
      this.reversalReason = reversalReason;
      return this;
    }

    public PaymentResponseBuilder originalPaymentId(Long originalPaymentId) {
      this.originalPaymentId = originalPaymentId;
      return this;
    }

    public PaymentResponseBuilder createdAt(java.time.LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public PaymentResponseBuilder updatedAt(java.time.LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public PaymentResponseBuilder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public PaymentResponseBuilder isOverdue(Boolean isOverdue) {
      this.isOverdue = isOverdue;
      return this;
    }

    public PaymentResponseBuilder canModify(Boolean canModify) {
      this.canModify = canModify;
      return this;
    }

    public PaymentResponseBuilder requiresProcessing(Boolean requiresProcessing) {
      this.requiresProcessing = requiresProcessing;
      return this;
    }

    public PaymentResponseBuilder daysOverdue(Integer daysOverdue) {
      this.daysOverdue = daysOverdue;
      return this;
    }

    public PaymentResponseBuilder amountBreakdown(String amountBreakdown) {
      this.amountBreakdown = amountBreakdown;
      return this;
    }

    public PaymentResponseBuilder statusDescription(String statusDescription) {
      this.statusDescription = statusDescription;
      return this;
    }

    public PaymentResponse build() {
      PaymentResponse response = new PaymentResponse();
      response.id = this.id;
      response.paymentNumber = this.paymentNumber;
      response.memberId = this.memberId;
      response.memberName = this.memberName;
      response.memberCode = this.memberCode;
      response.loanId = this.loanId;
      response.loanNumber = this.loanNumber;
      response.savingAccountId = this.savingAccountId;
      response.savingAccountNumber = this.savingAccountNumber;
      response.paymentType = this.paymentType;
      response.paymentTypeDisplay = this.paymentTypeDisplay;
      response.paymentStatus = this.paymentStatus;
      response.paymentStatusDisplay = this.paymentStatusDisplay;
      response.amount = this.amount;
      response.principalAmount = this.principalAmount;
      response.interestAmount = this.interestAmount;
      response.penaltyAmount = this.penaltyAmount;
      response.feeAmount = this.feeAmount;
      response.taxAmount = this.taxAmount;
      response.totalAmount = this.totalAmount;
      response.paymentDate = this.paymentDate;
      response.dueDate = this.dueDate;
      response.processedDate = this.processedDate;
      response.completedDate = this.completedDate;
      response.paymentMethod = this.paymentMethod;
      response.referenceNumber = this.referenceNumber;
      response.transactionId = this.transactionId;
      response.bankAccount = this.bankAccount;
      response.receiptNumber = this.receiptNumber;
      response.description = this.description;
      response.notes = this.notes;
      response.isRecurring = this.isRecurring;
      response.recurringFrequency = this.recurringFrequency;
      response.recurringEndDate = this.recurringEndDate;
      response.autoDebit = this.autoDebit;
      response.isVerified = this.isVerified;
      response.verifiedBy = this.verifiedBy;
      response.verifiedDate = this.verifiedDate;
      response.failedReason = this.failedReason;
      response.reversalReason = this.reversalReason;
      response.originalPaymentId = this.originalPaymentId;
      response.createdAt = this.createdAt;
      response.updatedAt = this.updatedAt;
      response.createdBy = this.createdBy;
      response.isOverdue = this.isOverdue;
      response.canModify = this.canModify;
      response.requiresProcessing = this.requiresProcessing;
      response.daysOverdue = this.daysOverdue;
      response.amountBreakdown = this.amountBreakdown;
      response.statusDescription = this.statusDescription;
      return response;
    }
  }
}
