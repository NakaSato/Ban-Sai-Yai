package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Guarantor entity representing loan guarantors.
 * Tracks member information for loan guarantee purposes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Entity
@Table(name = "guarantor", indexes = {
    @Index(name = "idx_guarantor_number", columnList = "guarantorNumber"),
    @Index(name = "idx_guarantor_loan_id", columnList = "loanId"),
    @Index(name = "idx_guarantor_member_id", columnList = "memberId")
})
@EntityListeners(AuditingEntityListener.class)
public class Guarantor extends BaseEntity {

  @Column(name = "guarantor_number", unique = true, nullable = false, length = 50)
  @NotBlank(message = "Guarantor number is required")
  @Size(min = 5, max = 50, message = "Guarantor number must be between 5 and 50 characters")
  private String guarantorNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loan_id", nullable = false)
  @NotNull(message = "Loan is required")
  private Loan loan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  @NotNull(message = "Member is required")
  private Member member;

  @Column(name = "guaranteed_amount", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Guaranteed amount is required")
  @DecimalMin(value = "1000.00", message = "Minimum guaranteed amount is THB 1,000")
  private BigDecimal guaranteedAmount;

  @Column(name = "guarantee_percentage", precision = 5, scale = 2)
  private BigDecimal guaranteePercentage;

  @Column(name = "relationship", length = 50)
  @Size(max = 50, message = "Relationship must not exceed 50 characters")
  private String relationship;

  @Column(name = "contact_phone", length = 20)
  @Pattern(regexp = "^[0-9]{9,10}$", message = "Phone number must be 9-10 digits")
  private String contactPhone;

  @Column(name = "contact_address", length = 300)
  @Size(max = 300, message = "Contact address must not exceed 300 characters")
  private String contactAddress;

  @Column(name = "occupation", length = 100)
  @Size(max = 100, message = "Occupation must not exceed 100 characters")
  private String occupation;

  @Column(name = "monthly_income", precision = 12, scale = 2)
  private BigDecimal monthlyIncome;

  @Column(name = "employer_name", length = 100)
  @Size(max = 100, message = "Employer name must not exceed 100 characters")
  private String employerName;

  @Column(name = "employer_address", length = 300)
  @Size(max = 300, message = "Employer address must not exceed 300 characters")
  private String employerAddress;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "guarantee_start_date")
  private LocalDate guaranteeStartDate;

  @Column(name = "guarantee_end_date")
  private LocalDate guaranteeEndDate;

  @Column(name = "guarantor_signature", length = 500)
  @Size(max = 500, message = "Guarantor signature path must not exceed 500 characters")
  private String guarantorSignature;

  @Column(name = "document_path", length = 500)
  @Size(max = 500, message = "Document path must not exceed 500 characters")
  private String documentPath;

  @Column(name = "notes", length = 500)
  @Size(max = 500, message = "Notes must not exceed 500 characters")
  private String notes;

  // Business logic methods
  /**
   * Check if guarantor is currently active
   */
  public boolean isCurrentlyActive() {
    if (!Boolean.TRUE.equals(isActive)) {
      return false;
    }

    LocalDate today = LocalDate.now();
    if (guaranteeStartDate != null && today.isBefore(guaranteeStartDate)) {
      return false;
    }

    if (guaranteeEndDate != null && today.isAfter(guaranteeEndDate)) {
      return false;
    }

    return true;
  }

  /**
   * Check if guarantor has sufficient shares to guarantee
   */
  public boolean hasSufficientShares() {
    if (member == null || member.getSavingAccount() == null) {
      return false;
    }

    BigDecimal memberShares = member.getSavingAccount().getShareCapital();
    if (memberShares == null) {
      return false;
    }
    BigDecimal requiredShares = guaranteedAmount.multiply(new BigDecimal("0.10")); // 10% of guaranteed amount

    return memberShares.compareTo(requiredShares) >= 0;
  }

  /**
   * Calculate remaining guarantee amount
   */
  public BigDecimal getRemainingGuaranteeAmount() {
    if (loan == null || loan.getOutstandingBalance() == null) {
      return guaranteedAmount;
    }

    BigDecimal remaining = guaranteedAmount.subtract(loan.getOutstandingBalance());
    return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
  }

  /**
   * Calculate guarantee utilization percentage
   */
  public BigDecimal getGuaranteeUtilizationPercentage() {
    if (guaranteedAmount == null || guaranteedAmount.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    if (loan == null || loan.getOutstandingBalance() == null) {
      return BigDecimal.ZERO;
    }

    return loan.getOutstandingBalance()
        .divide(guaranteedAmount, 4, java.math.RoundingMode.HALF_UP)
        .multiply(new BigDecimal("100"))
        .setScale(2, java.math.RoundingMode.HALF_UP);
  }

  /**
   * Check if guarantor can be released
   */
  public boolean canBeReleased() {
    return loan != null &&
        (loan.getStatus() == com.bansaiyai.bansaiyai.entity.enums.LoanStatus.COMPLETED ||
            loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Release guarantor from obligations
   */
  public void releaseGuarantor(String releaseNotes) {
    this.isActive = false;
    this.guaranteeEndDate = LocalDate.now();
    if (this.notes == null || this.notes.trim().isEmpty()) {
      this.notes = releaseNotes;
    } else {
      this.notes = this.notes + "\n" + releaseNotes;
    }
  }

  /**
   * Calculate maximum guarantee amount based on member's shares
   */
  public BigDecimal calculateMaximumGuaranteeAmount() {
    if (member == null || member.getSavingAccount() == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal memberShares = member.getSavingAccount().getShareCapital();
    if (memberShares == null) {
      return BigDecimal.ZERO;
    }

    // Maximum guarantee is 10 times member's share capital
    return memberShares.multiply(new BigDecimal("10"));
  }

  @PrePersist
  protected void onCreate() {
    if (guarantorNumber == null || guarantorNumber.trim().isEmpty()) {
      guarantorNumber = generateGuarantorNumber();
    }
    if (guaranteeStartDate == null) {
      guaranteeStartDate = LocalDate.now();
    }
    if (guaranteePercentage == null) {
      guaranteePercentage = new BigDecimal("100"); // Default 100% guarantee
    }
  }

  /**
   * Generate unique guarantor number with format: GUA-YYYYMMDD-XXXX
   */
  private String generateGuarantorNumber() {
    String date = LocalDate.now().toString().replace("-", "");
    String random = String.format("%04d", (int) (Math.random() * 10000));
    return "GUA-" + date + "-" + random;
  }
}
