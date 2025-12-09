package com.bansaiyai.bansaiyai.entity.enums;

import java.math.BigDecimal;

public enum LoanType {
  BUSINESS(5000000.00, 120, "Business Loan"),
  EDUCATION(1000000.00, 60, "Education Loan"),
  EMERGENCY(500000.00, 24, "Emergency Loan"),
  HOUSING(3000000.00, 180, "Housing Loan"),
  PERSONAL(1000000.00, 36, "Personal Loan");

  private final BigDecimal maximumAmount;
  private final Integer maximumTerm;
  private final String displayName;

  LoanType(double maximumAmount, int maximumTerm, String displayName) {
    this.maximumAmount = BigDecimal.valueOf(maximumAmount);
    this.maximumTerm = maximumTerm;
    this.displayName = displayName;
  }

  public BigDecimal getMaximumAmount() {
    return maximumAmount;
  }

  public Integer getMaximumTerm() {
    return maximumTerm;
  }

  public String getDisplayName() {
    return displayName;
  }
}
