package com.bansaiyai.bansaiyai.entity.enums;

/**
 * Enumeration for different types of transactions.
 * Defines transaction categories for savings and loan accounts.
 */
public enum TransactionType {

  /**
   * Cash or check deposit into account
   */
  DEPOSIT("Deposit", "Money added to account"),

  /**
   * Cash withdrawal from account
   */
  WITHDRAWAL("Withdrawal", "Money taken from account"),

  /**
   * Transfer from another account
   */
  TRANSFER_IN("Transfer In", "Money received from another account"),

  /**
   * Transfer to another account
   */
  TRANSFER_OUT("Transfer Out", "Money sent to another account"),

  /**
   * Interest credited to account
   */
  INTEREST_CREDIT("Interest", "Interest earned on savings"),

  /**
   * Bonus or dividend credit
   */
  BONUS_CREDIT("Bonus", "Bonus or dividend payment"),

  /**
   * Fee charged to account
   */
  FEE_DEDUCTION("Fee", "Service or maintenance fee"),

  /**
   * Tax deduction from account
   */
  TAX_DEDUCTION("Tax", "Tax withholding"),

  /**
   * Refund of previous transaction
   */
  REFUND("Refund", "Refund of charges or fees"),

  /**
   * Loan disbursement to account
   */
  LOAN_DISBURSEMENT("Loan Disbursement", "Loan amount credited to account"),

  /**
   * Loan repayment from account
   */
  LOAN_REPAYMENT("Loan Repayment", "Loan payment deducted from account"),

  /**
   * Penalty charge
   */
  PENALTY_CHARGE("Penalty", "Penalty for rule violation"),

  /**
   * Account opening credit
   */
  OPENING_BALANCE("Opening Balance", "Initial account balance"),

  /**
   * Account closing debit
   */
  CLOSING_WITHDRAWAL("Closing Withdrawal", "Final withdrawal during account closure"),

  /**
   * Currency conversion transaction
   */
  CURRENCY_CONVERSION("Currency Conversion", "Exchange between different currencies"),

  /**
   * Cheque deposit
   */
  CHEQUE_DEPOSIT("Cheque Deposit", "Cheque clearing and credit"),

  /**
   * Cheque withdrawal
   */
  CHEQUE_WITHDRAWAL("Cheque Withdrawal", "Cheque issued against account"),

  /**
   * Electronic funds transfer
   */
  ELECTRONIC_TRANSFER("Electronic Transfer", "Online or mobile banking transfer"),

  /**
   * Standing order payment
   */
  STANDING_ORDER("Standing Order", "Recurring scheduled payment"),

  /**
   * Direct debit instruction
   */
  DIRECT_DEBIT("Direct Debit", "Authorized automatic debit"),
  DIVIDEND_PAYOUT("Dividend Payout", "Annual dividend distribution");

  private final String displayName;
  private final String description;

  TransactionType(String displayName, String description) {
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
   * Check if transaction type increases account balance
   */
  public boolean isCreditType() {
    return this == DEPOSIT ||
        this == TRANSFER_IN ||
        this == INTEREST_CREDIT ||
        this == BONUS_CREDIT ||
        this == REFUND ||
        this == LOAN_DISBURSEMENT ||
        this == OPENING_BALANCE ||
        this == CHEQUE_DEPOSIT;
  }

  /**
   * Check if transaction type decreases account balance
   */
  public boolean isDebitType() {
    return this == WITHDRAWAL ||
        this == TRANSFER_OUT ||
        this == FEE_DEDUCTION ||
        this == TAX_DEDUCTION ||
        this == LOAN_REPAYMENT ||
        this == PENALTY_CHARGE ||
        this == CLOSING_WITHDRAWAL ||
        this == CHEQUE_WITHDRAWAL ||
        this == STANDING_ORDER ||
        this == DIRECT_DEBIT;
  }

  /**
   * Check if transaction can be reversed
   */
  public boolean isReversible() {
    return this == DEPOSIT ||
        this == WITHDRAWAL ||
        this == TRANSFER_IN ||
        this == TRANSFER_OUT ||
        this == CHEQUE_DEPOSIT ||
        this == CHEQUE_WITHDRAWAL ||
        this == ELECTRONIC_TRANSFER;
  }
}
