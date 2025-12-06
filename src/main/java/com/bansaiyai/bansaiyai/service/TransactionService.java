package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.DepositRequest;
import com.bansaiyai.bansaiyai.dto.LoanPaymentRequest;
import com.bansaiyai.bansaiyai.dto.TransactionResponse;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.SavingAccount;
import com.bansaiyai.bansaiyai.entity.SavingTransaction;
import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.entity.enums.TransactionType;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.ApprovalStatus;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.SavingRepository;
import com.bansaiyai.bansaiyai.repository.SavingTransactionRepository;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import com.bansaiyai.bansaiyai.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

        private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

        private final MemberRepository memberRepository;
        private final SavingRepository savingRepository;
        private final SavingTransactionRepository savingTransactionRepository;
        private final LoanRepository loanRepository;
        private final PaymentRepository paymentRepository;
        private final DashboardService dashboardService;
        private final AuditService auditService;

        public TransactionService(
                        MemberRepository memberRepository,
                        SavingRepository savingRepository,
                        SavingTransactionRepository savingTransactionRepository,
                        LoanRepository loanRepository,
                        PaymentRepository paymentRepository,
                        DashboardService dashboardService,
                        AuditService auditService) {
                this.memberRepository = memberRepository;
                this.savingRepository = savingRepository;
                this.savingTransactionRepository = savingTransactionRepository;
                this.loanRepository = loanRepository;
                this.paymentRepository = paymentRepository;
                this.dashboardService = dashboardService;
                this.auditService = auditService;
        }

        /**
         * Process a deposit transaction
         */
        @Transactional
        public TransactionResponse processDeposit(DepositRequest request) {
                try {
                        // Validate fiscal period is open
                        var fiscalPeriod = dashboardService.getCurrentFiscalPeriod();
                        if ("CLOSED".equals(fiscalPeriod.getStatus())) {
                                return new TransactionResponse(
                                                null,
                                                null,
                                                "DEPOSIT",
                                                request.getAmount(),
                                                LocalDateTime.now(),
                                                "FAILED",
                                                "Cannot process transaction: Fiscal period is closed");
                        }

                        // Validate member exists
                        Member member = memberRepository.findById(request.getMemberId())
                                        .orElseThrow(() -> new RuntimeException("Member not found"));

                        // Get or create member's savings account
                        List<SavingAccount> accounts = savingRepository.findByMemberIdAndIsActive(
                                        request.getMemberId(), true);

                        SavingAccount savingAccount;
                        if (accounts.isEmpty()) {
                                // Create new savings account if none exists
                                savingAccount = SavingAccount.builder()
                                                .member(member)
                                                .accountType(com.bansaiyai.bansaiyai.entity.enums.AccountType.SAVINGS)
                                                .accountName(member.getName() + " - Savings")
                                                .balance(BigDecimal.ZERO)
                                                .availableBalance(BigDecimal.ZERO)
                                                .interestRate(new BigDecimal("2.5"))
                                                .openingDate(LocalDate.now())
                                                .isActive(true)
                                                .isFrozen(false)
                                                .build();
                                savingAccount = savingRepository.save(savingAccount);
                        } else {
                                savingAccount = accounts.get(0);
                        }

                        // Create transaction
                        SavingTransaction transaction = new SavingTransaction();
                        transaction.setSavingAccount(savingAccount);
                        transaction.setTransactionType(TransactionType.DEPOSIT);
                        transaction.setAmount(request.getAmount());
                        transaction.setDescription(request.getNotes() != null ? request.getNotes() : "Deposit");
                        transaction.setTransactionDate(LocalDate.now());
                        transaction.setTransactionNumber(generateTransactionNumber());
                        transaction.setIsReversed(false);

                        // Update account balance
                        BigDecimal oldBalance = savingAccount.getBalance();
                        BigDecimal newBalance = oldBalance.add(request.getAmount());
                        transaction.setBalanceBefore(oldBalance);
                        transaction.setBalanceAfter(newBalance);
                        savingAccount.setBalance(newBalance);
                        savingAccount.setAvailableBalance(newBalance);

                        // Save transaction and account
                        transaction = savingTransactionRepository.save(transaction);
                        savingRepository.save(savingAccount);

                        return new TransactionResponse(
                                        transaction.getId(),
                                        transaction.getTransactionNumber(),
                                        "DEPOSIT",
                                        request.getAmount(),
                                        LocalDateTime.now(),
                                        "SUCCESS",
                                        "Deposit processed successfully");

                } catch (Exception e) {
                        log.error("Error processing deposit: {}", e.getMessage(), e);
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "DEPOSIT",
                                        request.getAmount(),
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Error processing deposit: " + e.getMessage());
                }
        }

        /**
         * Process a loan payment transaction
         */
        @Transactional
        public TransactionResponse processLoanPayment(LoanPaymentRequest request) {
                try {
                        // Validate fiscal period is open
                        var fiscalPeriod = dashboardService.getCurrentFiscalPeriod();
                        if ("CLOSED".equals(fiscalPeriod.getStatus())) {
                                return new TransactionResponse(
                                                null,
                                                null,
                                                "LOAN_PAYMENT",
                                                request.getPrincipalAmount().add(request.getInterestAmount()),
                                                LocalDateTime.now(),
                                                "FAILED",
                                                "Cannot process transaction: Fiscal period is closed");
                        }

                        // Validate member and loan exist
                        Member member = memberRepository.findById(request.getMemberId())
                                        .orElseThrow(() -> new RuntimeException("Member not found"));

                        Loan loan = loanRepository.findById(request.getLoanId())
                                        .orElseThrow(() -> new RuntimeException("Loan not found"));

                        // Validate loan belongs to member
                        if (!loan.getMember().getId().equals(request.getMemberId())) {
                                throw new RuntimeException("Loan does not belong to this member");
                        }

                        // Calculate total payment amount
                        BigDecimal totalAmount = request.getPrincipalAmount()
                                        .add(request.getInterestAmount())
                                        .add(request.getFineAmount() != null ? request.getFineAmount()
                                                        : BigDecimal.ZERO);

                        // Create payment record
                        Payment payment = new Payment();
                        payment.setLoan(loan);
                        payment.setMember(member);
                        payment.setPaymentType(PaymentType.LOAN_PRINCIPAL);
                        payment.setAmount(totalAmount);
                        payment.setPrincipalAmount(request.getPrincipalAmount());
                        payment.setInterestAmount(request.getInterestAmount());
                        payment.setPenaltyAmount(
                                        request.getFineAmount() != null ? request.getFineAmount() : BigDecimal.ZERO);
                        payment.setPaymentStatus(PaymentStatus.COMPLETED);
                        payment.setPaymentDate(LocalDate.now());
                        payment.setDueDate(loan.getMaturityDate());
                        payment.setPaymentNumber(generatePaymentNumber());
                        payment.setNotes(request.getNotes());

                        // Update loan balances
                        BigDecimal currentPaidPrincipal = loan.getPaidPrincipal() != null ? loan.getPaidPrincipal()
                                        : BigDecimal.ZERO;
                        BigDecimal currentPaidInterest = loan.getPaidInterest() != null ? loan.getPaidInterest()
                                        : BigDecimal.ZERO;
                        BigDecimal currentPenalty = loan.getPenaltyAmount() != null ? loan.getPenaltyAmount()
                                        : BigDecimal.ZERO;

                        loan.setPaidPrincipal(currentPaidPrincipal.add(request.getPrincipalAmount()));
                        loan.setPaidInterest(currentPaidInterest.add(request.getInterestAmount()));
                        loan.setPenaltyAmount(
                                        currentPenalty.add(request.getFineAmount() != null ? request.getFineAmount()
                                                        : BigDecimal.ZERO));

                        // Update outstanding balance
                        BigDecimal newOutstanding = loan.getPrincipalAmount().subtract(loan.getPaidPrincipal());
                        loan.setOutstandingBalance(newOutstanding);

                        // Save payment and loan
                        payment = paymentRepository.save(payment);
                        loanRepository.save(loan);

                        return new TransactionResponse(
                                        payment.getId(),
                                        payment.getPaymentNumber(),
                                        "LOAN_PAYMENT",
                                        totalAmount,
                                        LocalDateTime.now(),
                                        "SUCCESS",
                                        "Loan payment processed successfully");

                } catch (Exception e) {
                        log.error("Error processing loan payment: {}", e.getMessage(), e);
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "LOAN_PAYMENT",
                                        BigDecimal.ZERO,
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Error processing loan payment: " + e.getMessage());
                }
        }

        /**
         * Calculate minimum interest due based on loan type rate
         */
        public BigDecimal calculateMinimumInterest(Long loanId) {
                try {
                        Loan loan = loanRepository.findById(loanId)
                                        .orElseThrow(() -> new RuntimeException("Loan not found"));

                        // Calculate interest based on outstanding balance and interest rate
                        BigDecimal outstandingBalance = loan.getOutstandingBalance();
                        BigDecimal annualRate = loan.getInterestRate();

                        // Calculate monthly interest: (principal * rate) / 12 / 100
                        BigDecimal monthlyInterest = outstandingBalance
                                        .multiply(annualRate)
                                        .divide(new BigDecimal("1200"), 2, java.math.RoundingMode.HALF_UP);

                        return monthlyInterest;

                } catch (Exception e) {
                        log.error("Error calculating minimum interest: {}", e.getMessage());
                        return BigDecimal.ZERO;
                }
        }

        private String generateTransactionNumber() {
                return "TXN-" + LocalDate.now().toString().replace("-", "") + "-" +
                                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        private String generatePaymentNumber() {
                return "PAY-" + LocalDate.now().toString().replace("-", "") + "-" +
                                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        /**
         * Process a deposit transaction with creator tracking
         */
        @Transactional
        public TransactionResponse processDepositWithCreator(DepositRequest request, User creator) {
                try {
                        // Validate fiscal period is open
                        var fiscalPeriod = dashboardService.getCurrentFiscalPeriod();
                        if ("CLOSED".equals(fiscalPeriod.getStatus())) {
                                return new TransactionResponse(
                                                null,
                                                null,
                                                "DEPOSIT",
                                                request.getAmount(),
                                                LocalDateTime.now(),
                                                "FAILED",
                                                "Cannot process transaction: Fiscal period is closed");
                        }

                        // Validate member exists
                        Member member = memberRepository.findById(request.getMemberId())
                                        .orElseThrow(() -> new RuntimeException("Member not found"));

                        // Get or create member's savings account
                        List<SavingAccount> accounts = savingRepository.findByMemberIdAndIsActive(
                                        request.getMemberId(), true);

                        SavingAccount savingAccount;
                        if (accounts.isEmpty()) {
                                // Create new savings account if none exists
                                savingAccount = SavingAccount.builder()
                                                .member(member)
                                                .accountType(com.bansaiyai.bansaiyai.entity.enums.AccountType.SAVINGS)
                                                .accountName(member.getName() + " - Savings")
                                                .balance(BigDecimal.ZERO)
                                                .availableBalance(BigDecimal.ZERO)
                                                .interestRate(new BigDecimal("2.5"))
                                                .openingDate(LocalDate.now())
                                                .isActive(true)
                                                .isFrozen(false)
                                                .build();
                                savingAccount = savingRepository.save(savingAccount);
                        } else {
                                savingAccount = accounts.get(0);
                        }

                        // Create transaction with creator tracking
                        SavingTransaction transaction = new SavingTransaction();
                        transaction.setSavingAccount(savingAccount);
                        transaction.setTransactionType(TransactionType.DEPOSIT);
                        transaction.setAmount(request.getAmount());
                        transaction.setDescription(request.getNotes() != null ? request.getNotes() : "Deposit");
                        transaction.setTransactionDate(LocalDate.now());
                        transaction.setTransactionNumber(generateTransactionNumber());
                        transaction.setIsReversed(false);
                        transaction.setCreatorUser(creator);
                        transaction.setApprovalStatus(ApprovalStatus.PENDING);

                        // Update account balance
                        BigDecimal oldBalance = savingAccount.getBalance();
                        BigDecimal newBalance = oldBalance.add(request.getAmount());
                        transaction.setBalanceBefore(oldBalance);
                        transaction.setBalanceAfter(newBalance);
                        savingAccount.setBalance(newBalance);
                        savingAccount.setAvailableBalance(newBalance);

                        // Save transaction and account
                        transaction = savingTransactionRepository.save(transaction);
                        savingRepository.save(savingAccount);

                        // Log the transaction creation
                        auditService.logAction(creator, "TRANSACTION_CREATE", "SavingTransaction",
                                        transaction.getId(), null, null);

                        return new TransactionResponse(
                                        transaction.getId(),
                                        transaction.getTransactionNumber(),
                                        "DEPOSIT",
                                        request.getAmount(),
                                        LocalDateTime.now(),
                                        "SUCCESS",
                                        "Deposit processed successfully");

                } catch (Exception e) {
                        log.error("Error processing deposit: {}", e.getMessage(), e);
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "DEPOSIT",
                                        request.getAmount(),
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Error processing deposit: " + e.getMessage());
                }
        }

        /**
         * Process a loan payment transaction with creator tracking
         */
        @Transactional
        public TransactionResponse processLoanPaymentWithCreator(LoanPaymentRequest request, User creator) {
                try {
                        // Validate fiscal period is open
                        var fiscalPeriod = dashboardService.getCurrentFiscalPeriod();
                        if ("CLOSED".equals(fiscalPeriod.getStatus())) {
                                return new TransactionResponse(
                                                null,
                                                null,
                                                "LOAN_PAYMENT",
                                                request.getPrincipalAmount().add(request.getInterestAmount()),
                                                LocalDateTime.now(),
                                                "FAILED",
                                                "Cannot process transaction: Fiscal period is closed");
                        }

                        // Validate member and loan exist
                        Member member = memberRepository.findById(request.getMemberId())
                                        .orElseThrow(() -> new RuntimeException("Member not found"));

                        Loan loan = loanRepository.findById(request.getLoanId())
                                        .orElseThrow(() -> new RuntimeException("Loan not found"));

                        // Validate loan belongs to member
                        if (!loan.getMember().getId().equals(request.getMemberId())) {
                                throw new RuntimeException("Loan does not belong to this member");
                        }

                        // Calculate total payment amount
                        BigDecimal totalAmount = request.getPrincipalAmount()
                                        .add(request.getInterestAmount())
                                        .add(request.getFineAmount() != null ? request.getFineAmount()
                                                        : BigDecimal.ZERO);

                        // Create payment record with creator tracking
                        Payment payment = new Payment();
                        payment.setLoan(loan);
                        payment.setMember(member);
                        payment.setPaymentType(PaymentType.LOAN_PRINCIPAL);
                        payment.setAmount(totalAmount);
                        payment.setPrincipalAmount(request.getPrincipalAmount());
                        payment.setInterestAmount(request.getInterestAmount());
                        payment.setPenaltyAmount(
                                        request.getFineAmount() != null ? request.getFineAmount() : BigDecimal.ZERO);
                        payment.setPaymentStatus(PaymentStatus.COMPLETED);
                        payment.setPaymentDate(LocalDate.now());
                        payment.setDueDate(loan.getMaturityDate());
                        payment.setPaymentNumber(generatePaymentNumber());
                        payment.setNotes(request.getNotes());
                        payment.setCreatorUser(creator);
                        payment.setApprovalStatus(ApprovalStatus.PENDING);

                        // Update loan balances
                        BigDecimal currentPaidPrincipal = loan.getPaidPrincipal() != null ? loan.getPaidPrincipal()
                                        : BigDecimal.ZERO;
                        BigDecimal currentPaidInterest = loan.getPaidInterest() != null ? loan.getPaidInterest()
                                        : BigDecimal.ZERO;
                        BigDecimal currentPenalty = loan.getPenaltyAmount() != null ? loan.getPenaltyAmount()
                                        : BigDecimal.ZERO;

                        loan.setPaidPrincipal(currentPaidPrincipal.add(request.getPrincipalAmount()));
                        loan.setPaidInterest(currentPaidInterest.add(request.getInterestAmount()));
                        loan.setPenaltyAmount(
                                        currentPenalty.add(request.getFineAmount() != null ? request.getFineAmount()
                                                        : BigDecimal.ZERO));

                        // Update outstanding balance
                        BigDecimal newOutstanding = loan.getPrincipalAmount().subtract(loan.getPaidPrincipal());
                        loan.setOutstandingBalance(newOutstanding);

                        // Save payment and loan
                        payment = paymentRepository.save(payment);
                        loanRepository.save(loan);

                        // Log the payment creation
                        auditService.logAction(creator, "PAYMENT_CREATE", "Payment",
                                        payment.getId(), null, null);

                        return new TransactionResponse(
                                        payment.getId(),
                                        payment.getPaymentNumber(),
                                        "LOAN_PAYMENT",
                                        totalAmount,
                                        LocalDateTime.now(),
                                        "SUCCESS",
                                        "Loan payment processed successfully");

                } catch (Exception e) {
                        log.error("Error processing loan payment: {}", e.getMessage(), e);
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "LOAN_PAYMENT",
                                        BigDecimal.ZERO,
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Error processing loan payment: " + e.getMessage());
                }
        }

        /**
         * Check if a transaction can be voided (24-hour rule)
         */
        public boolean canVoidTransaction(Long transactionId, User requestingUser) {
                SavingTransaction transaction = savingTransactionRepository.findById(transactionId)
                                .orElseThrow(() -> new RuntimeException("Transaction not found"));

                // Check if transaction is already voided
                if (transaction.getVoidedAt() != null) {
                        return false;
                }

                // Check if transaction is within 24 hours
                LocalDateTime transactionTime = transaction.getCreatedAt() != null ? transaction.getCreatedAt()
                                : LocalDateTime.now();
                LocalDateTime now = LocalDateTime.now();
                long hoursBetween = ChronoUnit.HOURS.between(transactionTime, now);

                if (hoursBetween > 24) {
                        return false;
                }

                // Check if user has permission to void transactions
                if (!requestingUser.getPermissions().contains("transaction.void")) {
                        return false;
                }

                return true;
        }

        /**
         * Void a transaction with audit logging
         */
        @Transactional
        public TransactionResponse voidTransaction(Long transactionId, User requestingUser, String reason) {
                try {
                        SavingTransaction transaction = savingTransactionRepository.findById(transactionId)
                                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

                        // Check if transaction can be voided
                        if (!canVoidTransaction(transactionId, requestingUser)) {
                                return new TransactionResponse(
                                                null,
                                                null,
                                                "VOID",
                                                BigDecimal.ZERO,
                                                LocalDateTime.now(),
                                                "FAILED",
                                                "Transaction cannot be voided");
                        }

                        // Void the transaction
                        transaction.setIsReversed(true);
                        transaction.setReversedAt(LocalDateTime.now());
                        transaction.setReversedBy(requestingUser.getUsername());
                        transaction.setReversalReason(reason);
                        transaction.setVoidedAt(LocalDateTime.now());

                        // Reverse the account balance
                        SavingAccount account = transaction.getSavingAccount();
                        BigDecimal currentBalance = account.getBalance();
                        BigDecimal transactionEffect = transaction.getNetEffect();
                        account.setBalance(currentBalance.subtract(transactionEffect));
                        account.setAvailableBalance(account.getBalance());

                        // Save changes
                        savingTransactionRepository.save(transaction);
                        savingRepository.save(account);

                        // Log the void operation
                        auditService.logAction(requestingUser, "TRANSACTION_VOID", "SavingTransaction",
                                        transactionId, null, null);

                        return new TransactionResponse(
                                        transaction.getId(),
                                        transaction.getTransactionNumber(),
                                        "VOID",
                                        transaction.getAmount(),
                                        LocalDateTime.now(),
                                        "SUCCESS",
                                        "Transaction voided successfully");

                } catch (Exception e) {
                        log.error("Error voiding transaction: {}", e.getMessage(), e);
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "VOID",
                                        BigDecimal.ZERO,
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Error voiding transaction: " + e.getMessage());
                }
        }

        /**
         * Get pending transactions for approval queue
         */
        @Transactional(readOnly = true)
        public List<SavingTransaction> getPendingTransactions() {
                return savingTransactionRepository.findByApprovalStatus(ApprovalStatus.PENDING);
        }

        /**
         * Get pending payments for approval queue
         */
        @Transactional(readOnly = true)
        public List<Payment> getPendingPayments() {
                return paymentRepository.findByApprovalStatus(ApprovalStatus.PENDING);
        }

        /**
         * Approve a transaction with self-approval check
         */
        @Transactional
        public TransactionResponse approveTransaction(Long transactionId, User approver) {
                try {
                        SavingTransaction transaction = savingTransactionRepository.findById(transactionId)
                                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

                        // Check if transaction is already approved
                        if (transaction.getApprovalStatus() != ApprovalStatus.PENDING) {
                                return new TransactionResponse(
                                                null,
                                                null,
                                                "APPROVE",
                                                BigDecimal.ZERO,
                                                LocalDateTime.now(),
                                                "FAILED",
                                                "Transaction is not pending approval");
                        }

                        // Check for self-approval (SoD enforcement)
                        if (transaction.getCreatorUser() != null &&
                                        transaction.getCreatorUser().getId().equals(approver.getId())) {
                                throw new AccessDeniedException("Self-approval is not allowed");
                        }

                        // Approve the transaction
                        transaction.setApprovalStatus(ApprovalStatus.APPROVED);
                        transaction.setApproverUser(approver);

                        savingTransactionRepository.save(transaction);

                        // Log the approval
                        auditService.logAction(approver, "TRANSACTION_APPROVE", "SavingTransaction",
                                        transactionId, null, null);

                        return new TransactionResponse(
                                        transaction.getId(),
                                        transaction.getTransactionNumber(),
                                        "APPROVE",
                                        transaction.getAmount(),
                                        LocalDateTime.now(),
                                        "SUCCESS",
                                        "Transaction approved successfully");

                } catch (AccessDeniedException e) {
                        log.warn("Self-approval attempt denied for transaction {}: {}",
                                        transactionId, e.getMessage());
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "APPROVE",
                                        BigDecimal.ZERO,
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Self-approval is not allowed");

                } catch (Exception e) {
                        log.error("Error approving transaction: {}", e.getMessage(), e);
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "APPROVE",
                                        BigDecimal.ZERO,
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Error approving transaction: " + e.getMessage());
                }
        }

        /**
         * Approve a payment with self-approval check
         */
        @Transactional
        public TransactionResponse approvePayment(Long paymentId, User approver) {
                try {
                        Payment payment = paymentRepository.findById(paymentId)
                                        .orElseThrow(() -> new RuntimeException("Payment not found"));

                        // Check if payment is already approved
                        if (payment.getApprovalStatus() != ApprovalStatus.PENDING) {
                                return new TransactionResponse(
                                                null,
                                                null,
                                                "APPROVE",
                                                BigDecimal.ZERO,
                                                LocalDateTime.now(),
                                                "FAILED",
                                                "Payment is not pending approval");
                        }

                        // Check for self-approval (SoD enforcement)
                        if (payment.getCreatorUser() != null &&
                                        payment.getCreatorUser().getId().equals(approver.getId())) {
                                throw new AccessDeniedException("Self-approval is not allowed");
                        }

                        // Approve the payment
                        payment.setApprovalStatus(ApprovalStatus.APPROVED);
                        payment.setApproverUser(approver);

                        paymentRepository.save(payment);

                        // Log the approval
                        auditService.logAction(approver, "PAYMENT_APPROVE", "Payment",
                                        paymentId, null, null);

                        return new TransactionResponse(
                                        payment.getId(),
                                        payment.getPaymentNumber(),
                                        "APPROVE",
                                        payment.getAmount(),
                                        LocalDateTime.now(),
                                        "SUCCESS",
                                        "Payment approved successfully");

                } catch (AccessDeniedException e) {
                        log.warn("Self-approval attempt denied for payment {}: {}",
                                        paymentId, e.getMessage());
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "APPROVE",
                                        BigDecimal.ZERO,
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Self-approval is not allowed");

                } catch (Exception e) {
                        log.error("Error approving payment: {}", e.getMessage(), e);
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "APPROVE",
                                        BigDecimal.ZERO,
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Error approving payment: " + e.getMessage());
                }
        }

        /**
         * Reject a transaction
         */
        @Transactional
        public TransactionResponse rejectTransaction(Long transactionId, User approver, String reason) {
                try {
                        SavingTransaction transaction = savingTransactionRepository.findById(transactionId)
                                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

                        // Check if transaction is already processed
                        if (transaction.getApprovalStatus() != ApprovalStatus.PENDING) {
                                return new TransactionResponse(
                                                null,
                                                null,
                                                "REJECT",
                                                BigDecimal.ZERO,
                                                LocalDateTime.now(),
                                                "FAILED",
                                                "Transaction is not pending approval");
                        }

                        // Reject the transaction
                        transaction.setApprovalStatus(ApprovalStatus.REJECTED);
                        transaction.setApproverUser(approver);

                        savingTransactionRepository.save(transaction);

                        // Log the rejection
                        auditService.logAction(approver, "TRANSACTION_REJECT", "SavingTransaction",
                                        transactionId, null, null);

                        return new TransactionResponse(
                                        transaction.getId(),
                                        transaction.getTransactionNumber(),
                                        "REJECT",
                                        transaction.getAmount(),
                                        LocalDateTime.now(),
                                        "SUCCESS",
                                        "Transaction rejected: " + reason);

                } catch (Exception e) {
                        log.error("Error rejecting transaction: {}", e.getMessage(), e);
                        return new TransactionResponse(
                                        null,
                                        null,
                                        "REJECT",
                                        BigDecimal.ZERO,
                                        LocalDateTime.now(),
                                        "FAILED",
                                        "Error rejecting transaction: " + e.getMessage());
                }
        }
}
