package com.bansaiyai.bansaiyai.entity.enums;

/**
 * Enumeration for different types of savings accounts.
 * Defines the account types available to cooperative members.
 */
public enum AccountType {

  /**
   * Regular savings account with standard interest rate
   */
  SAVINGS("Regular Savings", "Standard savings account with competitive interest rate"),

  /**
   * Fixed deposit account with higher interest rate
   */
  FIXED_DEPOSIT("Fixed Deposit", "Term deposit with guaranteed higher returns"),

  /**
   * Junior savings account for minors
   */
  JUNIOR("Junior Savings", "Savings account for members under 18 years"),

  /**
   * Senior citizen account with special benefits
   */
  SENIOR("Senior Savings", "Special account for members 60 years and above"),

  /**
   * Business savings account
   */
  BUSINESS("Business Savings", "Savings account tailored for business members"),

  /**
   * Education savings account
   */
  EDUCATION("Education Savings", "Dedicated savings for educational expenses"),

  /**
   * Emergency fund account
   */
  EMERGENCY("Emergency Fund", "High-liquidity emergency savings account");

  private final String displayName;
  private final String description;

  AccountType(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }
}
