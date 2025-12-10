package com.bansaiyai.bansaiyai.entity.enums;

/**
 * Enumeration for different types of payments in the cooperative banking
 * system.
 * Categorizes all monetary transactions for proper accounting and reporting.
 */
public enum PaymentType {

  LOAN_PRINCIPAL("Loan Principal Payment", true),
  LOAN_INTEREST("Loan Interest Payment", true),
  LOAN_PENALTY("Loan Penalty Payment", true),
  LOAN_REPAYMENT("Loan Repayment", true),
  LOAN_CLOSURE("Loan Closure", true),
  LOAN_PAYMENT("Loan Payment", true),
  LOAN_FEE("Loan Fee", true),
  SAVINGS_DEPOSIT("Savings Deposit", false),
  SHARE_CAPITAL("Share Capital Contribution", false),
  MEMBERSHIP_FEE("Membership Fee", false),
  PROCESSING_FEE("Processing Fee", false),
  LATE_FEE("Late Payment Fee", true),
  INSURANCE_PREMIUM("Insurance Premium", false),
  SERVICE_CHARGE("Service Charge", false),
  REFUND("Refund", false),
  TRANSFER_IN("Transfer In", false),
  TRANSFER_OUT("Transfer Out", false),
  ADJUSTMENT("Account Adjustment", false),
  REVERSAL("Payment Reversal", false),
  WRITE_OFF("Write Off", true),
  DIVIDEND_PAYOUT("Dividend Payout", false);

  private final String displayName;
  private final boolean isDebit;

  PaymentType(String displayName, boolean isDebit) {
    this.displayName = displayName;
    this.isDebit = isDebit;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isDebit() {
    return isDebit;
  }

  public boolean isCredit() {
    return !isDebit;
  }

  /**
   * Check if payment type is related to loans
   */
  public boolean isLoanRelated() {
    return this == LOAN_PRINCIPAL || this == LOAN_INTEREST ||
        this == LOAN_PENALTY || this == LATE_FEE;
  }

  /**
   * Check if payment type is related to savings
   */
  public boolean isSavingsRelated() {
    return this == SAVINGS_DEPOSIT || this == TRANSFER_IN || this == TRANSFER_OUT;
  }

  /**
   * Check if payment type is a fee
   */
  public boolean isFee() {
    return this == MEMBERSHIP_FEE || this == PROCESSING_FEE ||
        this == LATE_FEE || this == SERVICE_CHARGE;
  }
}
