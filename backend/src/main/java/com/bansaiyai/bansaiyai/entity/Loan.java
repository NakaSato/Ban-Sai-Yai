package com.bansaiyai.bansaiyai.entity;

import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Loan entity representing member loan applications and contracts.
 * Contains loan details, terms, collateral, and payment information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Entity
@Table(name = "loan", indexes = {
    @Index(name = "idx_loan_number", columnList = "loanNumber"),
    @Index(name = "idx_loan_member_id", columnList = "memberId"),
    @Index(name = "idx_loan_status", columnList = "status"),
    @Index(name = "idx_loan_start_date", columnList = "startDate"),
    @Index(name = "idx_loan_uuid", columnList = "uuid")
})
@EntityListeners(AuditingEntityListener.class)
public class Loan extends BaseEntity {

  /**
   * UUID for external API use - prevents ID enumeration attacks
   * This is the primary identifier exposed in public APIs
   */
  @Column(name = "uuid", nullable = false, unique = true, columnDefinition = "BINARY(16)")
  private UUID uuid;

  @Column(name = "loan_number", unique = true, nullable = false, length = 50)
  @NotBlank(message = "Loan number is required")
  @Size(min = 5, max = 50, message = "Loan number must be between 5 and 50 characters")
  private String loanNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  @NotNull(message = "Member is required")
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(name = "loan_type", nullable = false, length = 20)
  @NotNull(message = "Loan type is required")
  private LoanType loanType;

  @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Principal amount is required")
  @DecimalMin(value = "1000.00", message = "Minimum principal amount is THB 1,000")
  @DecimalMax(value = "5000000.00", message = "Maximum principal amount is THB 5,000,000")
  private BigDecimal principalAmount;

  @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
  @NotNull(message = "Interest rate is required")
  @DecimalMin(value = "0.01", message = "Minimum interest rate is 0.01%")
  @DecimalMax(value = "36.00", message = "Maximum interest rate is 36%")
  private BigDecimal interestRate;

  @Column(name = "term_months", nullable = false)
  @NotNull(message = "Loan term is required")
  @Min(value = 1, message = "Minimum loan term is 1 month")
  @Max(value = 120, message = "Maximum loan term is 120 months")
  private Integer termMonths;

  @Column(name = "start_date", nullable = false)
  @NotNull(message = "Start date is required")
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  @NotNull(message = "End date is required")
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @NotNull(message = "Status is required")
  private LoanStatus status;

  @Column(name = "purpose", length = 500)
  @Size(max = 500, message = "Purpose must not exceed 500 characters")
  private String purpose;

  @Column(name = "approved_amount", precision = 15, scale = 2)
  private BigDecimal approvedAmount;

  @Column(name = "disbursement_date")
  private LocalDate disbursementDate;

  @Column(name = "maturity_date")
  private LocalDate maturityDate;

  @Column(name = "outstanding_balance", precision = 15, scale = 2)
  private BigDecimal outstandingBalance;

  @Column(name = "paid_principal", precision = 15, scale = 2)
  private BigDecimal paidPrincipal;

  @Column(name = "paid_interest", precision = 15, scale = 2)
  private BigDecimal paidInterest;

  @Column(name = "penalty_amount", precision = 15, scale = 2)
  private BigDecimal penaltyAmount;

  @Column(name = "collateral_value", precision = 15, scale = 2)
  private BigDecimal collateralValue;

  @Column(name = "guarantee_amount", precision = 15, scale = 2)
  private BigDecimal guaranteeAmount;

  @Column(name = "approval_notes", length = 1000)
  private String approvalNotes;

  @Column(name = "rejection_reason", length = 500)
  private String rejectionReason;

  @Column(name = "approved_by")
  private String approvedBy;

  @Column(name = "approved_date")
  private LocalDate approvedDate;

  @Column(name = "disbursed_by")
  private String disbursedBy;

  @Column(name = "disbursement_reference", length = 100)
  private String disbursementReference;

  @Column(name = "contract_document_path")
  private String contractDocumentPath;

  @Column(name = "approval_document_path")
  private String approvalDocumentPath;

  // Relationships
  @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Collateral> collaterals;

