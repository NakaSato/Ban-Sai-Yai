package com.bansaiyai.bansaiyai.entity;

import com.bansaiyai.bansaiyai.entity.enums.CollateralType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

/**
 * Collateral entity representing loan collateral assets.
 * Tracks property, vehicles, and other assets used as loan guarantees.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Entity
@Table(name = "collateral", indexes = {
    @Index(name = "idx_collateral_number", columnList = "collateralNumber"),
    @Index(name = "idx_loan_id", columnList = "loanId"),
    @Index(name = "idx_collateral_type", columnList = "collateralType")
})
@EntityListeners(AuditingEntityListener.class)
public class Collateral extends BaseEntity {

  @Column(name = "collateral_number", unique = true, nullable = false, length = 50)
  @NotBlank(message = "Collateral number is required")
  @Size(min = 5, max = 50, message = "Collateral number must be between 5 and 50 characters")
  private String collateralNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loan_id", nullable = false)
  @NotNull(message = "Loan is required")
  private Loan loan;

  @Enumerated(EnumType.STRING)
  @Column(name = "collateral_type", nullable = false, length = 30)
  @NotNull(message = "Collateral type is required")
  private CollateralType collateralType;

  @Column(name = "description", nullable = false, length = 500)
  @NotBlank(message = "Description is required")
  @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
  private String description;

  @Column(name = "estimated_value", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Estimated value is required")
  @DecimalMin(value = "1000.00", message = "Minimum estimated value is THB 1,000")
  private BigDecimal estimatedValue;

  @Column(name = "appraised_value", precision = 15, scale = 2)
  private BigDecimal appraisedValue;

  @Column(name = "loan_value_ratio", precision = 5, scale = 2)
  private BigDecimal loanValueRatio;

  @Column(name = "ownership_document", length = 100)
  @Size(max = 100, message = "Ownership document must not exceed 100 characters")
  private String ownershipDocument;

  @Column(name = "document_number", length = 50)
  @Size(max = 50, message = "Document number must not exceed 50 characters")
  private String documentNumber;

  @Column(name = "issue_date")
  private java.time.LocalDate issueDate;

  @Column(name = "expiry_date")
  private java.time.LocalDate expiryDate;

  @Column(name = "location", length = 300)
  @Size(max = 300, message = "Location must not exceed 300 characters")
  private String location;

  @Column(name = "owner_name", length = 100)
  @Size(max = 100, message = "Owner name must not exceed 100 characters")
  private String ownerName;

  @Column(name = "owner_address", length = 300)
  @Size(max = 300, message = "Owner address must not exceed 300 characters")
  private String ownerAddress;

  @Column(name = "is_verified", nullable = false)
  @Builder.Default
  private Boolean isVerified = false;

  @Column(name = "verification_date")
  private java.time.LocalDate verificationDate;

  @Column(name = "verified_by")
  private String verifiedBy;

  @Column(name = "verification_notes", length = 500)
  @Size(max = 500, message = "Verification notes must not exceed 500 characters")
  private String verificationNotes;

  @Column(name = "is_released", nullable = false)
  @Builder.Default
  private Boolean isReleased = false;

  @Column(name = "release_date")
  private java.time.LocalDate releaseDate;

  @Column(name = "release_notes", length = 500)
  @Size(max = 500, message = "Release notes must not exceed 500 characters")
  private String releaseNotes;

  @Column(name = "photo_path", length = 500)
  @Size(max = 500, message = "Photo path must not exceed 500 characters")
  private String photoPath;

  @Column(name = "document_path", length = 500)
  @Size(max = 500, message = "Document path must not exceed 500 characters")
  private String documentPath;

  // Business logic methods
  /**
   * Check if collateral is valid (not expired)
   */
  public boolean isValid() {
    if (expiryDate == null) {
      return true; // No expiry date means it's always valid
    }
    return !java.time.LocalDate.now().isAfter(expiryDate);
  }

  /**
   * Check if collateral is verified
   */
  public boolean isVerifiedCollateral() {
    return Boolean.TRUE.equals(isVerified) && verificationDate != null;
  }

  /**
   * Check if collateral is available (not released)
   */
  public boolean isAvailable() {
    return !Boolean.TRUE.equals(isReleased);
  }

  /**
   * Get maximum loan amount based on collateral value and ratio
   */
  public BigDecimal getMaximumLoanAmount() {
    BigDecimal value = appraisedValue != null ? appraisedValue : estimatedValue;
    BigDecimal ratio = loanValueRatio != null ? loanValueRatio : new BigDecimal("0.70"); // Default 70%

    if (value == null || ratio == null) {
      return BigDecimal.ZERO;
    }

    return value.multiply(ratio).setScale(2, BigDecimal.ROUND_HALF_UP);
  }

  /**
   * Mark collateral as verified
   */
  public void markAsVerified(String verifiedBy, String notes) {
    this.isVerified = true;
    this.verificationDate = java.time.LocalDate.now();
    this.verifiedBy = verifiedBy;
    this.verificationNotes = notes;
  }

  /**
   * Release collateral
   */
  public void releaseCollateral(String releaseNotes) {
    this.isReleased = true;
    this.releaseDate = java.time.LocalDate.now();
    this.releaseNotes = releaseNotes;
  }

  /**
   * Calculate loan-to-value ratio
   */
  public BigDecimal calculateLoanToValueRatio(BigDecimal loanAmount) {
    if (loanAmount == null || estimatedValue == null || estimatedValue.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return loanAmount.divide(estimatedValue, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(new BigDecimal("100"))
        .setScale(2, BigDecimal.ROUND_HALF_UP);
  }

  @PrePersist
  protected void onCreate() {
    if (collateralNumber == null || collateralNumber.trim().isEmpty()) {
      collateralNumber = generateCollateralNumber();
    }
    if (loanValueRatio == null) {
      loanValueRatio = new BigDecimal("0.70"); // Default 70% loan-to-value ratio
    }
  }

  /**
   * Generate unique collateral number with format: COL-YYYYMMDD-XXXX
   */
  private String generateCollateralNumber() {
    String date = java.time.LocalDate.now().toString().replace("-", "");
    String random = String.format("%04d", (int) (Math.random() * 10000));
    return "COL-" + date + "-" + random;
  }
}
