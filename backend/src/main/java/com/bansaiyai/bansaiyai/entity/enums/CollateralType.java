package com.bansaiyai.bansaiyai.entity.enums;

import java.math.BigDecimal;

/**
 * Enum representing different types of collateral accepted for loans.
 * Includes real estate, vehicles, and other valuable assets.
 */
public enum CollateralType {
  REAL_ESTATE("Real Estate", "Land, buildings, and property"),
  VEHICLE("Vehicle", "Cars, motorcycles, and other vehicles"),
  JEWELRY("Jewelry", "Gold, diamonds, and precious items"),
  ELECTRONICS("Electronics", "Computers, phones, and appliances"),
  DEPOSIT_CERTIFICATE("Deposit Certificate", "Bank deposits and certificates"),
  INVESTMENT("Investment", "Stocks, bonds, and mutual funds"),
  MACHINERY("Machinery", "Business equipment and machinery"),
  LIVESTOCK("Livestock", "Animals and agricultural assets"),
  OTHER("Other", "Other types of collateral");

  private final String displayName;
  private final String description;

  CollateralType(String displayName, String description) {
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
   * Get default loan-to-value ratio for this collateral type
   */
  public BigDecimal getDefaultLoanToValueRatio() {
    switch (this) {
      case REAL_ESTATE:
        return new BigDecimal("0.80"); // 80%
      case VEHICLE:
        return new BigDecimal("0.60"); // 60%
      case JEWELRY:
        return new BigDecimal("0.70"); // 70%
      case ELECTRONICS:
        return new BigDecimal("0.30"); // 30%
      case DEPOSIT_CERTIFICATE:
        return new BigDecimal("0.95"); // 95%
      case INVESTMENT:
        return new BigDecimal("0.50"); // 50%
      case MACHINERY:
        return new BigDecimal("0.40"); // 40%
      case LIVESTOCK:
        return new BigDecimal("0.50"); // 50%
      case OTHER:
        return new BigDecimal("0.50"); // 50%
      default:
        return new BigDecimal("0.70"); // Default 70%
    }
  }

  /**
   * Check if collateral type requires professional appraisal
   */
  public boolean requiresProfessionalAppraisal() {
    return this == REAL_ESTATE ||
        this == JEWELRY ||
        this == MACHINERY ||
        this == LIVESTOCK;
  }

  /**
   * Check if collateral type requires ownership documents
   */
  public boolean requiresOwnershipDocuments() {
    return this == REAL_ESTATE ||
        this == VEHICLE ||
        this == MACHINERY;
  }

  /**
   * Check if collateral type depreciates over time
   */
  public boolean isDepreciating() {
    return this == VEHICLE ||
        this == ELECTRONICS ||
        this == MACHINERY;
  }

  /**
   * Get maximum age for this collateral type (in years)
   */
  public Integer getMaximumAge() {
    switch (this) {
      case VEHICLE:
        return 10; // 10 years max for vehicles
      case ELECTRONICS:
        return 5; // 5 years max for electronics
      case MACHINERY:
        return 15; // 15 years max for machinery
      default:
        return null; // No age limit for others
    }
  }

  /**
   * Check if this collateral type is suitable for given loan amount
   */
  public boolean isSuitableForLoanAmount(BigDecimal loanAmount) {
    switch (this) {
      case REAL_ESTATE:
        return loanAmount.compareTo(new BigDecimal("100000")) >= 0; // Suitable for large loans
      case VEHICLE:
        return loanAmount.compareTo(new BigDecimal("50000")) >= 0 &&
            loanAmount.compareTo(new BigDecimal("500000")) <= 0;
      case ELECTRONICS:
        return loanAmount.compareTo(new BigDecimal("10000")) <= 0; // Only small loans
      default:
        return true; // No specific restrictions
    }
  }
}
