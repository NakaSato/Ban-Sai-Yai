package com.bansaiyai.bansaiyai.entity;

import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing payment transactions in the cooperative banking system.
 * Tracks all monetary movements including loan repayments, deposits, fees, and
 * transfers.
 */
@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

  @Column(name = "payment_number", nullable = false, unique = true)
  private String paymentNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loan_id")
  private Loan loan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "saving_account_id")
  private SavingAccount savingAccount;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_type", nullable = false)
  private PaymentType paymentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false)
  private PaymentStatus paymentStatus;

  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(name = "principal_amount", precision = 19, scale = 2)
  private BigDecimal principalAmount;

  @Column(name = "interest_amount", precision = 19, scale = 2)
  private BigDecimal interestAmount;

  @Column(name = "penalty_amount", precision = 19, scale = 2)
  private BigDecimal penaltyAmount;

  @Column(name = "fee_amount", precision = 19, scale = 2)
  private BigDecimal feeAmount;

  @Column(name = "tax_amount", precision = 19, scale = 2)
  private BigDecimal taxAmount;

  @Column(name = "payment_date")
  private LocalDate paymentDate;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Column(name = "processed_date")
  private LocalDateTime processedDate;

  @Column(name = "completed_date")
  private LocalDateTime completedDate;

  @Column(name = "payment_method")
  private String paymentMethod;

  @Column(name = "reference_number")
  private String referenceNumber;

  @Column(name = "transaction_id")
  private String transactionId;

  @Column(name = "bank_account")
  private String bankAccount;

  @Column(name = "receipt_number")
  private String receiptNumber;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "notes", length = 1000)
  private String notes;

  @Column(name = "is_recurring")
  private Boolean isRecurring = false;

  @Column(name = "recurring_frequency")
  private String recurringFrequency;

  @Column(name = "recurring_end_date")
  private LocalDate recurringEndDate;

  @Column(name = "auto_debit")
  private Boolean autoDebit = false;

  @Column(name = "is_verified")
  private Boolean isVerified = false;

  @Column(name = "verified_by")
  private String verifiedBy;

  @Column(name = "verified_date")
  private LocalDateTime verifiedDate;

  @Column(name = "failed_reason", length = 500)
  private String failedReason;

  @Column(name = "reversal_reason", length = 500)
  private String reversalReason;

  @Column(name = "original_payment_id")
  private Long originalPaymentId;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @Version
  @Column(name = "version")
  private Long version;

  // Business logic methods

  /**
   * Check if payment is for a loan
   */
  @Transient
  public boolean isLoanPayment() {
    return paymentType != null && paymentType.isLoanRelated();
  }

  /**
   * Check if payment is for savings
   */
  @Transient
  public boolean isSavingsPayment() {
    return paymentType != null && paymentType.isSavingsRelated();
  }

  /**
   * Check if payment is a fee
   */
  @Transient
  public boolean isFeePayment() {
    return paymentType != null && paymentType.isFee();
  }

  /**
   * Get total amount breakdown
   */
  @Transient
  public BigDecimal getTotalAmount() {
    BigDecimal total = amount;
    if (feeAmount != null) {
      total = total.add(feeAmount);
    }
    if (taxAmount != null) {
      total = total.add(taxAmount);
    }
    return total;
  }

  /**
   * Check if payment is overdue
   */
  @Transient
  public boolean isOverdue() {
    return dueDate != null && LocalDate.now().isAfter(dueDate) &&
        paymentStatus != PaymentStatus.COMPLETED;
  }

  /**
   * Check if payment can be modified
   */
  @Transient
  public boolean canModify() {
    return paymentStatus != null && paymentStatus.isModifiable();
  }

  /**
   * Check if payment requires processing
   */
  @Transient
  public boolean requiresProcessing() {
    return paymentStatus == PaymentStatus.PENDING ||
        paymentStatus == PaymentStatus.VERIFIED;
  }

  /**
   * Get payment amount breakdown for display
   */
  @Transient
  public String getAmountBreakdown() {
    StringBuilder breakdown = new StringBuilder();
    breakdown.append("Principal: ").append(amount != null ? amount : "0.00");

    if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
      breakdown.append(", Interest: ").append(interestAmount);
    }

    if (penaltyAmount != null && penaltyAmount.compareTo(BigDecimal.ZERO) > 0) {
      breakdown.append(", Penalty: ").append(penaltyAmount);
    }

    if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
      breakdown.append(", Fee: ").append(feeAmount);
    }

    return breakdown.toString();
  }

  /**
   * Generate unique payment number
   */
  public static String generatePaymentNumber() {
    return "PAY" + System.currentTimeMillis();
  }

  /**
   * Process payment completion
   */
  public void completePayment() {
    this.paymentStatus = PaymentStatus.COMPLETED;
    this.completedDate = LocalDateTime.now();
    this.processedDate = LocalDateTime.now();
  }

  /**
   * Mark payment as failed
   */
  public void failPayment(String reason) {
    this.paymentStatus = PaymentStatus.FAILED;
    this.failedReason = reason;
    this.processedDate = LocalDateTime.now();
  }

  /**
   * Cancel payment
   */
  public void cancelPayment(String reason) {
    if (canModify()) {
      this.paymentStatus = PaymentStatus.CANCELLED;
      this.notes = (notes != null ? notes + "\n" : "") + "Cancelled: " + reason;
    }
  }

  /**
   * Verify payment
   */
  public void verifyPayment(String verifiedBy) {
    this.isVerified = true;
    this.verifiedBy = verifiedBy;
    this.verifiedDate = LocalDateTime.now();
    this.paymentStatus = PaymentStatus.VERIFIED;
  }

  // Additional getters and setters for Lombok compatibility
  public String getPaymentNumber() {
    return paymentNumber;
  }

  public Member getMember() {
    return member;
  }

  public Loan getLoan() {
    return loan;
  }

  public SavingAccount getSavingAccount() {
    return savingAccount;
  }

  public PaymentType getPaymentType() {
    return paymentType;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getPrincipalAmount() {
    return principalAmount;
  }

  public BigDecimal getInterestAmount() {
    return interestAmount;
  }

  public BigDecimal getPenaltyAmount() {
    return penaltyAmount;
  }

  public BigDecimal getFeeAmount() {
    return feeAmount;
  }

  public BigDecimal getTaxAmount() {
    return taxAmount;
  }

  public LocalDate getPaymentDate() {
    return paymentDate;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public LocalDateTime getProcessedDate() {
    return processedDate;
  }

  public LocalDateTime getCompletedDate() {
    return completedDate;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public String getBankAccount() {
    return bankAccount;
  }

  public String getReceiptNumber() {
    return receiptNumber;
  }

  public String getDescription() {
    return description;
  }

  public String getNotes() {
    return notes;
  }

  public Boolean getIsRecurring() {
    return isRecurring;
  }

  public String getRecurringFrequency() {
    return recurringFrequency;
  }

  public LocalDate getRecurringEndDate() {
    return recurringEndDate;
  }

  public Boolean getAutoDebit() {
    return autoDebit;
  }

  public Boolean getIsVerified() {
    return isVerified;
  }

  public String getVerifiedBy() {
    return verifiedBy;
  }

  public LocalDateTime getVerifiedDate() {
    return verifiedDate;
  }

  public String getFailedReason() {
    return failedReason;
  }

  public String getReversalReason() {
    return reversalReason;
  }

  public Long getOriginalPaymentId() {
    return originalPaymentId;
  }

  // Setters
  public void setPaymentNumber(String paymentNumber) {
    this.paymentNumber = paymentNumber;
  }

  public void setMember(Member member) {
    this.member = member;
  }

  public void setLoan(Loan loan) {
    this.loan = loan;
  }

  public void setSavingAccount(SavingAccount savingAccount) {
    this.savingAccount = savingAccount;
  }

  public void setPaymentType(PaymentType paymentType) {
    this.paymentType = paymentType;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public void setPrincipalAmount(BigDecimal principalAmount) {
    this.principalAmount = principalAmount;
  }

  public void setInterestAmount(BigDecimal interestAmount) {
    this.interestAmount = interestAmount;
  }

  public void setPenaltyAmount(BigDecimal penaltyAmount) {
    this.penaltyAmount = penaltyAmount;
  }

  public void setFeeAmount(BigDecimal feeAmount) {
    this.feeAmount = feeAmount;
  }

  public void setTaxAmount(BigDecimal taxAmount) {
    this.taxAmount = taxAmount;
  }

  public void setPaymentDate(LocalDate paymentDate) {
    this.paymentDate = paymentDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public void setProcessedDate(LocalDateTime processedDate) {
    this.processedDate = processedDate;
  }

  public void setCompletedDate(LocalDateTime completedDate) {
    this.completedDate = completedDate;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public void setReferenceNumber(String referenceNumber) {
    this.referenceNumber = referenceNumber;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public void setBankAccount(String bankAccount) {
    this.bankAccount = bankAccount;
  }

  public void setReceiptNumber(String receiptNumber) {
    this.receiptNumber = receiptNumber;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public void setIsRecurring(Boolean isRecurring) {
    this.isRecurring = isRecurring;
  }

  public void setRecurringFrequency(String recurringFrequency) {
    this.recurringFrequency = recurringFrequency;
  }

  public void setRecurringEndDate(LocalDate recurringEndDate) {
    this.recurringEndDate = recurringEndDate;
  }

  public void setAutoDebit(Boolean autoDebit) {
    this.autoDebit = autoDebit;
  }

  public void setIsVerified(Boolean isVerified) {
    this.isVerified = isVerified;
  }

  public void setVerifiedBy(String verifiedBy) {
    this.verifiedBy = verifiedBy;
  }

  public void setVerifiedDate(LocalDateTime verifiedDate) {
    this.verifiedDate = verifiedDate;
  }

  public void setFailedReason(String failedReason) {
    this.failedReason = failedReason;
  }

  public void setReversalReason(String reversalReason) {
    this.reversalReason = reversalReason;
  }

  public void setOriginalPaymentId(Long originalPaymentId) {
    this.originalPaymentId = originalPaymentId;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  // Manual builder for Lombok compatibility
  public static PaymentBuilder builder() {
    return new PaymentBuilder();
  }

  public static class PaymentBuilder {
    private String paymentNumber;
    private Member member;
    private Loan loan;
    private SavingAccount savingAccount;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private java.math.BigDecimal amount;
    private java.math.BigDecimal principalAmount;
    private java.math.BigDecimal interestAmount;
    private java.math.BigDecimal penaltyAmount;
    private java.math.BigDecimal feeAmount;
    private java.math.BigDecimal taxAmount;
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
    private Boolean isRecurring = false;
    private String recurringFrequency;
    private java.time.LocalDate recurringEndDate;
    private Boolean autoDebit = false;
    private Boolean isVerified = false;
    private String verifiedBy;
    private java.time.LocalDateTime verifiedDate;
    private String failedReason;
    private String reversalReason;
    private Long originalPaymentId;
    private String createdBy;
    private String updatedBy;

    public PaymentBuilder paymentNumber(String paymentNumber) {
      this.paymentNumber = paymentNumber;
      return this;
    }

    public PaymentBuilder member(Member member) {
      this.member = member;
      return this;
    }

    public PaymentBuilder loan(Loan loan) {
      this.loan = loan;
      return this;
    }

    public PaymentBuilder savingAccount(SavingAccount savingAccount) {
      this.savingAccount = savingAccount;
      return this;
    }

    public PaymentBuilder paymentType(PaymentType paymentType) {
      this.paymentType = paymentType;
      return this;
    }

    public PaymentBuilder paymentStatus(PaymentStatus paymentStatus) {
      this.paymentStatus = paymentStatus;
      return this;
    }

    public PaymentBuilder amount(java.math.BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public PaymentBuilder principalAmount(java.math.BigDecimal principalAmount) {
      this.principalAmount = principalAmount;
      return this;
    }

    public PaymentBuilder interestAmount(java.math.BigDecimal interestAmount) {
      this.interestAmount = interestAmount;
      return this;
    }

    public PaymentBuilder penaltyAmount(java.math.BigDecimal penaltyAmount) {
      this.penaltyAmount = penaltyAmount;
      return this;
    }

    public PaymentBuilder feeAmount(java.math.BigDecimal feeAmount) {
      this.feeAmount = feeAmount;
      return this;
    }

    public PaymentBuilder taxAmount(java.math.BigDecimal taxAmount) {
      this.taxAmount = taxAmount;
      return this;
    }

    public PaymentBuilder paymentDate(java.time.LocalDate paymentDate) {
      this.paymentDate = paymentDate;
      return this;
    }

    public PaymentBuilder dueDate(java.time.LocalDate dueDate) {
      this.dueDate = dueDate;
      return this;
    }

    public PaymentBuilder processedDate(java.time.LocalDateTime processedDate) {
      this.processedDate = processedDate;
      return this;
    }

    public PaymentBuilder completedDate(java.time.LocalDateTime completedDate) {
      this.completedDate = completedDate;
      return this;
    }

    public PaymentBuilder paymentMethod(String paymentMethod) {
      this.paymentMethod = paymentMethod;
      return this;
    }

    public PaymentBuilder referenceNumber(String referenceNumber) {
      this.referenceNumber = referenceNumber;
      return this;
    }

    public PaymentBuilder transactionId(String transactionId) {
      this.transactionId = transactionId;
      return this;
    }

    public PaymentBuilder bankAccount(String bankAccount) {
      this.bankAccount = bankAccount;
      return this;
    }

    public PaymentBuilder receiptNumber(String receiptNumber) {
      this.receiptNumber = receiptNumber;
      return this;
    }

    public PaymentBuilder description(String description) {
      this.description = description;
      return this;
    }

    public PaymentBuilder notes(String notes) {
      this.notes = notes;
      return this;
    }

    public PaymentBuilder isRecurring(Boolean isRecurring) {
      this.isRecurring = isRecurring;
      return this;
    }

    public PaymentBuilder recurringFrequency(String recurringFrequency) {
      this.recurringFrequency = recurringFrequency;
      return this;
    }

    public PaymentBuilder recurringEndDate(java.time.LocalDate recurringEndDate) {
      this.recurringEndDate = recurringEndDate;
      return this;
    }

    public PaymentBuilder autoDebit(Boolean autoDebit) {
      this.autoDebit = autoDebit;
      return this;
    }

    public PaymentBuilder isVerified(Boolean isVerified) {
      this.isVerified = isVerified;
      return this;
    }

    public PaymentBuilder verifiedBy(String verifiedBy) {
      this.verifiedBy = verifiedBy;
      return this;
    }

    public PaymentBuilder verifiedDate(java.time.LocalDateTime verifiedDate) {
      this.verifiedDate = verifiedDate;
      return this;
    }

    public PaymentBuilder failedReason(String failedReason) {
      this.failedReason = failedReason;
      return this;
    }

    public PaymentBuilder reversalReason(String reversalReason) {
      this.reversalReason = reversalReason;
      return this;
    }

    public PaymentBuilder originalPaymentId(Long originalPaymentId) {
      this.originalPaymentId = originalPaymentId;
      return this;
    }

    public PaymentBuilder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public PaymentBuilder updatedBy(String updatedBy) {
      this.updatedBy = updatedBy;
      return this;
    }

    public Payment build() {
      Payment payment = new Payment();
      payment.paymentNumber = this.paymentNumber;
      payment.member = this.member;
      payment.loan = this.loan;
      payment.savingAccount = this.savingAccount;
      payment.paymentType = this.paymentType;
      payment.paymentStatus = this.paymentStatus;
      payment.amount = this.amount;
      payment.principalAmount = this.principalAmount;
      payment.interestAmount = this.interestAmount;
      payment.penaltyAmount = this.penaltyAmount;
      payment.feeAmount = this.feeAmount;
      payment.taxAmount = this.taxAmount;
      payment.paymentDate = this.paymentDate;
      payment.dueDate = this.dueDate;
      payment.processedDate = this.processedDate;
      payment.completedDate = this.completedDate;
      payment.paymentMethod = this.paymentMethod;
      payment.referenceNumber = this.referenceNumber;
      payment.transactionId = this.transactionId;
      payment.bankAccount = this.bankAccount;
      payment.receiptNumber = this.receiptNumber;
      payment.description = this.description;
      payment.notes = this.notes;
      payment.isRecurring = this.isRecurring;
      payment.recurringFrequency = this.recurringFrequency;
      payment.recurringEndDate = this.recurringEndDate;
      payment.autoDebit = this.autoDebit;
      payment.isVerified = this.isVerified;
      payment.verifiedBy = this.verifiedBy;
      payment.verifiedDate = this.verifiedDate;
      payment.failedReason = this.failedReason;
      payment.reversalReason = this.reversalReason;
      payment.originalPaymentId = this.originalPaymentId;
      payment.createdBy = this.createdBy;
      payment.updatedBy = this.updatedBy;
      return payment;
    }
  }
}
