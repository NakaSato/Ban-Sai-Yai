package com.bansaiyai.bansaiyai.entity;

import com.bansaiyai.bansaiyai.entity.enums.AccountType;
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

/**
 * Savings account entity for cooperative member savings.
 * Tracks account balances, interest rates, and transaction history.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Entity
@Table(name = "saving_account", indexes = {
    @Index(name = "idx_account_number", columnList = "accountNumber"),
    @Index(name = "idx_member_id", columnList = "memberId"),
    @Index(name = "idx_account_type", columnList = "accountType"),
    @Index(name = "idx_is_active", columnList = "isActive")
})
@EntityListeners(AuditingEntityListener.class)
public class SavingAccount extends BaseEntity {

  @Column(name = "account_number", unique = true, nullable = false, length = 20)
  @NotBlank(message = "Account number is required")
  @Size(min = 8, max = 20, message = "Account number must be between 8 and 20 characters")
  private String accountNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  @NotNull(message = "Member is required")
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false, length = 20)
  @NotNull(message = "Account type is required")
  private AccountType accountType;

  @Column(name = "account_name", nullable = false, length = 100)
  @NotBlank(message = "Account name is required")
  @Size(max = 100, message = "Account name must not exceed 100 characters")
  private String accountName;

  @Column(name = "balance", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Balance is required")
  @DecimalMin(value = "0.00", message = "Balance cannot be negative")
  private BigDecimal balance;

  @Column(name = "available_balance", nullable = false, precision = 15, scale = 2)
  @NotNull(message = "Available balance is required")
  @DecimalMin(value = "0.00", message = "Available balance cannot be negative")
  private BigDecimal availableBalance;

  @Column(name = "share_capital", precision = 15, scale = 2)
  @DecimalMin(value = "0.00", message = "Share capital cannot be negative")
  private BigDecimal shareCapital;

  @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
  @NotNull(message = "Interest rate is required")
  @DecimalMin(value = "0.01", message = "Minimum interest rate is 0.01%")
  @DecimalMax(value = "20.00", message = "Maximum interest rate is 20%")
  private BigDecimal interestRate;

  @Column(name = "minimum_balance", precision = 15, scale = 2)
  @DecimalMin(value = "0.00", message = "Minimum balance cannot be negative")
  private BigDecimal minimumBalance;

  @Column(name = "overdraft_limit", precision = 15, scale = 2)
  @DecimalMin(value = "0.00", message = "Overdraft limit cannot be negative")
  private BigDecimal overdraftLimit;

  @Column(name = "opening_date", nullable = false)
  @NotNull(message = "Opening date is required")
  private LocalDate openingDate;

  @Column(name = "last_interest_date")
  private LocalDate lastInterestDate;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "is_frozen", nullable = false)
  private Boolean isFrozen = false;

  @Column(name = "freeze_reason", length = 500)
  private String freezeReason;

  @Column(name = "notes", length = 1000)
  private String notes;

  // Relationships
  @OneToMany(mappedBy = "savingAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<SavingTransaction> transactions;

  @OneToMany(mappedBy = "savingAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<SavingBalance> balances;

  // Business logic methods
  /**
   * Check if account can withdraw specified amount
   */
  public boolean canWithdraw(BigDecimal amount) {
    if (isFrozen || !isActive) {
      return false;
    }
    BigDecimal availableFunds = availableBalance.add(overdraftLimit != null ? overdraftLimit : BigDecimal.ZERO);
    return availableFunds.compareTo(amount) >= 0;
  }

  /**
   * Check if account meets minimum balance requirement
   */
  public boolean meetsMinimumBalance() {
    if (minimumBalance == null) {
      return true;
    }
    return balance.compareTo(minimumBalance) >= 0;
  }

  /**
   * Calculate daily interest
   */
  public BigDecimal calculateDailyInterest() {
    if (balance.compareTo(BigDecimal.ZERO) <= 0 || interestRate.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    // Daily interest = (Balance * Annual Rate) / 365 / 100
    return balance.multiply(interestRate)
        .divide(new BigDecimal("36500"), 8, RoundingMode.HALF_UP);
  }

  /**
   * Get effective available balance for withdrawals
   */
  public BigDecimal getEffectiveAvailableBalance() {
    BigDecimal overdraft = overdraftLimit != null ? overdraftLimit : BigDecimal.ZERO;
    return availableBalance.add(overdraft);
  }

  /**
   * Check if account is overdue for interest calculation
   */
  public boolean isInterestOverdue() {
    if (lastInterestDate == null) {
      return true;
    }
    return lastInterestDate.isBefore(LocalDate.now().minusMonths(1));
  }

  /**
   * Update available balance after transaction
   */
  public void updateAvailableBalance(BigDecimal amount, boolean isCredit) {
    if (isCredit) {
      availableBalance = availableBalance.add(amount);
      balance = balance.add(amount);
    } else {
      availableBalance = availableBalance.subtract(amount);
      balance = balance.subtract(amount);
    }
  }

  @PrePersist
  protected void onCreate() {
    if (accountNumber == null || accountNumber.trim().isEmpty()) {
      accountNumber = generateAccountNumber();
    }
    if (openingDate == null) {
      openingDate = LocalDate.now();
    }
    if (lastInterestDate == null) {
      lastInterestDate = LocalDate.now();
    }
    if (minimumBalance == null) {
      minimumBalance = BigDecimal.ZERO;
    }
    if (availableBalance == null) {
      availableBalance = balance != null ? balance : BigDecimal.ZERO;
    }
    if (overdraftLimit == null) {
      overdraftLimit = BigDecimal.ZERO;
    }
    if (shareCapital == null) {
      shareCapital = BigDecimal.ZERO;
    }
  }

  /**
   * Generate unique account number with format: SA-YYYYMMDD-XXXX
   */
  private String generateAccountNumber() {
    String date = LocalDate.now().toString().replace("-", "");
    String random = String.format("%04d", (int) (Math.random() * 10000));
    return "SA-" + date + "-" + random;
  }

  // Additional getters and setters for Lombok compatibility
  public BigDecimal getShareCapital() {
    return shareCapital;
  }

  public void setShareCapital(BigDecimal shareCapital) {
    this.shareCapital = shareCapital;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public Member getMember() {
    return member;
  }

  public AccountType getAccountType() {
    return accountType;
  }

  public String getAccountName() {
    return accountName;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public BigDecimal getAvailableBalance() {
    return availableBalance;
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

  public LocalDate getOpeningDate() {
    return openingDate;
  }

  public LocalDate getLastInterestDate() {
    return lastInterestDate;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public Boolean getIsFrozen() {
    return isFrozen;
  }

  public String getFreezeReason() {
    return freezeReason;
  }

  public String getNotes() {
    return notes;
  }

  // Setters
  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public void setIsFrozen(Boolean isFrozen) {
    this.isFrozen = isFrozen;
  }

  public void setFreezeReason(String freezeReason) {
    this.freezeReason = freezeReason;
  }

  public void setLastInterestDate(LocalDate lastInterestDate) {
    this.lastInterestDate = lastInterestDate;
  }

  // Manual builder for Lombok compatibility
  public static SavingAccountBuilder builder() {
    return new SavingAccountBuilder();
  }

  public static class SavingAccountBuilder {
    private String accountNumber;
    private Member member;
    private AccountType accountType;
    private String accountName;
    private java.math.BigDecimal balance;
    private java.math.BigDecimal availableBalance;
    private java.math.BigDecimal shareCapital;
    private java.math.BigDecimal interestRate;
    private java.math.BigDecimal minimumBalance;
    private java.math.BigDecimal overdraftLimit;
    private java.time.LocalDate openingDate;
    private java.time.LocalDate lastInterestDate;
    private Boolean isActive = true;
    private Boolean isFrozen = false;
    private String freezeReason;
    private String notes;
    private String createdBy;
    private String updatedBy;

    public SavingAccountBuilder accountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
      return this;
    }

    public SavingAccountBuilder member(Member member) {
      this.member = member;
      return this;
    }

    public SavingAccountBuilder accountType(AccountType accountType) {
      this.accountType = accountType;
      return this;
    }

    public SavingAccountBuilder accountName(String accountName) {
      this.accountName = accountName;
      return this;
    }

    public SavingAccountBuilder balance(java.math.BigDecimal balance) {
      this.balance = balance;
      return this;
    }

    public SavingAccountBuilder availableBalance(java.math.BigDecimal availableBalance) {
      this.availableBalance = availableBalance;
      return this;
    }

    public SavingAccountBuilder shareCapital(java.math.BigDecimal shareCapital) {
      this.shareCapital = shareCapital;
      return this;
    }

    public SavingAccountBuilder interestRate(java.math.BigDecimal interestRate) {
      this.interestRate = interestRate;
      return this;
    }

    public SavingAccountBuilder minimumBalance(java.math.BigDecimal minimumBalance) {
      this.minimumBalance = minimumBalance;
      return this;
    }

    public SavingAccountBuilder overdraftLimit(java.math.BigDecimal overdraftLimit) {
      this.overdraftLimit = overdraftLimit;
      return this;
    }

    public SavingAccountBuilder openingDate(java.time.LocalDate openingDate) {
      this.openingDate = openingDate;
      return this;
    }

    public SavingAccountBuilder lastInterestDate(java.time.LocalDate lastInterestDate) {
      this.lastInterestDate = lastInterestDate;
      return this;
    }

    public SavingAccountBuilder isActive(Boolean isActive) {
      this.isActive = isActive;
      return this;
    }

    public SavingAccountBuilder isFrozen(Boolean isFrozen) {
      this.isFrozen = isFrozen;
      return this;
    }

    public SavingAccountBuilder freezeReason(String freezeReason) {
      this.freezeReason = freezeReason;
      return this;
    }

    public SavingAccountBuilder notes(String notes) {
      this.notes = notes;
      return this;
    }

    public SavingAccountBuilder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public SavingAccountBuilder updatedBy(String updatedBy) {
      this.updatedBy = updatedBy;
      return this;
    }

    public SavingAccountBuilder id(Long id) {
      // Note: ID is typically set by JPA, but added for testing
      return this;
    }

    public SavingAccount build() {
      SavingAccount account = new SavingAccount();
      account.accountNumber = this.accountNumber;
      account.member = this.member;
      account.accountType = this.accountType;
      account.accountName = this.accountName;
      account.balance = this.balance;
      account.availableBalance = this.availableBalance;
      account.shareCapital = this.shareCapital;
      account.interestRate = this.interestRate;
      account.minimumBalance = this.minimumBalance;
      account.overdraftLimit = this.overdraftLimit;
      account.openingDate = this.openingDate;
      account.lastInterestDate = this.lastInterestDate;
      account.isActive = this.isActive;
      account.isFrozen = this.isFrozen;
      account.freezeReason = this.freezeReason;
      account.notes = this.notes;
      account.setCreatedBy(this.createdBy);
      account.setUpdatedBy(this.updatedBy);
      return account;
    }
  }
}
