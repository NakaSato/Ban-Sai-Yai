package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LoanBalance entity representing monthly loan balance snapshots.
 * Tracks principal and interest balances over time for reporting and analysis.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Entity
@Table(name = "loan_balance", indexes = {
    @Index(name = "idx_loan_id", columnList = "loanId"),
    @Index(name = "idx_balance_date", columnList = "balanceDate"),
    @Index(name = "idx_forward_id", columnList = "forwardId")
})
@EntityListeners(AuditingEntityListener.class)
public class LoanBalance extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loan_id", nullable = false)
  @NotNull(message = "Loan is required")
  private Loan loan;

  @Column(name = "balance_date", nullable = false)
  @NotNull(message = "Balance date is required")
  private LocalDate balanceDate;

  @Column(name = "opening_principal", precision = 15, scale = 2)
  private BigDecimal openingPrincipal;

  @Column(name = "opening_interest", precision = 15, scale = 2)
  private BigDecimal openingInterest;

  @Column(name = "opening_penalty", precision = 15, scale = 2)
  private BigDecimal openingPenalty;

  @Column(name = "principal_paid", precision = 15, scale = 2)
  private BigDecimal principalPaid;

  @Column(name = "interest_paid", precision = 15, scale = 2)
  private BigDecimal interestPaid;

  @Column(name = "penalty_paid", precision = 15, scale = 2)
  private BigDecimal penaltyPaid;

  @Column(name = "closing_principal", precision = 15, scale = 2)
  private BigDecimal closingPrincipal;

  @Column(name = "closing_interest", precision = 15, scale = 2)
  private BigDecimal closingInterest;

  @Column(name = "closing_penalty", precision = 15, scale = 2)
  private BigDecimal closingPenalty;

  @Column(name = "total_paid", precision = 15, scale = 2)
  private BigDecimal totalPaid;

  @Column(name = "outstanding_balance", precision = 15, scale = 2)
  private BigDecimal outstandingBalance;

  @Column(name = "days_in_arrears")
  private Integer daysInArrears;

  @Column(name = "payment_count")
  private Integer paymentCount;

  @Column(name = "average_payment", precision = 15, scale = 2)
  private BigDecimal averagePayment;

  @Column(name = "interest_accrued", precision = 15, scale = 2)
  private BigDecimal interestAccrued;

  @Column(name = "penalty_accrued", precision = 15, scale = 2)
  private BigDecimal penaltyAccrued;

  @Column(name = "is_current", nullable = false)
  @Builder.Default
  private Boolean isCurrent = false;

  @Column(name = "forward_id")
  private Long forwardId;

  @Column(name = "forward_date")
  private LocalDate forwardDate;

  @Column(name = "notes", length = 500)
  @Size(max = 500, message = "Notes must not exceed 500 characters")
  private String notes;

  // Business logic methods
  /**
   * Calculate total opening balance
   */
  public BigDecimal getTotalOpeningBalance() {
    BigDecimal total = openingPrincipal != null ? openingPrincipal : BigDecimal.ZERO;
    total = total.add(openingInterest != null ? openingInterest : BigDecimal.ZERO);
    total = total.add(openingPenalty != null ? openingPenalty : BigDecimal.ZERO);
    return total;
  }

  /**
   * Calculate total closing balance
   */
  public BigDecimal getTotalClosingBalance() {
    BigDecimal total = closingPrincipal != null ? closingPrincipal : BigDecimal.ZERO;
    total = total.add(closingInterest != null ? closingInterest : BigDecimal.ZERO);
    total = total.add(closingPenalty != null ? closingPenalty : BigDecimal.ZERO);
    return total;
  }

  /**
   * Calculate total payments made during period
   */
  public BigDecimal getTotalPayments() {
    BigDecimal total = principalPaid != null ? principalPaid : BigDecimal.ZERO;
    total = total.add(interestPaid != null ? interestPaid : BigDecimal.ZERO);
    total = total.add(penaltyPaid != null ? penaltyPaid : BigDecimal.ZERO);
    return total;
  }

  /**
   * Calculate total amounts accrued during period
   */
  public BigDecimal getTotalAccrued() {
    BigDecimal total = interestAccrued != null ? interestAccrued : BigDecimal.ZERO;
    total = total.add(penaltyAccrued != null ? penaltyAccrued : BigDecimal.ZERO);
    return total;
  }

  /**
   * Check if balance is overdue
   */
  public boolean isOverdue() {
    return daysInArrears != null && daysInArrears > 0;
  }

  /**
   * Calculate payment efficiency (actual vs expected payments)
   */
  public BigDecimal getPaymentEfficiency() {
    if (averagePayment == null || averagePayment.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    if (totalPaid == null || totalPaid.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return totalPaid.divide(averagePayment, 4, java.math.RoundingMode.HALF_UP)
        .multiply(new BigDecimal("100"))
        .setScale(2, java.math.RoundingMode.HALF_UP);
  }

  /**
   * Calculate arrears amount
   */
  public BigDecimal getArrearsAmount() {
    if (!isOverdue()) {
      return BigDecimal.ZERO;
    }

    BigDecimal expectedBalance = getExpectedBalance();
    if (outstandingBalance == null || expectedBalance == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal arrears = outstandingBalance.subtract(expectedBalance);
    return arrears.compareTo(BigDecimal.ZERO) > 0 ? arrears : BigDecimal.ZERO;
  }

  /**
   * Calculate expected balance based on loan schedule
   */
  private BigDecimal getExpectedBalance() {
    if (loan == null || loan.calculateTotalPayment() == null) {
      return BigDecimal.ZERO;
    }

    // This is a simplified calculation - in real implementation,
    // this would use the actual loan amortization schedule
    BigDecimal monthlyPayment = loan.calculateMonthlyInstallment();
    int monthsElapsed = getMonthsElapsed();

    if (monthsElapsed <= 0) {
      return loan.getPrincipalAmount();
    }

    BigDecimal expectedPaid = monthlyPayment.multiply(new BigDecimal(monthsElapsed));
    BigDecimal expectedBalance = loan.getPrincipalAmount().subtract(expectedPaid);

    return expectedBalance.compareTo(BigDecimal.ZERO) > 0 ? expectedBalance : BigDecimal.ZERO;
  }

  /**
   * Calculate months elapsed since loan start
   */
  private int getMonthsElapsed() {
    if (loan == null || loan.getStartDate() == null) {
      return 0;
    }

    if (balanceDate == null) {
      balanceDate = LocalDate.now();
    }

    return (int) java.time.temporal.ChronoUnit.MONTHS.between(
        loan.getStartDate(), balanceDate);
  }

  /**
   * Initialize balance for new month
   */
  public void initializeForMonth(LocalDate balanceDate, Loan loan) {
    this.loan = loan;
    this.balanceDate = balanceDate;
    this.isCurrent = balanceDate.equals(LocalDate.now().withDayOfMonth(1));

    // Set opening balances from previous closing or loan amount
    if (loan.getOutstandingBalance() != null) {
      this.openingPrincipal = loan.getOutstandingBalance();
    } else {
      this.openingPrincipal = loan.getPrincipalAmount();
    }

    this.openingInterest = BigDecimal.ZERO;
    this.openingPenalty = BigDecimal.ZERO;
    this.principalPaid = BigDecimal.ZERO;
    this.interestPaid = BigDecimal.ZERO;
    this.penaltyPaid = BigDecimal.ZERO;
    this.paymentCount = 0;
    this.interestAccrued = BigDecimal.ZERO;
    this.penaltyAccrued = BigDecimal.ZERO;
  }

  /**
   * Close balance for month
   */
  public void closeBalance() {
    // Calculate closing balances
    this.closingPrincipal = openingPrincipal.subtract(principalPaid != null ? principalPaid : BigDecimal.ZERO);
    this.closingInterest = (openingInterest != null ? openingInterest : BigDecimal.ZERO)
        .add(interestAccrued != null ? interestAccrued : BigDecimal.ZERO)
        .subtract(interestPaid != null ? interestPaid : BigDecimal.ZERO);
    this.closingPenalty = (openingPenalty != null ? openingPenalty : BigDecimal.ZERO)
        .add(penaltyAccrued != null ? penaltyAccrued : BigDecimal.ZERO)
        .subtract(penaltyPaid != null ? penaltyPaid : BigDecimal.ZERO);

    // Calculate outstanding balance
    this.outstandingBalance = closingPrincipal.add(closingInterest).add(closingPenalty);

    // Calculate total paid
    this.totalPaid = getTotalPayments();

    // Calculate average payment
    if (paymentCount != null && paymentCount > 0) {
      this.averagePayment = totalPaid.divide(new BigDecimal(paymentCount), 2, java.math.RoundingMode.HALF_UP);
    }

    // Set forward date for next month
    this.forwardDate = balanceDate.plusMonths(1);
  }

  @PrePersist
  protected void onCreate() {
    if (balanceDate == null) {
      balanceDate = LocalDate.now();
    }
    if (isCurrent == null) {
      isCurrent = false;
    }
  }
}
