package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.LoanBalance;
import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import com.bansaiyai.bansaiyai.repository.LoanBalanceRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.AccountingRepository;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountingService {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AccountingService.class);

        private final LoanRepository loanRepository;
        private final LoanBalanceRepository loanBalanceRepository;
        private final PaymentRepository paymentRepository;
        private final AccountingRepository accountingRepository;
        private final UserRepository userRepository;
        private final com.bansaiyai.bansaiyai.repository.AccountRepository accountRepository;
        private final com.bansaiyai.bansaiyai.repository.FiscalPeriodRepository fiscalPeriodRepository;
        private final com.bansaiyai.bansaiyai.repository.SavingAccountRepository savingAccountRepository;
        private final com.bansaiyai.bansaiyai.repository.SavingBalanceRepository savingBalanceRepository;
        private final AuditService auditService;

        /**
         * Execute monthly closing for a specific month and year.
         * This process snapshots the balance of all active loans.
         *
         * @param month The month to close (1-12)
         * @param year  The year to close
         * @return Summary string of the operation
         */
        @Transactional
        @PreAuthorize("hasRole('SECRETARY')")
        public String closeMonth(int month, int year, String username) {
                YearMonth targetMonth = YearMonth.of(year, month);
                LocalDate startDate = targetMonth.atDay(1);
                LocalDate endDate = targetMonth.atEndOfMonth();

                log.info("Starting monthly closing for {}/{}", month, year);

                // 0. Check if period is already closed
                if (fiscalPeriodRepository.findByMonthAndYear(month, year)
                                .map(fp -> "CLOSED".equals(fp.getStatus()))
                                .orElse(false)) {
                        throw new RuntimeException("Fiscal period " + month + "/" + year + " is already closed.");
                }

                // Validation: Check if Total Debits == Total Credits (Trial Balance)
                String periodStr = targetMonth.toString();
                BigDecimal totalDebits = accountingRepository.sumDebitsByFiscalPeriod(periodStr);
                BigDecimal totalCredits = accountingRepository.sumCreditsByFiscalPeriod(periodStr);

                // Allow small difference for rounding error (0.01)
                if (totalDebits.subtract(totalCredits).abs().compareTo(new BigDecimal("0.05")) > 0) {
                        log.error("Trial Balance Mismatch for {}: Debits={}, Credits={}", periodStr, totalDebits,
                                        totalCredits);
                        throw new com.bansaiyai.bansaiyai.exception.BusinessException(
                                        String.format("Cannot close month: Trial Balance mismatch. Debits (%.2f) != Credits (%.2f)",
                                                        totalDebits, totalCredits));
                }

                // Fetch all active loans (and Defaulted ones too as they still accrue/balances)
                List<Loan> activeLoans = loanRepository
                                .findByStatusIn(List.of(LoanStatus.ACTIVE, LoanStatus.DEFAULTED));

                int processedLoans = 0;
                BigDecimal totalLoanBalance = BigDecimal.ZERO;

                for (Loan loan : activeLoans) {
                        // Check if already closed for this month
                        Optional<LoanBalance> existingBalance = loanBalanceRepository.findByLoanIdAndBalanceDate(
                                        loan.getId(),
                                        endDate);
                        if (existingBalance.isPresent()) {
                                log.warn("Loan {} already closed for {}/{}", loan.getLoanNumber(), month, year);
                                continue;
                        }

                        processLoanClosing(loan, startDate, endDate);
                        processedLoans++;

                        // Add to total for summary (using current outstanding)
                        totalLoanBalance = totalLoanBalance.add(loan.getOutstandingBalance());
                }

                // Process Savings Snapshots
                List<com.bansaiyai.bansaiyai.entity.SavingAccount> activeSavings = savingAccountRepository
                                .findByIsActiveTrue();
                int processedSavings = 0;
                BigDecimal totalSavingBalance = BigDecimal.ZERO;

                for (com.bansaiyai.bansaiyai.entity.SavingAccount account : activeSavings) {
                        // Check if snapshot exists
                        if (savingBalanceRepository.existsBySavingAccountIdAndBalanceDate(account.getId(), endDate)) {
                                continue;
                        }

                        // Creates snapshot
                        com.bansaiyai.bansaiyai.entity.SavingBalance snapshot = com.bansaiyai.bansaiyai.entity.SavingBalance
                                        .builder()
                                        .savingAccount(account)
                                        .balanceDate(endDate)
                                        .isMonthEnd(true)
                                        .openingBalance(account.getBalance()) // Simplified for snapshot
                                        .closingBalance(account.getBalance())
                                        .totalDeposits(BigDecimal.ZERO)
                                        .totalWithdrawals(BigDecimal.ZERO)
                                        .interestEarned(BigDecimal.ZERO) // Interest calculation logic should be
                                                                         // separate or accumulated
                                        .feesCharged(BigDecimal.ZERO)
                                        .daysBelowMinimum(0)
                                        .interestRate(account.getInterestRate())
                                        .build();

                        savingBalanceRepository.save(snapshot);
                        processedSavings++;
                        totalSavingBalance = totalSavingBalance.add(account.getBalance());
                }

                // Lock the Period
                com.bansaiyai.bansaiyai.entity.FiscalPeriod period = fiscalPeriodRepository
                                .findByMonthAndYear(month, year)
                                .orElse(com.bansaiyai.bansaiyai.entity.FiscalPeriod.builder()
                                                .month(month)
                                                .year(year)
                                                .build());
                period.setStatus("CLOSED");
                period.setClosedAt(java.time.LocalDateTime.now());
                period.setClosedBy(username);
                fiscalPeriodRepository.save(period);

                log.info("Monthly closing completed. Loans: {}, Savings: {}", processedLoans, processedSavings);

                // Audit Log
                try {
                        User user = userRepository.findByUsername(username).orElse(null);
                        if (user != null) {
                                auditService.logAction(user, "MONTH_CLOSE", "Accounting", null, null,
                                                "Month: " + month + ", Year: " + year + ", Loans: " + processedLoans
                                                                + ", Savings: " + processedSavings);
                        }
                } catch (Exception e) {
                        log.error("Failed to audit month close", e);
                }

                return String.format("Closed %d/%d. Processed %d Loans (฿%,.2f), %d Savings (฿%,.2f). Period Locked.",
                                month, year, processedLoans, totalLoanBalance, processedSavings, totalSavingBalance);
        }

        private void processLoanClosing(Loan loan, LocalDate startDate, LocalDate endDate) {
                // 1. Get Opening Balance (Previous month's closing or Loan creation details)
                BigDecimal openingPrincipal = loan.getOutstandingBalance(); // This is current live balance, but for
                                                                            // snapshot we
                                                                            // might want to reconstruct?
                // ideally we trust the live Loan entity is up to date with payments.
                // But strictly for "forward", we want: Start Balance - Payments + Interest =
                // End Balance.

                // Let's get the payments for THIS month to analyze what happened
                List<Payment> paymentsThisMonth = paymentRepository.findLoanPaymentsByLoan(
                                loan.getId(),
                                List.of(PaymentType.LOAN_REPAYMENT, PaymentType.LOAN_CLOSURE)).stream()
                                .filter(p -> !p.getPaymentDate().isBefore(startDate)
                                                && !p.getPaymentDate().isAfter(endDate))
                                .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                                .toList();

                BigDecimal principalPaid = paymentsThisMonth.stream()
                                .map(p -> p.getPrincipalAmount() != null ? p.getPrincipalAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal interestPaid = paymentsThisMonth.stream()
                                .map(p -> p.getInterestAmount() != null ? p.getInterestAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal penaltyPaid = paymentsThisMonth.stream()
                                .map(p -> p.getPenaltyAmount() != null ? p.getPenaltyAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Calculate Interest Accrued for this month
                // Formula: Principal * Rate * Days / 365
                // Ideally we use the balance at start of month or average daily balance.
                // For simplicity/standard practice in this context: Use current principal (if
                // monthly reducing) or opening.
                // Using "Reducing Balance" logic typically implies interest on remaining
                // balance.
                // We will use the Loan entity's calculation if available, or manual.

                // Simple Daily Interest Calculation
                long daysInMonth = targetMonthDays(startDate);
                BigDecimal dailyRate = loan.getInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                                .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);

                // We use outstanding balance. Note: If payments happened mid-month, this is an
                // approximation unless we do daily steps.
                // Assuming simple month-end calculation on current outstanding:
                BigDecimal interestAccrued = loan.getOutstandingBalance()
                                .multiply(dailyRate)
                                .multiply(BigDecimal.valueOf(daysInMonth))
                                .setScale(2, RoundingMode.HALF_UP);

                // Create Snapshot
                LoanBalance balance = LoanBalance.builder()
                                .loan(loan)
                                .balanceDate(endDate)
                                .openingPrincipal(loan.getOutstandingBalance().add(principalPaid)) // Reverse engineer
                                                                                                   // opening from
                                                                                                   // current + paid?
                                // Actually, if we trust Loan.outstandingBalance is REAL-TIME, then Closing
                                // Principal = Loan.outstandingBalance.
                                // Opening Principal = Closing + PrincipalPaid.
                                .closingPrincipal(loan.getOutstandingBalance())

                                .principalPaid(principalPaid)
                                .interestPaid(interestPaid)
                                .penaltyPaid(penaltyPaid)

                                .interestAccrued(interestAccrued)
                                // .penaltyAccrued() // Complexity: penalty logic would go here

                                .outstandingBalance(loan.getOutstandingBalance())
                                .paymentCount(paymentsThisMonth.size())
                                .isCurrent(true) // Latest snapshot
                                .build();

                // Validate
                if (balance.getOutstandingBalance().compareTo(BigDecimal.ZERO) < 0) {
                        // Should not happen unless overpaid seriously.
                        log.error("Loan {} has negative balance: {}", loan.getLoanNumber(),
                                        balance.getOutstandingBalance());
                }

                loanBalanceRepository.save(balance);
        }

        private long targetMonthDays(LocalDate date) {
                return date.lengthOfMonth();
        }

        /**
         * Check for loans that have passed maturity date and have outstanding balance.
         */
        /**
         * Check for loans that have passed maturity date and have outstanding balance.
         */
        @Transactional
        public void checkAndFlagOverdueLoans() {
                // Find active loans that are past their maturity date
                List<Loan> overdueLoans = loanRepository.findOverdueLoansByStatus(LoanStatus.ACTIVE);

                int updatedCount = 0;
                for (Loan loan : overdueLoans) {
                        // Double check outstanding balance just in case
                        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
                                log.warn("Loan {} is overdue. Maturity Date: {}. Marking as DEFAULTED.",
                                                loan.getLoanNumber(), loan.getMaturityDate());

                                loan.setStatus(LoanStatus.DEFAULTED);
                                loanRepository.save(loan);
                                updatedCount++;

                                // Optional: Create an Audit Log for system action?
                                // System user might not exist in repository easily here without specific
                                // lookup,
                                // but we could log it if we fetched a system user. For now, Logger is
                                // sufficient.
                        }
                }

                if (updatedCount > 0) {
                        log.info("Overdue Check: Flagged {} loans as DEFAULTED.", updatedCount);
                } else {
                        log.info("Overdue Check: No new overdue loans found.");
                }
        }

        /**
         * Create a general journal entry (Income/Expense).
         */
        @Transactional
        public com.bansaiyai.bansaiyai.entity.AccountingEntry createEntry(
                        com.bansaiyai.bansaiyai.dto.JournalEntryRequest request, String username) {
                // Validation
                if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new RuntimeException("Amount must be positive");
                }

                // Create Entry
                com.bansaiyai.bansaiyai.entity.AccountingEntry entry = new com.bansaiyai.bansaiyai.entity.AccountingEntry();
                entry.setTransactionDate(
                                request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
                entry.setAccountCode(request.getAccountCode() != null ? request.getAccountCode() : "GEN");
                entry.setAccountName(request.getAccountName());
                entry.setDescription(request.getDescription());
                entry.setFiscalPeriod(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM").format(LocalDate.now()));
                entry.setReferenceType("GENERAL");
                entry.setReferenceNumber("JRN-" + System.currentTimeMillis());

                // Map Type to Debit/Credit
                if ("EXPENSE".equalsIgnoreCase(request.getType())) {
                        entry.setDebit(request.getAmount());
                        entry.setCredit(BigDecimal.ZERO);
                } else {
                        // INCOME or default
                        entry.setCredit(request.getAmount());
                        entry.setDebit(BigDecimal.ZERO);
                }

                // Audit Log
                try {
                        User user = userRepository.findByUsername(username).orElse(null);
                        if (user != null) {
                                auditService.logAction(user, "JOURNAL_ENTRY", "AccountingEntry", null, null,
                                                "Account: " + request.getAccountName() + ", Amt: " + request.getAmount()
                                                                + ", Type: " + request.getType());
                        }
                } catch (Exception e) {
                        log.error("Failed to audit journal entry", e);
                }

                return accountingRepository.save(entry);
        }

        /**
         * Confirm period end (Locking).
         */
        @Transactional
        public void confirmPeriod(int month, int year, String username) {
                // Logic to mark period as confirmed.
                // For MVP, we can just log it or update a Status flag in a FiscalPeriod entity
                // (if exists).
                // Based on specs: "Lock loan_forward and dividend".

                YearMonth targetMonth = YearMonth.of(year, month);
                LocalDate endDate = targetMonth.atEndOfMonth();

                // Lock LoanBalances
                List<LoanBalance> balances = loanBalanceRepository.findByBalanceDate(endDate);
                balances.forEach(b -> {
                        b.setIsVerified(true);
                        loanBalanceRepository.save(b);
                });

                log.info("Confirmed period {}/{}. Locked {} loan balances.", month, year, balances.size());

                // Audit Log
                try {
                        User user = userRepository.findByUsername(username).orElse(null);
                        if (user != null) {
                                auditService.logAction(user, "PERIOD_CONFIRM", "Accounting", null, null,
                                                "Confirmed Period: " + month + "/" + year);
                        }
                } catch (Exception e) {
                        log.error("Failed to audit period confirm", e);
                }
        }

        // ==========================================
        // Chart of Accounts Management
        // ==========================================

        @Transactional
        @PreAuthorize("hasRole('SECRETARY')")
        public com.bansaiyai.bansaiyai.entity.Account createAccount(com.bansaiyai.bansaiyai.entity.Account account) {
                if (accountRepository.existsById(account.getCode())) {
                        throw new RuntimeException("Account code already exists: " + account.getCode());
                }
                return accountRepository.save(account);
        }

        @Transactional
        @PreAuthorize("hasRole('SECRETARY')")
        public com.bansaiyai.bansaiyai.entity.Account updateAccount(String code,
                        com.bansaiyai.bansaiyai.entity.Account accountDetails) {
                com.bansaiyai.bansaiyai.entity.Account account = accountRepository.findById(code)
                                .orElseThrow(() -> new RuntimeException("Account not found: " + code));

                account.setName(accountDetails.getName());
                account.setCategory(accountDetails.getCategory());
                account.setParentCode(accountDetails.getParentCode());
                account.setDescription(accountDetails.getDescription());

                return accountRepository.save(account);
        }

        @Transactional
        @PreAuthorize("hasRole('SECRETARY')")
        public void deleteAccount(String code) {
                if (!accountRepository.existsById(code)) {
                        throw new RuntimeException("Account not found: " + code);
                }
                // Check if used in accounting entries?
                // For now, strict deletion might be risky if data exists.
                // Ideally check accountingRepository.existsByAccountCode(code).
                if (accountingRepository.existsByAccountCode(code)) {
                        throw new RuntimeException("Cannot delete account with existing transactions.");
                }

                accountRepository.deleteById(code);
        }

        public List<com.bansaiyai.bansaiyai.entity.Account> getAllAccounts() {
                return accountRepository.findAll();
        }

        /**
         * Get daily summary of accounting entries
         */
        public java.util.Map<String, BigDecimal> getDailySummary(LocalDate date) {
                java.util.Map<String, BigDecimal> summary = new java.util.HashMap<>();

                // Get all accounting entries for the date
                List<com.bansaiyai.bansaiyai.entity.AccountingEntry> entries = accountingRepository.findAll().stream()
                                .filter(e -> e.getTransactionDate() != null && e.getTransactionDate().equals(date))
                                .collect(java.util.stream.Collectors.toList());

                // Calculate total debits and credits
                BigDecimal totalDebits = entries.stream()
                                .map(e -> e.getDebit() != null ? e.getDebit() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalCredits = entries.stream()
                                .map(e -> e.getCredit() != null ? e.getCredit() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                summary.put("totalDebits", totalDebits);
                summary.put("totalCredits", totalCredits);
                summary.put("balance", totalDebits.subtract(totalCredits));
                summary.put("entryCount", BigDecimal.valueOf(entries.size()));

                return summary;
        }
}