  @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Guarantor> guarantors;

  @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Payment> payments;

  @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<LoanBalance> loanBalances;

  // Business logic methods
  /**
   * Calculate monthly installment using reducing balance formula
   */
  public BigDecimal calculateMonthlyInstallment() {
    if (principalAmount == null || interestRate == null || termMonths == null) {
      return BigDecimal.ZERO;
    }

    // Convert annual rate to monthly rate
    BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(1200), 8, RoundingMode.HALF_UP);

    // Calculate installment using formula: P * r / (1 - (1 + r)^-n)
    BigDecimal denominator = BigDecimal.ONE.subtract(
        BigDecimal.ONE.add(monthlyRate).pow(-termMonths));

    return principalAmount.multiply(monthlyRate).divide(denominator, 2, RoundingMode.HALF_UP);
  }

  /**
   * Calculate total interest payable over loan term
   */
  public BigDecimal calculateTotalInterest() {
    BigDecimal monthlyInstallment = calculateMonthlyInstallment();
    return monthlyInstallment.multiply(BigDecimal.valueOf(termMonths)).subtract(principalAmount);
  }

  /**
   * Calculate total payment amount (principal + interest)
   */
  public BigDecimal calculateTotalPayment() {
    return principalAmount.add(calculateTotalInterest());
  }

  /**
   * Check if loan is overdue
   */
  public boolean isOverdue() {
    if (status != LoanStatus.ACTIVE || maturityDate == null) {
      return false;
    }
    return LocalDate.now().isAfter(maturityDate) && getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Get days overdue
   */
  public Long getDaysOverdue() {
    if (!isOverdue()) {
      return 0L;
    }
    return java.time.temporal.ChronoUnit.DAYS.between(maturityDate, LocalDate.now());
  }

  /**
   * Calculate penalty for overdue payment
   */
  public BigDecimal calculatePenalty() {
    if (!isOverdue()) {
      return BigDecimal.ZERO;
    }

    Long daysOverdue = getDaysOverdue();
    BigDecimal outstandingBalance = getOutstandingBalance();

    // Penalty rate: 1% per month on outstanding balance
    BigDecimal monthlyPenaltyRate = new BigDecimal("0.01");
    BigDecimal dailyRate = monthlyPenaltyRate.divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP);

    return outstandingBalance.multiply(dailyRate).multiply(BigDecimal.valueOf(daysOverdue))
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Get current outstanding balance
   */
  public BigDecimal getOutstandingBalance() {
    if (outstandingBalance != null) {
      return outstandingBalance;
    }
    return principalAmount.subtract(paidPrincipal != null ? paidPrincipal : BigDecimal.ZERO);
  }

  /**
   * Get total amount paid so far
   */
  public BigDecimal getTotalPaid() {
    return (paidPrincipal != null ? paidPrincipal : BigDecimal.ZERO)
        .add(paidInterest != null ? paidInterest : BigDecimal.ZERO)
        .add(penaltyAmount != null ? penaltyAmount : BigDecimal.ZERO);
  }

  /**
   * Check if loan can be approved
   */
  public boolean canBeApproved() {
    return status == LoanStatus.PENDING &&
        member != null &&
        member.isEligibleForLoan() &&
        loanType != null &&
        principalAmount != null &&
        principalAmount.compareTo(loanType.getMaximumAmount()) <= 0 &&
        termMonths != null &&
        termMonths <= loanType.getMaximumTerm();
  }

  @PrePersist
  protected void onCreate() {
    // Auto-generate UUID for security
    if (uuid == null) {
      uuid = UUID.randomUUID();
    }
    if (loanNumber == null || loanNumber.trim().isEmpty()) {
      loanNumber = generateLoanNumber();
    }
    if (status == null) {
      status = LoanStatus.PENDING;
    }
    if (outstandingBalance == null) {
      outstandingBalance = principalAmount;
    }
    if (paidPrincipal == null) {
      paidPrincipal = BigDecimal.ZERO;
    }
    if (paidInterest == null) {
      paidInterest = BigDecimal.ZERO;
    }
    if (penaltyAmount == null) {
      penaltyAmount = BigDecimal.ZERO;
    }
    // Calculate maturity date if not set
    if (maturityDate == null && startDate != null && termMonths != null) {
      maturityDate = startDate.plusMonths(termMonths);
    }
  }

  @PreUpdate
  protected void onUpdate() {
    // Update outstanding balance when payments are made
    if (paidPrincipal != null) {
      outstandingBalance = principalAmount.subtract(paidPrincipal);
    }
  }

  // Additional getters and setters for Lombok compatibility
  public void setId(Long id) {
    super.setId(id);
  }

  public String getLoanNumber() {
    return loanNumber;
  }

  public Member getMember() {
    return member;
  }

  public LoanType getLoanType() {
    return loanType;
  }

  public BigDecimal getPrincipalAmount() {
    return principalAmount;
  }

  public BigDecimal getInterestRate() {
    return interestRate;
  }

  public Integer getTermMonths() {
    return termMonths;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public LoanStatus getStatus() {
    return status;
  }

  public String getPurpose() {
    return purpose;
  }

  public BigDecimal getApprovedAmount() {
    return approvedAmount;
  }

  public LocalDate getDisbursementDate() {
    return disbursementDate;
  }

  public LocalDate getMaturityDate() {
    return maturityDate;
  }

  public BigDecimal getPaidPrincipal() {
    return paidPrincipal;
  }

  public BigDecimal getPaidInterest() {
    return paidInterest;
  }

  public BigDecimal getPenaltyAmount() {
    return penaltyAmount;
  }

  public BigDecimal getCollateralValue() {
    return collateralValue;
  }

  public BigDecimal getGuaranteeAmount() {
    return guaranteeAmount;
  }

  public String getApprovalNotes() {
    return approvalNotes;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public String getApprovedBy() {
    return approvedBy;
  }

  public LocalDate getApprovedDate() {
    return approvedDate;
  }

  public String getDisbursedBy() {
    return disbursedBy;
  }

  public String getDisbursementReference() {
    return disbursementReference;
  }

  public LocalDateTime getCreatedAt() {
    return super.getCreatedAt();
  }

  public LocalDateTime getUpdatedAt() {
    return super.getUpdatedAt();
  }

  public String getCreatedBy() {
    return super.getCreatedBy();
  }

  public String getUpdatedBy() {
    return super.getUpdatedBy();
  }

  // Setters
  public void setLoanType(LoanType loanType) {
    this.loanType = loanType;
  }

  public void setPrincipalAmount(BigDecimal principalAmount) {
    this.principalAmount = principalAmount;
  }

  public void setInterestRate(BigDecimal interestRate) {
    this.interestRate = interestRate;
  }

  public void setTermMonths(Integer termMonths) {
    this.termMonths = termMonths;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public void setStatus(LoanStatus status) {
    this.status = status;
  }

  public void setMaturityDate(LocalDate maturityDate) {
    this.maturityDate = maturityDate;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public void setApprovedAmount(BigDecimal approvedAmount) {
    this.approvedAmount = approvedAmount;
  }

  public void setApprovedDate(LocalDate approvedDate) {
    this.approvedDate = approvedDate;
  }

  public void setApprovalNotes(String approvalNotes) {
    this.approvalNotes = approvalNotes;
  }

  public void setApprovedBy(String approvedBy) {
    this.approvedBy = approvedBy;
  }

  public void setDisbursementDate(LocalDate disbursementDate) {
    this.disbursementDate = disbursementDate;
  }

  public void setDisbursedBy(String disbursedBy) {
    this.disbursedBy = disbursedBy;
  }

  public void setDisbursementReference(String disbursementReference) {
    this.disbursementReference = disbursementReference;
  }

  public void setOutstandingBalance(BigDecimal outstandingBalance) {
    this.outstandingBalance = outstandingBalance;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public void setCollateralValue(BigDecimal collateralValue) {
    this.collateralValue = collateralValue;
  }

  public void setUpdatedBy(String updatedBy) {
    super.setUpdatedBy(updatedBy);
  }

  /**
   * Generate unique loan number with format: LN-YYYYMMDD-XXXX
   */
  private String generateLoanNumber() {
    String date = LocalDate.now().toString().replace("-", "");
    String random = String.format("%04d", (int) (Math.random() * 10000));
    return "LN-" + date + "-" + random;
  }

  // Manual builder for Lombok compatibility
  public static LoanBuilder builder() {
    return new LoanBuilder();
  }

  public static class LoanBuilder {
    private UUID uuid;
    private String loanNumber;
    private Member member;
    private LoanType loanType;
    private java.math.BigDecimal principalAmount;
    private java.math.BigDecimal interestRate;
    private Integer termMonths;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private LoanStatus status;
    private String purpose;
    private java.math.BigDecimal approvedAmount;
    private java.time.LocalDate disbursementDate;
    private java.time.LocalDate maturityDate;
    private java.math.BigDecimal outstandingBalance;
    private java.math.BigDecimal paidPrincipal;
    private java.math.BigDecimal paidInterest;
    private java.math.BigDecimal penaltyAmount;
    private java.math.BigDecimal collateralValue;
    private java.math.BigDecimal guaranteeAmount;
    private String approvalNotes;
    private String rejectionReason;
    private String approvedBy;
    private java.time.LocalDate approvedDate;
    private String disbursedBy;
    private String disbursementReference;
    private String contractDocumentPath;
    private String approvalDocumentPath;
    private String createdBy;
    private String updatedBy;

    public LoanBuilder contractDocumentPath(String contractDocumentPath) {
      this.contractDocumentPath = contractDocumentPath;
      return this;
    }

    public LoanBuilder approvalDocumentPath(String approvalDocumentPath) {
      this.approvalDocumentPath = approvalDocumentPath;
      return this;
    }

    public LoanBuilder uuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public LoanBuilder loanNumber(String loanNumber) {
      this.loanNumber = loanNumber;
      return this;
    }

    public LoanBuilder member(Member member) {
      this.member = member;
      return this;
    }

    public LoanBuilder loanType(LoanType loanType) {
      this.loanType = loanType;
      return this;
    }

    public LoanBuilder principalAmount(java.math.BigDecimal principalAmount) {
      this.principalAmount = principalAmount;
      return this;
    }

    public LoanBuilder interestRate(java.math.BigDecimal interestRate) {
      this.interestRate = interestRate;
      return this;
    }

    public LoanBuilder termMonths(Integer termMonths) {
      this.termMonths = termMonths;
      return this;
    }

    public LoanBuilder startDate(java.time.LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    public LoanBuilder endDate(java.time.LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    public LoanBuilder status(LoanStatus status) {
      this.status = status;
      return this;
    }

    public LoanBuilder purpose(String purpose) {
      this.purpose = purpose;
      return this;
    }

    public LoanBuilder approvedAmount(java.math.BigDecimal approvedAmount) {
      this.approvedAmount = approvedAmount;
      return this;
    }

    public LoanBuilder disbursementDate(java.time.LocalDate disbursementDate) {
      this.disbursementDate = disbursementDate;
      return this;
    }

    public LoanBuilder maturityDate(java.time.LocalDate maturityDate) {
      this.maturityDate = maturityDate;
      return this;
    }

    public LoanBuilder outstandingBalance(java.math.BigDecimal outstandingBalance) {
      this.outstandingBalance = outstandingBalance;
      return this;
    }

    public LoanBuilder paidPrincipal(java.math.BigDecimal paidPrincipal) {
      this.paidPrincipal = paidPrincipal;
      return this;
    }

    public LoanBuilder paidInterest(java.math.BigDecimal paidInterest) {
      this.paidInterest = paidInterest;
      return this;
    }

    public LoanBuilder penaltyAmount(java.math.BigDecimal penaltyAmount) {
      this.penaltyAmount = penaltyAmount;
      return this;
    }

    public LoanBuilder collateralValue(java.math.BigDecimal collateralValue) {
      this.collateralValue = collateralValue;
      return this;
    }

    public LoanBuilder guaranteeAmount(java.math.BigDecimal guaranteeAmount) {
      this.guaranteeAmount = guaranteeAmount;
      return this;
    }

    public LoanBuilder approvalNotes(String approvalNotes) {
      this.approvalNotes = approvalNotes;
      return this;
    }

    public LoanBuilder rejectionReason(String rejectionReason) {
      this.rejectionReason = rejectionReason;
      return this;
    }

    public LoanBuilder approvedBy(String approvedBy) {
      this.approvedBy = approvedBy;
      return this;
    }

    public LoanBuilder approvedDate(java.time.LocalDate approvedDate) {
      this.approvedDate = approvedDate;
      return this;
    }

    public LoanBuilder disbursedBy(String disbursedBy) {
      this.disbursedBy = disbursedBy;
      return this;
    }

    public LoanBuilder disbursementReference(String disbursementReference) {
      this.disbursementReference = disbursementReference;
      return this;
    }

    public LoanBuilder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public LoanBuilder updatedBy(String updatedBy) {
      this.updatedBy = updatedBy;
      return this;
    }

    public LoanBuilder id(Long id) {
      // Note: ID is typically set by JPA, but added for testing
      return this;
    }

    public LoanBuilder createdAt(java.time.LocalDateTime createdAt) {
      // Note: createdAt is typically set by JPA, but added for testing
      return this;
    }

    public Loan build() {
      Loan loan = new Loan();
      loan.uuid = this.uuid;
      loan.loanNumber = this.loanNumber;
      loan.member = this.member;
      loan.loanType = this.loanType;
      loan.principalAmount = this.principalAmount;
      loan.interestRate = this.interestRate;
      loan.termMonths = this.termMonths;
      loan.startDate = this.startDate;
      loan.endDate = this.endDate;
      loan.status = this.status;
      loan.purpose = this.purpose;
      loan.approvedAmount = this.approvedAmount;
      loan.disbursementDate = this.disbursementDate;
      loan.maturityDate = this.maturityDate;
      loan.outstandingBalance = this.outstandingBalance;
      loan.paidPrincipal = this.paidPrincipal;
      loan.paidInterest = this.paidInterest;
      loan.penaltyAmount = this.penaltyAmount;
      loan.collateralValue = this.collateralValue;
      loan.guaranteeAmount = this.guaranteeAmount;
      loan.approvalNotes = this.approvalNotes;
      loan.rejectionReason = this.rejectionReason;
      loan.approvedBy = this.approvedBy;
      loan.approvedDate = this.approvedDate;
      loan.disbursedBy = this.disbursedBy;
      loan.disbursementReference = this.disbursementReference;
      loan.contractDocumentPath = this.contractDocumentPath;
      loan.approvalDocumentPath = this.approvalDocumentPath;
      loan.setCreatedBy(this.createdBy);
      loan.setUpdatedBy(this.updatedBy);
      return loan;
    }
  }

  // Helper methods
  public boolean isActive() {
    return LoanStatus.ACTIVE.equals(this.status);
  }

  public java.math.BigDecimal calculateAccruedInterest(LocalDate paymentDate) {
    if (outstandingBalance == null || outstandingBalance.compareTo(java.math.BigDecimal.ZERO) == 0) {
      return java.math.BigDecimal.ZERO;
    }
    if (interestRate == null) {
      return java.math.BigDecimal.ZERO;
    }

    // Simple daily interest calculation
    // Interest = Outstanding * (Rate/100) * (Days/365)
    java.math.BigDecimal annualRate = interestRate.divide(new java.math.BigDecimal("100"), 4,
        java.math.RoundingMode.HALF_UP);
    java.math.BigDecimal dailyRate = annualRate.divide(new java.math.BigDecimal("365"), 8,
        java.math.RoundingMode.HALF_UP);

    LocalDate fromDate = startDate;
    if (fromDate == null)
      fromDate = LocalDate.now().minusDays(30); // Fallback

    long days = java.time.temporal.ChronoUnit.DAYS.between(fromDate, paymentDate);
    if (days <= 0)
      return java.math.BigDecimal.ZERO;

    return outstandingBalance.multiply(dailyRate)
        .multiply(new java.math.BigDecimal(days))
        .setScale(2, java.math.RoundingMode.HALF_UP);
  }
}
