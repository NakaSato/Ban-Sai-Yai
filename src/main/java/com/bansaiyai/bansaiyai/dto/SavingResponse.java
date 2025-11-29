package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning savings account data.
 * Contains account information with calculated fields and formatted dates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingResponse {

  private Long id;
  private String accountNumber;
  private Long memberId;
  private String memberName;
  private AccountType accountType;
  private String accountTypeDisplay;
  private String accountName;
  private BigDecimal balance;
  private BigDecimal availableBalance;
  private BigDecimal effectiveAvailableBalance;
  private BigDecimal interestRate;
  private BigDecimal minimumBalance;
  private BigDecimal overdraftLimit;
  private LocalDate openingDate;
  private LocalDate lastInterestDate;
  private Boolean isActive;
  private Boolean isFrozen;
  private String freezeReason;
  private String notes;
  private Boolean meetsMinimumBalance;
  private Boolean isInterestOverdue;
  private BigDecimal dailyInterest;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  // Calculated fields
  private Integer accountAgeInDays;
  private String accountStatus;
  private BigDecimal interestEarnedThisMonth;
  private Integer daysSinceLastTransaction;
  private Boolean hasOverdraftProtection;

  // Convenience methods for display
  public String getFormattedBalance() {
    if (balance == null)
      return "0.00";
    return String.format("%,.2f", balance);
  }

  public String getFormattedAvailableBalance() {
    if (availableBalance == null)
      return "0.00";
    return String.format("%,.2f", availableBalance);
  }

  public String getFormattedInterestRate() {
    if (interestRate == null)
      return "0.00%";
    return String.format("%.2f%%", interestRate);
  }

  public String getFormattedOpeningDate() {
    if (openingDate == null)
      return "";
    return openingDate.toString();
  }

  public String getAccountStatusDescription() {
    if (!isActive)
      return "Inactive";
    if (isFrozen)
      return "Frozen";
    return "Active";
  }

  public Boolean getCanWithdraw() {
    return isActive && !isFrozen && effectiveAvailableBalance.compareTo(BigDecimal.ZERO) > 0;
  }

  // Static factory method from entity
  public static SavingResponse fromEntity(com.bansaiyai.bansaiyai.entity.SavingAccount account) {
    if (account == null)
      return null;

    return SavingResponse.builder()
        .id(account.getId())
        .accountNumber(account.getAccountNumber())
        .memberId(account.getMember() != null ? account.getMember().getId() : null)
        .memberName(account.getMember() != null ? account.getMember().getName() : null)
        .accountType(account.getAccountType())
        .accountTypeDisplay(account.getAccountType() != null ? account.getAccountType().getDisplayName() : null)
        .accountName(account.getAccountName())
        .balance(account.getBalance())
        .availableBalance(account.getAvailableBalance())
        .effectiveAvailableBalance(account.getEffectiveAvailableBalance())
        .interestRate(account.getInterestRate())
        .minimumBalance(account.getMinimumBalance())
        .overdraftLimit(account.getOverdraftLimit())
        .openingDate(account.getOpeningDate())
        .lastInterestDate(account.getLastInterestDate())
        .isActive(account.getIsActive())
        .isFrozen(account.getIsFrozen())
        .freezeReason(account.getFreezeReason())
        .notes(account.getNotes())
        .meetsMinimumBalance(account.meetsMinimumBalance())
        .isInterestOverdue(account.isInterestOverdue())
        .dailyInterest(account.calculateDailyInterest())
        .createdAt(account.getCreatedAt())
        .updatedAt(account.getUpdatedAt())
        .createdBy(account.getCreatedBy())
        .updatedBy(account.getUpdatedBy())
        .accountAgeInDays(account.getOpeningDate() != null
            ? (int) java.time.temporal.ChronoUnit.DAYS.between(account.getOpeningDate(), LocalDate.now())
            : 0)
        .accountStatus(getAccountStatusFromEntity(account))
        .hasOverdraftProtection(account.getOverdraftLimit() != null &&
            account.getOverdraftLimit().compareTo(BigDecimal.ZERO) > 0)
        .build();
  }

  private static String getAccountStatusFromEntity(com.bansaiyai.bansaiyai.entity.SavingAccount account) {
    if (!account.getIsActive())
      return "INACTIVE";
    if (account.getIsFrozen())
      return "FROZEN";
    return "ACTIVE";
  }

  // Manual builder for Lombok compatibility
  public static SavingResponseBuilder builder() {
    return new SavingResponseBuilder();
  }

  public static class SavingResponseBuilder {
    private Long id;
    private String accountNumber;
    private Long memberId;
    private String memberName;
    private AccountType accountType;
    private String accountTypeDisplay;
    private String accountName;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal effectiveAvailableBalance;
    private BigDecimal interestRate;
    private BigDecimal minimumBalance;
    private BigDecimal overdraftLimit;
    private LocalDate openingDate;
    private LocalDate lastInterestDate;
    private Boolean isActive;
    private Boolean isFrozen;
    private String freezeReason;
    private String notes;
    private Boolean meetsMinimumBalance;
    private Boolean isInterestOverdue;
    private BigDecimal dailyInterest;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer accountAgeInDays;
    private String accountStatus;
    private BigDecimal interestEarnedThisMonth;
    private Integer daysSinceLastTransaction;
    private Boolean hasOverdraftProtection;

    public SavingResponseBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public SavingResponseBuilder accountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
      return this;
    }

    public SavingResponseBuilder memberId(Long memberId) {
      this.memberId = memberId;
      return this;
    }

    public SavingResponseBuilder memberName(String memberName) {
      this.memberName = memberName;
      return this;
    }

    public SavingResponseBuilder accountType(AccountType accountType) {
      this.accountType = accountType;
      return this;
    }

    public SavingResponseBuilder accountTypeDisplay(String accountTypeDisplay) {
      this.accountTypeDisplay = accountTypeDisplay;
      return this;
    }

    public SavingResponseBuilder accountName(String accountName) {
      this.accountName = accountName;
      return this;
    }

    public SavingResponseBuilder balance(BigDecimal balance) {
      this.balance = balance;
      return this;
    }

    public SavingResponseBuilder availableBalance(BigDecimal availableBalance) {
      this.availableBalance = availableBalance;
      return this;
    }

    public SavingResponseBuilder effectiveAvailableBalance(BigDecimal effectiveAvailableBalance) {
      this.effectiveAvailableBalance = effectiveAvailableBalance;
      return this;
    }

    public SavingResponseBuilder interestRate(BigDecimal interestRate) {
      this.interestRate = interestRate;
      return this;
    }

    public SavingResponseBuilder minimumBalance(BigDecimal minimumBalance) {
      this.minimumBalance = minimumBalance;
      return this;
    }

    public SavingResponseBuilder overdraftLimit(BigDecimal overdraftLimit) {
      this.overdraftLimit = overdraftLimit;
      return this;
    }

    public SavingResponseBuilder openingDate(LocalDate openingDate) {
      this.openingDate = openingDate;
      return this;
    }

    public SavingResponseBuilder lastInterestDate(LocalDate lastInterestDate) {
      this.lastInterestDate = lastInterestDate;
      return this;
    }

    public SavingResponseBuilder isActive(Boolean isActive) {
      this.isActive = isActive;
      return this;
    }

    public SavingResponseBuilder isFrozen(Boolean isFrozen) {
      this.isFrozen = isFrozen;
      return this;
    }

    public SavingResponseBuilder freezeReason(String freezeReason) {
      this.freezeReason = freezeReason;
      return this;
    }

    public SavingResponseBuilder notes(String notes) {
      this.notes = notes;
      return this;
    }

    public SavingResponseBuilder meetsMinimumBalance(Boolean meetsMinimumBalance) {
      this.meetsMinimumBalance = meetsMinimumBalance;
      return this;
    }

    public SavingResponseBuilder isInterestOverdue(Boolean isInterestOverdue) {
      this.isInterestOverdue = isInterestOverdue;
      return this;
    }

    public SavingResponseBuilder dailyInterest(BigDecimal dailyInterest) {
      this.dailyInterest = dailyInterest;
      return this;
    }

    public SavingResponseBuilder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public SavingResponseBuilder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public SavingResponseBuilder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public SavingResponseBuilder updatedBy(String updatedBy) {
      this.updatedBy = updatedBy;
      return this;
    }

    public SavingResponseBuilder accountAgeInDays(Integer accountAgeInDays) {
      this.accountAgeInDays = accountAgeInDays;
      return this;
    }

    public SavingResponseBuilder accountStatus(String accountStatus) {
      this.accountStatus = accountStatus;
      return this;
    }

    public SavingResponseBuilder interestEarnedThisMonth(BigDecimal interestEarnedThisMonth) {
      this.interestEarnedThisMonth = interestEarnedThisMonth;
      return this;
    }

    public SavingResponseBuilder daysSinceLastTransaction(Integer daysSinceLastTransaction) {
      this.daysSinceLastTransaction = daysSinceLastTransaction;
      return this;
    }

    public SavingResponseBuilder hasOverdraftProtection(Boolean hasOverdraftProtection) {
      this.hasOverdraftProtection = hasOverdraftProtection;
      return this;
    }

    public SavingResponse build() {
      SavingResponse response = new SavingResponse();
      response.id = this.id;
      response.accountNumber = this.accountNumber;
      response.memberId = this.memberId;
      response.memberName = this.memberName;
      response.accountType = this.accountType;
      response.accountTypeDisplay = this.accountTypeDisplay;
      response.accountName = this.accountName;
      response.balance = this.balance;
      response.availableBalance = this.availableBalance;
      response.effectiveAvailableBalance = this.effectiveAvailableBalance;
      response.interestRate = this.interestRate;
      response.minimumBalance = this.minimumBalance;
      response.overdraftLimit = this.overdraftLimit;
      response.openingDate = this.openingDate;
      response.lastInterestDate = this.lastInterestDate;
      response.isActive = this.isActive;
      response.isFrozen = this.isFrozen;
      response.freezeReason = this.freezeReason;
      response.notes = this.notes;
      response.meetsMinimumBalance = this.meetsMinimumBalance;
      response.isInterestOverdue = this.isInterestOverdue;
      response.dailyInterest = this.dailyInterest;
      response.createdAt = this.createdAt;
      response.updatedAt = this.updatedAt;
      response.createdBy = this.createdBy;
      response.updatedBy = this.updatedBy;
      response.accountAgeInDays = this.accountAgeInDays;
      response.accountStatus = this.accountStatus;
      response.interestEarnedThisMonth = this.interestEarnedThisMonth;
      response.daysSinceLastTransaction = this.daysSinceLastTransaction;
      response.hasOverdraftProtection = this.hasOverdraftProtection;
      return response;
    }
  }
}
