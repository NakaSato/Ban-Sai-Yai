package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.AccountType;
import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for creating a new savings account.
 * Contains validation rules and account creation parameters.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingAccountRequest {

  @NotNull(message = "Member ID is required")
  private Long memberId;

  @NotNull(message = "Account type is required")
  private AccountType accountType;

  @NotBlank(message = "Account name is required")
  @Size(min = 3, max = 100, message = "Account name must be between 3 and 100 characters")
  private String accountName;

  @NotNull(message = "Initial deposit is required")
  @DecimalMin(value = "100.00", message = "Minimum initial deposit is 100.00")
  @DecimalMax(value = "1000000.00", message = "Maximum initial deposit is 1,000,000.00")
  private BigDecimal initialDeposit;

  @DecimalMin(value = "0.00", message = "Interest rate cannot be negative")
  @DecimalMax(value = "20.00", message = "Interest rate cannot exceed 20%")
  private BigDecimal interestRate;

  @DecimalMin(value = "0.00", message = "Minimum balance cannot be negative")
  private BigDecimal minimumBalance;

  @DecimalMin(value = "0.00", message = "Overdraft limit cannot be negative")
  @DecimalMax(value = "50000.00", message = "Overdraft limit cannot exceed 50,000.00")
  private BigDecimal overdraftLimit;

  @Size(max = 1000, message = "Notes must not exceed 1000 characters")
  private String notes;

  // Convenience method to set default interest rate based on account type
  public void setDefaultInterestRate() {
    if (interestRate == null) {
      switch (accountType) {
        case SAVINGS:
          interestRate = new BigDecimal("4.00");
          break;
        case FIXED_DEPOSIT:
          interestRate = new BigDecimal("6.50");
          break;
        case JUNIOR:
          interestRate = new BigDecimal("5.00");
          break;
        case SENIOR:
          interestRate = new BigDecimal("5.50");
          break;
        case BUSINESS:
          interestRate = new BigDecimal("3.50");
          break;
        case EDUCATION:
          interestRate = new BigDecimal("5.00");
          break;
        case EMERGENCY:
          interestRate = new BigDecimal("2.50");
          break;
        default:
          interestRate = new BigDecimal("4.00");
      }
    }
  }

  // Convenience method to set default minimum balance based on account type
  public void setDefaultMinimumBalance() {
    if (minimumBalance == null) {
      switch (accountType) {
        case FIXED_DEPOSIT:
          minimumBalance = new BigDecimal("1000.00");
          break;
        case BUSINESS:
          minimumBalance = new BigDecimal("500.00");
          break;
        default:
          minimumBalance = new BigDecimal("100.00");
      }
    }
  }

  // Manual getters for Lombok compatibility
  public Long getMemberId() {
    return memberId;
  }

  public AccountType getAccountType() {
    return accountType;
  }

  public String getAccountName() {
    return accountName;
  }

  public BigDecimal getInitialDeposit() {
    return initialDeposit;
  }

  public BigDecimal getInterestRate() {
    return interestRate;
  }

  public BigDecimal getMinimumBalance() {
    return minimumBalance;
  }

  public BigDecimal getOverdraftLimit() {
    return overdraftLimit;
  }

  public String getNotes() {
    return notes;
  }

  // Manual setters for Lombok compatibility
  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }

  public void setAccountType(AccountType accountType) {
    this.accountType = accountType;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public void setInitialDeposit(BigDecimal initialDeposit) {
    this.initialDeposit = initialDeposit;
  }

  public void setInterestRate(BigDecimal interestRate) {
    this.interestRate = interestRate;
  }

  public void setMinimumBalance(BigDecimal minimumBalance) {
    this.minimumBalance = minimumBalance;
  }

  public void setOverdraftLimit(BigDecimal overdraftLimit) {
    this.overdraftLimit = overdraftLimit;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  // Manual builder for Lombok compatibility
  public static SavingAccountRequestBuilder builder() {
    return new SavingAccountRequestBuilder();
  }

  public static class SavingAccountRequestBuilder {
    private Long memberId;
    private AccountType accountType;
    private String accountName;
    private BigDecimal initialDeposit;
    private BigDecimal interestRate;
    private BigDecimal minimumBalance;
    private BigDecimal overdraftLimit;
    private String notes;

    public SavingAccountRequestBuilder memberId(Long memberId) {
      this.memberId = memberId;
      return this;
    }

    public SavingAccountRequestBuilder accountType(AccountType accountType) {
      this.accountType = accountType;
      return this;
    }

    public SavingAccountRequestBuilder accountName(String accountName) {
      this.accountName = accountName;
      return this;
    }

    public SavingAccountRequestBuilder initialDeposit(BigDecimal initialDeposit) {
      this.initialDeposit = initialDeposit;
      return this;
    }

    public SavingAccountRequestBuilder interestRate(BigDecimal interestRate) {
      this.interestRate = interestRate;
      return this;
    }

    public SavingAccountRequestBuilder minimumBalance(BigDecimal minimumBalance) {
      this.minimumBalance = minimumBalance;
      return this;
    }

    public SavingAccountRequestBuilder overdraftLimit(BigDecimal overdraftLimit) {
      this.overdraftLimit = overdraftLimit;
      return this;
    }

    public SavingAccountRequestBuilder notes(String notes) {
      this.notes = notes;
      return this;
    }

    public SavingAccountRequest build() {
      SavingAccountRequest request = new SavingAccountRequest();
      request.memberId = this.memberId;
      request.accountType = this.accountType;
      request.accountName = this.accountName;
      request.initialDeposit = this.initialDeposit;
      request.interestRate = this.interestRate;
      request.minimumBalance = this.minimumBalance;
      request.overdraftLimit = this.overdraftLimit;
      request.notes = this.notes;
      return request;
    }
  }
}
