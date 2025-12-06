package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Saving balance entity for tracking daily account balances.
 * Maintains historical record of account balances for interest calculation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Entity
@Table(name = "saving_balance", indexes = {
    @Index(name = "idx_account_date", columnList = "savingAccountId,balanceDate"),
    @Index(name = "idx_balance_date", columnList = "balanceDate")
})
public class SavingBalance extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "saving_account_id", nullable = false)
  @NotNull(message = "Saving account is required")
  private SavingAccount savingAccount;

  @Column(name = "balance_date", nullable = false)
  @NotNull(message = "Balance date is required")
  private LocalDate balanceDate;

  @Column(name = "opening_balance", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Opening balance is required")
  @DecimalMin(value = "0.00", message = "Opening balance cannot be negative")
  private BigDecimal openingBalance;

  @Column(name = "closing_balance", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Closing balance is required")
  @DecimalMin(value = "0.00", message = "Closing balance cannot be negative")
  private BigDecimal closingBalance;

  @Column(name = "total_deposits", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Total deposits is required")
  @DecimalMin(value = "0.00", message = "Total deposits cannot be negative")
  private BigDecimal totalDeposits;

  @Column(name = "total_withdrawals", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Total withdrawals is required")
  @DecimalMin(value = "0.00", message = "Total withdrawals cannot be negative")
  private BigDecimal totalWithdrawals;

  @Column(name = "interest_earned", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Interest earned is required")
  @DecimalMin(value = "0.00", message = "Interest earned cannot be negative")
  private BigDecimal interestEarned;

  @Column(name = "fees_charged", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Fees charged is required")
  @DecimalMin(value = "0.00", message = "Fees charged cannot be negative")
  private BigDecimal feesCharged;

  @Column(name = "minimum_balance", precision = 15, scale = 2)
  @DecimalMin(value = "0.00", message = "Minimum balance cannot be negative")
  private BigDecimal minimumBalance;

  @Column(name = "average_balance", precision = 15, scale = 2)
  @DecimalMin(value = "0.00", message = "Average balance cannot be negative")
  private BigDecimal averageBalance;

  @Column(name = "days_below_minimum", nullable = false)
  @Min(value = 0, message = "Days below minimum cannot be negative")
  private Integer daysBelowMinimum;

  @Column(name = "interest_rate", precision = 5, scale = 2)
  @DecimalMin(value = "0.00", message = "Interest rate cannot be negative")
  private BigDecimal interestRate;

  @Builder.Default
  @Column(name = "is_month_end", nullable = false)
  private Boolean isMonthEnd = false;

  // Business logic methods
  /**
   * Calculate net change for the period
   */
  public BigDecimal getNetChange() {
    return totalDeposits.subtract(totalWithdrawals)
        .add(interestEarned)
        .subtract(feesCharged);
  }

  /**
   * Check if balance met minimum requirements
   */
  public boolean metMinimumBalanceRequirement() {
    if (minimumBalance == null) {
      return true;
    }
    return averageBalance != null && averageBalance.compareTo(minimumBalance) >= 0;
  }

  /**
   * Calculate effective days for interest calculation
   */
  public Integer getEffectiveDays() {
    if (daysBelowMinimum == null || daysBelowMinimum == 0) {
      return 1; // Assume 1 day if not specified
    }
    return Math.max(1, 30 - daysBelowMinimum);
  }

  /**
   * Calculate daily average balance
   */
  public BigDecimal calculateDailyAverage() {
    if (averageBalance != null) {
      return averageBalance;
    }
    // Simple calculation: (Opening + Closing) / 2
    return openingBalance.add(closingBalance)
        .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
  }

  /**
   * Get period description
   */
  public String getPeriodDescription() {
    if (isMonthEnd != null && isMonthEnd) {
      return "Month End - " + balanceDate.getMonth() + " " + balanceDate.getYear();
    }
    return "Daily - " + balanceDate;
  }

  @PrePersist
  protected void onCreate() {
    if (balanceDate == null) {
      balanceDate = LocalDate.now();
    }
    if (daysBelowMinimum == null) {
      daysBelowMinimum = 0;
    }
    if (isMonthEnd == null) {
      isMonthEnd = false;
    }
    if (averageBalance == null) {
      averageBalance = calculateDailyAverage();
    }
  }
}
