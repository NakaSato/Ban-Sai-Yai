package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.*;
import com.bansaiyai.bansaiyai.entity.enums.AccountType;
import com.bansaiyai.bansaiyai.entity.enums.ApprovalStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import com.bansaiyai.bansaiyai.entity.enums.TransactionType;
import com.bansaiyai.bansaiyai.entity.User.Role;
import com.bansaiyai.bansaiyai.entity.User.UserStatus;
import com.bansaiyai.bansaiyai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeederService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final SavingRepository savingRepository;
    private final SavingTransactionRepository savingTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String seedDemoData() {
        if (memberRepository.count() > 10) {
            return "Database already seems populated. Skipping seed.";
        }

        log.info("Starting Demo Data Seed...");

        seedUsers();
        seedAccounts();
        seedFiscalPeriods();
        seedMembersAndTransactions();

        log.info("Seeding Completed.");
        return "Seeded Users, Accounts, Periods, Members, Savings, and Transactions.";
    }

    private void seedUsers() {
        if (userRepository.count() > 0)
            return;

        createUser("secretary", "sec123", Role.SECRETARY, "Secretary", "User");
        createUser("president", "pres123", Role.PRESIDENT, "President", "User");
        createUser("officer", "off123", Role.OFFICER, "Officer", "User");
    }

    private void createUser(String username, String password, Role role, String firstName, String lastName) {
        if (userRepository.findByUsername(username).isPresent())
            return;

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .firstName(firstName)
                .lastName(lastName)
                .email(username + "@example.com")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .build();
        userRepository.save(user);
    }

    private void seedAccounts() {
        if (accountRepository.count() > 0)
            return;

        createAccount("10100", "Cash on Hand", "ASSET", null);
        createAccount("10200", "Bank Deposits", "ASSET", null);
        createAccount("10300", "Loans Receivable", "ASSET", null);
        createAccount("20100", "Member Savings", "LIABILITY", null);
        createAccount("20200", "Share Capital", "EQUITY", null);
        createAccount("30100", "Retained Earnings", "EQUITY", null);
        createAccount("40100", "Interest Income", "INCOME", null);
        createAccount("40200", "Fee Income", "INCOME", null);
        createAccount("50100", "Office Expense", "EXPENSE", null);
    }

    private void createAccount(String code, String name, String category, String parent) {
        Account account = Account.builder()
                .code(code)
                .name(name)
                .category(category)
                .parentCode(parent)
                .description("Default " + name)
                .build();
        accountRepository.save(account);
    }

    private void seedFiscalPeriods() {
        if (fiscalPeriodRepository.count() > 0)
            return;

        int year = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        for (int m = 1; m <= 12; m++) {
            String status = m < currentMonth ? "CLOSED" : "OPEN";
            if (m == currentMonth)
                status = "OPEN";

            FiscalPeriod period = FiscalPeriod.builder()
                    .month(m)
                    .year(year)
                    .status(status)
                    .build();
            if ("CLOSED".equals(status)) {
                period.setClosedAt(LocalDateTime.of(year, m, 28, 23, 59));
                period.setClosedBy("system");
            }
            fiscalPeriodRepository.save(period);
        }
    }

    private void seedMembersAndTransactions() {
        Random rand = new Random();
        User creator = userRepository.findByUsername("president").orElse(null);

        for (int i = 1; i <= 20; i++) {
            String memberId = String.format("DEMO%03d", i);
            Member m = Member.builder()
                    .memberId(memberId)
                    .name("Demo Member " + i)
                    .isActive(true)
                    .registrationDate(LocalDate.now().minusMonths(rand.nextInt(24) + 1))
                    .shareCapital(new BigDecimal("1000")) // Initial share
                    .build();
            m = memberRepository.save(m);

            // Create Saving Account
            SavingAccount saving = SavingAccount.builder()
                    .member(m)
                    .accountNumber("SA-" + memberId)
                    .accountType(AccountType.SAVINGS)
                    .accountName(m.getName() + " Savings")
                    .balance(BigDecimal.ZERO)
                    .availableBalance(BigDecimal.ZERO)
                    .interestRate(new BigDecimal("0.50"))
                    .openingDate(m.getRegistrationDate())
                    .isActive(true)
                    .build();
            saving = savingRepository.save(saving);

            // Seed Savings Transactions (Deposits)
            for (int t = 0; t < 5; t++) {
                BigDecimal amount = new BigDecimal(rand.nextInt(5000) + 500);
                SavingTransaction txn = new SavingTransaction();
                txn.setSavingAccount(saving);
                txn.setTransactionType(TransactionType.DEPOSIT);
                txn.setAmount(amount);
                txn.setTransactionDate(LocalDate.now().minusMonths(rand.nextInt(6)));
                txn.setTransactionNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8));
                txn.setApprovalStatus(ApprovalStatus.APPROVED);
                txn.setCreatorUser(creator);

                saving.setBalance(saving.getBalance().add(amount));
                saving.setAvailableBalance(saving.getAvailableBalance().add(amount));

                savingTransactionRepository.save(txn);
            }
            savingRepository.save(saving);

            // Create Loan
            if (i <= 10) {
                createLoanAndPayments(m, creator);
            }
        }
    }

    private void createLoanAndPayments(Member member, User creator) {
        Loan loan = Loan.builder()
                .member(member)
                .uuid(UUID.randomUUID())
                .loanNumber("LN-" + UUID.randomUUID().toString().substring(0, 8))
                .loanType(LoanType.PERSONAL)
                .principalAmount(new BigDecimal("20000"))
                .outstandingBalance(new BigDecimal("20000"))
                .interestRate(new BigDecimal("12.0"))
                .termMonths(12)
                .startDate(LocalDate.now().minusMonths(4))
                .maturityDate(LocalDate.now().plusMonths(8))
                .status(LoanStatus.ACTIVE)
                .purpose("Demo Loan")
                .build();
        loan = loanRepository.save(loan);

        // Seed Payments
        BigDecimal paymentAmount = new BigDecimal("2000");
        BigDecimal interestPart = new BigDecimal("200");
        BigDecimal principalPart = new BigDecimal("1800");

        for (int p = 0; p < 3; p++) {
            Payment payment = new Payment();
            payment.setLoan(loan);
            payment.setMember(member);
            payment.setPaymentType(PaymentType.LOAN_PRINCIPAL); // Simplified
            payment.setAmount(paymentAmount);
            payment.setPrincipalAmount(principalPart);
            payment.setInterestAmount(interestPart);
            payment.setPaymentDate(LocalDate.now().minusMonths(3 - p));
            payment.setPaymentNumber("PAY-" + UUID.randomUUID().toString().substring(0, 8));
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setApprovalStatus(ApprovalStatus.APPROVED);
            payment.setCreatorUser(creator);

            paymentRepository.save(payment);

            loan.setOutstandingBalance(loan.getOutstandingBalance().subtract(principalPart));
            loan.setPaidPrincipal(
                    (loan.getPaidPrincipal() == null ? BigDecimal.ZERO : loan.getPaidPrincipal()).add(principalPart));
            loan.setPaidInterest(
                    (loan.getPaidInterest() == null ? BigDecimal.ZERO : loan.getPaidInterest()).add(interestPart));
        }
        loanRepository.save(loan);
    }
}
