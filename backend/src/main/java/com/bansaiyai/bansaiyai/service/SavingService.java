package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.SavingAccountRequest;
import com.bansaiyai.bansaiyai.dto.SavingResponse;
import com.bansaiyai.bansaiyai.entity.*;
import com.bansaiyai.bansaiyai.entity.enums.AccountType;
import com.bansaiyai.bansaiyai.entity.enums.TransactionType;
import com.bansaiyai.bansaiyai.repository.SavingRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bansaiyai.bansaiyai.util.InputSanitizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing savings accounts and transactions.
 * Handles account creation, transactions, interest calculations, and balance
 * management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SavingService {

  private final SavingRepository savingRepository;
  private final MemberRepository memberRepository;
  private final InputSanitizer inputSanitizer;

  /**
   * Create a new savings account
   */
  public SavingResponse createAccount(SavingAccountRequest request, String createdBy) {
    // Sanitize request inputs
    request.setAccountName(inputSanitizer.sanitizeText(request.getAccountName()));
    request.setNotes(inputSanitizer.sanitizeText(request.getNotes()));

    // Validate member exists and is active
    Member member = memberRepository.findById(request.getMemberId())
        .orElseThrow(() -> new RuntimeException("Member not found with ID: " + request.getMemberId()));

    if (!member.getIsActive()) {
      throw new RuntimeException("Cannot create account for inactive member");
    }

    // Check if member already has an account of this type
    Optional<SavingAccount> existingAccount = savingRepository.findByMemberIdAndAccountType(
        request.getMemberId(), request.getAccountType());
    if (existingAccount.isPresent() && existingAccount.get().getIsActive()) {
      throw new RuntimeException(
          "Member already has an active " + request.getAccountType().getDisplayName() + " account");
    }

    // Set default values
    request.setDefaultInterestRate();
    request.setDefaultMinimumBalance();

    // Create account
    SavingAccount account = SavingAccount.builder()
        .member(member)
        .accountType(request.getAccountType())
        .accountName(request.getAccountName())
        .balance(request.getInitialDeposit())
        .availableBalance(request.getInitialDeposit())
        .interestRate(request.getInterestRate())
        .minimumBalance(request.getMinimumBalance())
        .overdraftLimit(request.getOverdraftLimit())
        .notes(request.getNotes())
        .build();

    SavingAccount savedAccount = savingRepository.save(account);

    // Create opening transaction
    createOpeningTransaction(savedAccount, request.getInitialDeposit(), createdBy);

    log.info("Created savings account {} for member {}", savedAccount.getAccountNumber(), member.getId());
    return SavingResponse.fromEntity(savedAccount);
  }

  /**
   * Get account by ID
   */
  @Transactional(readOnly = true)
  public SavingResponse getAccount(Long accountId) {
    SavingAccount account = savingRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
    return SavingResponse.fromEntity(account);
  }

  /**
   * Get account by account number
   */
  @Transactional(readOnly = true)
  public SavingResponse getAccountByNumber(String accountNumber) {
    SavingAccount account = savingRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
    return SavingResponse.fromEntity(account);
  }

  /**
   * Get all accounts for a member
   */
  @Transactional(readOnly = true)
  public Page<SavingResponse> getAccountsByMember(Long memberId, Pageable pageable) {
    Page<SavingAccount> accounts = savingRepository.findByMemberIdAndIsActive(memberId, true, pageable);
    return accounts.map(SavingResponse::fromEntity);
  }

  /**
   * Get all accounts (admin only)
   */
  @Transactional(readOnly = true)
  public Page<SavingResponse> getAllAccounts(Pageable pageable) {
    Page<SavingAccount> accounts = savingRepository.findActiveAccounts(pageable);
    return accounts.map(SavingResponse::fromEntity);
  }

  /**
   * Process deposit to account
   */
  public SavingResponse deposit(Long accountId, BigDecimal amount, String description, String createdBy) {
    // Sanitize description
    description = inputSanitizer.sanitizeText(description);

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Deposit amount must be positive");
    }

    SavingAccount account = savingRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

    if (!account.getIsActive()) {
      throw new RuntimeException("Cannot deposit to inactive account");
    }

    BigDecimal balanceBefore = account.getBalance();
    account.updateAvailableBalance(amount, true);
    SavingAccount updatedAccount = savingRepository.save(account);

    // Create transaction
    createTransaction(updatedAccount, TransactionType.DEPOSIT, amount, description,
        balanceBefore, updatedAccount.getBalance(), createdBy);

    log.info("Deposited {} to account {}", amount, account.getAccountNumber());
    return SavingResponse.fromEntity(updatedAccount);
  }

  /**
   * Process withdrawal from account
   */
  public SavingResponse withdraw(Long accountId, BigDecimal amount, String description, String createdBy) {
    // Sanitize description
    description = inputSanitizer.sanitizeText(description);

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Withdrawal amount must be positive");
    }

    SavingAccount account = savingRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

    if (!account.canWithdraw(amount)) {
      throw new RuntimeException("Insufficient funds or account restrictions");
    }

    BigDecimal balanceBefore = account.getBalance();
    account.updateAvailableBalance(amount, false);
    SavingAccount updatedAccount = savingRepository.save(account);

    // Create transaction
    createTransaction(updatedAccount, TransactionType.WITHDRAWAL, amount, description,
        balanceBefore, updatedAccount.getBalance(), createdBy);

    log.info("Withdrew {} from account {}", amount, account.getAccountNumber());
    return SavingResponse.fromEntity(updatedAccount);
  }

  /**
   * Freeze account
   */
  public SavingResponse freezeAccount(Long accountId, String reason, String updatedBy) {
    // Sanitize reason
    reason = inputSanitizer.sanitizeText(reason);

    SavingAccount account = savingRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

    account.setIsFrozen(true);
    account.setFreezeReason(reason);
    account.setUpdatedBy(updatedBy);

    SavingAccount updatedAccount = savingRepository.save(account);
    log.info("Froze account {} for reason: {}", account.getAccountNumber(), reason);
    return SavingResponse.fromEntity(updatedAccount);
  }

  /**
   * Unfreeze account
   */
  public SavingResponse unfreezeAccount(Long accountId, String updatedBy) {
    SavingAccount account = savingRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

    account.setIsFrozen(false);
    account.setFreezeReason(null);
    account.setUpdatedBy(updatedBy);

    SavingAccount updatedAccount = savingRepository.save(account);
    log.info("Unfroze account {}", account.getAccountNumber());
    return SavingResponse.fromEntity(updatedAccount);
  }

  /**
   * Close account
   */
  public SavingResponse closeAccount(Long accountId, String updatedBy) {
    SavingAccount account = savingRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

    if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
      throw new RuntimeException("Cannot close account with positive balance");
    }

    account.setIsActive(false);
    account.setUpdatedBy(updatedBy);

    SavingAccount updatedAccount = savingRepository.save(account);
    log.info("Closed account {}", account.getAccountNumber());
    return SavingResponse.fromEntity(updatedAccount);
  }

  /**
   * Calculate and credit interest to accounts
   */
  public void calculateAndCreditInterest(LocalDate asOfDate) {
    List<SavingAccount> accounts = savingRepository.findAccountsRequiringInterest(asOfDate.minusMonths(1));

    for (SavingAccount account : accounts) {
      try {
        BigDecimal interestAmount = calculateInterestForPeriod(account, asOfDate);
        if (interestAmount.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal balanceBefore = account.getBalance();
          account.updateAvailableBalance(interestAmount, true);
          account.setLastInterestDate(asOfDate);
          savingRepository.save(account);

          // Create interest transaction
          createTransaction(account, TransactionType.INTEREST_CREDIT, interestAmount,
              "Monthly interest credit", balanceBefore, account.getBalance(), "SYSTEM");

          log.info("Credited interest {} to account {}", interestAmount, account.getAccountNumber());
        }
      } catch (Exception e) {
        log.error("Failed to calculate interest for account {}: {}",
            account.getAccountNumber(), e.getMessage());
      }
    }
  }

  /**
   * Calculate interest for a period
   */
  private BigDecimal calculateInterestForPeriod(SavingAccount account, LocalDate asOfDate) {
    LocalDate startDate = account.getLastInterestDate() != null ? account.getLastInterestDate()
        : account.getOpeningDate();

    int daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, asOfDate);
    if (daysInPeriod <= 0) {
      return BigDecimal.ZERO;
    }

    // Simple daily compounding interest
    BigDecimal dailyInterest = account.calculateDailyInterest();
    return dailyInterest.multiply(new BigDecimal(daysInPeriod))
        .setScale(2, java.math.RoundingMode.HALF_UP);
  }

  /**
   * Create opening transaction
   */
  private void createOpeningTransaction(SavingAccount account, BigDecimal amount, String createdBy) {
    createTransaction(account, TransactionType.OPENING_BALANCE, amount,
        "Account opening deposit", BigDecimal.ZERO, amount, createdBy);
  }

  /**
   * Create a transaction
   */
  private void createTransaction(SavingAccount account, TransactionType type, BigDecimal amount,
      String description, BigDecimal balanceBefore, BigDecimal balanceAfter, String createdBy) {

    SavingTransaction transaction = SavingTransaction.builder()
        .savingAccount(account)
        .transactionType(type)
        .amount(amount)
        .description(description)
        .balanceBefore(balanceBefore)
        .balanceAfter(balanceAfter)
        .build();

    // Log transaction details
    log.debug("Created transaction: {} {} for account {} - {}",
        type, amount, account.getAccountNumber(), transaction.getDescription());
  }

  /**
   * Get account statistics
   */
  @Transactional(readOnly = true)
  public AccountStatistics getAccountStatistics(Long accountId, LocalDate startDate, LocalDate endDate) {
    SavingAccount account = savingRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

    // Note: In a real implementation, you'd query transaction repository for these
    // values
    return AccountStatistics.builder()
        .accountId(accountId)
        .accountNumber(account.getAccountNumber())
        .currentBalance(account.getBalance())
        .accountType(account.getAccountType())
        .accountName(account.getAccountName())
        .openingDate(account.getOpeningDate())
        .lastInterestDate(account.getLastInterestDate())
        .interestRate(account.getInterestRate())
        .isActive(account.getIsActive())
        .isFrozen(account.getIsFrozen())
        .build();
  }

  /**
   * DTO for account statistics
   */
  public static class AccountStatistics {
    private Long accountId;
    private String accountNumber;
    private BigDecimal currentBalance;
    private AccountType accountType;
    private String accountName;
    private LocalDate openingDate;
    private LocalDate lastInterestDate;
    private BigDecimal interestRate;
    private Boolean isActive;
    private Boolean isFrozen;
    // Additional fields like transaction counts, interest earned, etc. would be
    // added here

    // No-arg constructor
    public AccountStatistics() {
    }

    // Getters and setters
    public Long getAccountId() {
      return accountId;
    }

    public void setAccountId(Long accountId) {
      this.accountId = accountId;
    }

    public String getAccountNumber() {
      return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
    }

    public BigDecimal getCurrentBalance() {
      return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
      this.currentBalance = currentBalance;
    }

    public AccountType getAccountType() {
      return accountType;
    }

    public void setAccountType(AccountType accountType) {
      this.accountType = accountType;
    }

    public String getAccountName() {
      return accountName;
    }

    public void setAccountName(String accountName) {
      this.accountName = accountName;
    }

    public LocalDate getOpeningDate() {
      return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
      this.openingDate = openingDate;
    }

    public LocalDate getLastInterestDate() {
      return lastInterestDate;
    }

    public void setLastInterestDate(LocalDate lastInterestDate) {
      this.lastInterestDate = lastInterestDate;
    }

    public BigDecimal getInterestRate() {
      return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
      this.interestRate = interestRate;
    }

    public Boolean getIsActive() {
      return isActive;
    }

    public void setIsActive(Boolean isActive) {
      this.isActive = isActive;
    }

    public Boolean getIsFrozen() {
      return isFrozen;
    }

    public void setIsFrozen(Boolean isFrozen) {
      this.isFrozen = isFrozen;
    }

    // Manual builder
    public static AccountStatisticsBuilder builder() {
      return new AccountStatisticsBuilder();
    }

    public static class AccountStatisticsBuilder {
      private Long accountId;
      private String accountNumber;
      private BigDecimal currentBalance;
      private AccountType accountType;
      private String accountName;
      private LocalDate openingDate;
      private LocalDate lastInterestDate;
      private BigDecimal interestRate;
      private Boolean isActive;
      private Boolean isFrozen;

      public AccountStatisticsBuilder accountId(Long accountId) {
        this.accountId = accountId;
        return this;
      }

      public AccountStatisticsBuilder accountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
      }

      public AccountStatisticsBuilder currentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
        return this;
      }

      public AccountStatisticsBuilder accountType(AccountType accountType) {
        this.accountType = accountType;
        return this;
      }

      public AccountStatisticsBuilder accountName(String accountName) {
        this.accountName = accountName;
        return this;
      }

      public AccountStatisticsBuilder openingDate(LocalDate openingDate) {
        this.openingDate = openingDate;
        return this;
      }

      public AccountStatisticsBuilder lastInterestDate(LocalDate lastInterestDate) {
        this.lastInterestDate = lastInterestDate;
        return this;
      }

      public AccountStatisticsBuilder interestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
        return this;
      }

      public AccountStatisticsBuilder isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
      }

      public AccountStatisticsBuilder isFrozen(Boolean isFrozen) {
        this.isFrozen = isFrozen;
        return this;
      }

      public AccountStatistics build() {
        AccountStatistics statistics = new AccountStatistics();
        statistics.accountId = this.accountId;
        statistics.accountNumber = this.accountNumber;
        statistics.currentBalance = this.currentBalance;
        statistics.accountType = this.accountType;
        statistics.accountName = this.accountName;
        statistics.openingDate = this.openingDate;
        statistics.lastInterestDate = this.lastInterestDate;
        statistics.interestRate = this.interestRate;
        statistics.isActive = this.isActive;
        statistics.isFrozen = this.isFrozen;
        return statistics;
      }
    }
  }

  public SavingStatistics getSavingStatistics() {
    BigDecimal totalSavings = savingRepository.sumTotalSavings();
    long totalAccounts = savingRepository.count();

    return SavingStatistics.builder()
        .totalSavings(totalSavings)
        .totalAccounts(totalAccounts)
        .build();
  }

  @lombok.Data
  @lombok.Builder
  public static class SavingStatistics {
    private BigDecimal totalSavings;
    private long totalAccounts;
  }
}
