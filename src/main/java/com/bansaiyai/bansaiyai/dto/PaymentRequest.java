package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating or processing payments.
 * Contains validation rules and payment processing parameters.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

  @NotNull(message = "Member ID is required")
  private Long memberId;

  private Long loanId;

  private Long savingAccountId;

  @NotNull(message = "Payment type is required")
  private PaymentType paymentType;

  @NotNull(message = "Payment amount is required")
  @DecimalMin(value = "0.01", message = "Payment amount must be at least 0.01")
  @DecimalMax(value = "1000000.00", message = "Payment amount cannot exceed 1,000,000.00")
  private BigDecimal amount;

  private BigDecimal principalAmount;

  private BigDecimal interestAmount;

  private BigDecimal penaltyAmount;

  private BigDecimal feeAmount;

  private BigDecimal taxAmount;

  @Future(message = "Payment date cannot be in the past")
  private LocalDate paymentDate;

  private LocalDate dueDate;

  @Size(max = 50, message = "Payment method cannot exceed 50 characters")
  private String paymentMethod;

  @Size(max = 100, message = "Reference number cannot exceed 100 characters")
  private String referenceNumber;

  @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
  private String transactionId;

  @Size(max = 50, message = "Bank account cannot exceed 50 characters")
  private String bankAccount;

  @Size(max = 50, message = "Receipt number cannot exceed 50 characters")
  private String receiptNumber;

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  private String description;

  @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
  private String notes;

  private Boolean isRecurring = false;

  @Size(max = 20, message = "Recurring frequency cannot exceed 20 characters")
  private String recurringFrequency;

  private LocalDate recurringEndDate;

  private Boolean autoDebit = false;

  private Boolean isVerified = false;

  // Validation methods for loan payments
  @AssertTrue(message = "Loan ID is required for loan payments", groups = LoanPaymentValidation.class)
  private boolean isLoanPaymentValid() {
    if (paymentType != null && paymentType.isLoanRelated()) {
      return loanId != null;
    }
    return true;
  }

  @AssertTrue(message = "Principal and interest amounts required for loan payments", groups = LoanPaymentValidation.class)
  private boolean isLoanAmountBreakdownValid() {
    if (paymentType != null && paymentType.isLoanRelated()) {
      return principalAmount != null && interestAmount != null;
    }
    return true;
  }

  // Validation methods for savings payments
  @AssertTrue(message = "Savings account ID is required for savings payments", groups = SavingsPaymentValidation.class)
  private boolean isSavingsPaymentValid() {
    if (paymentType != null && paymentType.isSavingsRelated()) {
      return savingAccountId != null;
    }
    return true;
  }

  // Validation for payment amounts
  @AssertTrue(message = "Amount breakdown must match total amount")
  private boolean isAmountBreakdownValid() {
    if (principalAmount == null && interestAmount == null &&
        penaltyAmount == null && feeAmount == null && taxAmount == null) {
      return true; // No breakdown provided
    }

    BigDecimal total = BigDecimal.ZERO;
    if (principalAmount != null)
      total = total.add(principalAmount);
    if (interestAmount != null)
      total = total.add(interestAmount);
    if (penaltyAmount != null)
      total = total.add(penaltyAmount);
    if (feeAmount != null)
      total = total.add(feeAmount);
    if (taxAmount != null)
      total = total.add(taxAmount);

    return amount != null && amount.compareTo(total) == 0;
  }

  // Validation for recurring payments
  @AssertTrue(message = "Recurring frequency is required for recurring payments")
  private boolean isRecurringPaymentValid() {
    if (Boolean.TRUE.equals(isRecurring)) {
      return recurringFrequency != null && !recurringFrequency.trim().isEmpty();
    }
    return true;
  }

  @AssertTrue(message = "Recurring end date must be after start date")
  private boolean isRecurringDateValid() {
    if (Boolean.TRUE.equals(isRecurring) && recurringEndDate != null && paymentDate != null) {
      return recurringEndDate.isAfter(paymentDate);
    }
    return true;
  }

  // Convenience methods
  public boolean isLoanPayment() {
    return paymentType != null && paymentType.isLoanRelated();
  }

  public boolean isSavingsPayment() {
    return paymentType != null && paymentType.isSavingsRelated();
  }

  public boolean isFeePayment() {
    return paymentType != null && paymentType.isFee();
  }

  // Manual getters for Lombok compatibility
  public Long getMemberId() {
    return memberId;
  }

  public Long getLoanId() {
    return loanId;
  }

  public Long getSavingAccountId() {
    return savingAccountId;
  }

  public PaymentType getPaymentType() {
    return paymentType;
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

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public String getTransactionId() {
    return transactionId;
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

  // Validation groups
  public interface LoanPaymentValidation {
  }

  public interface SavingsPaymentValidation {
  }

  public interface RecurringPaymentValidation {
  }
}
