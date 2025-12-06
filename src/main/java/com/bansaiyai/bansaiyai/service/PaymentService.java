package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.PaymentRequest;
import com.bansaiyai.bansaiyai.dto.PaymentResponse;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.SavingAccount;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import com.bansaiyai.bansaiyai.repository.SavingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing payment operations.
 * Handles loan repayments, savings deposits, fee processing, and payment
 * reconciliation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final MemberRepository memberRepository;
  private final LoanRepository loanRepository;
  private final SavingRepository savingRepository;
  private final LoanService loanService;
  private final SavingService savingService;

  /**
   * Create a new payment
   */
  public PaymentResponse createPayment(PaymentRequest request, String createdBy) {
    log.info("Creating payment for member: {}, type: {}, amount: {}",
        request.getMemberId(), request.getPaymentType(), request.getAmount());

    // Validate member
    Member member = memberRepository.findById(request.getMemberId())
        .orElseThrow(() -> new RuntimeException("Member not found: " + request.getMemberId()));

    if (!member.getIsActive()) {
      throw new RuntimeException("Member is not active");
    }

    // Validate related entities based on payment type
    Loan loan = null;
    SavingAccount savingAccount = null;

    if (request.isLoanPayment()) {
      loan = loanRepository.findById(request.getLoanId())
          .orElseThrow(() -> new RuntimeException("Loan not found: " + request.getLoanId()));
    }

    if (request.isSavingsPayment()) {
      savingAccount = savingRepository.findById(request.getSavingAccountId())
          .orElseThrow(() -> new RuntimeException("Savings account not found: " + request.getSavingAccountId()));
    }

    // Create payment entity
    Payment payment = Payment.builder()
        .paymentNumber(Payment.generatePaymentNumber())
        .member(member)
        .loan(loan)
        .savingAccount(savingAccount)
        .paymentType(request.getPaymentType())
        .paymentStatus(PaymentStatus.PENDING)
        .amount(request.getAmount())
        .principalAmount(request.getPrincipalAmount())
        .interestAmount(request.getInterestAmount())
        .penaltyAmount(request.getPenaltyAmount())
        .feeAmount(request.getFeeAmount())
        .taxAmount(request.getTaxAmount())
        .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
        .dueDate(request.getDueDate())
        .paymentMethod(request.getPaymentMethod())
        .referenceNumber(request.getReferenceNumber())
        .transactionId(request.getTransactionId())
        .bankAccount(request.getBankAccount())
        .receiptNumber(request.getReceiptNumber())
        .description(request.getDescription())
        .notes(request.getNotes())
        .isRecurring(request.getIsRecurring())
        .recurringFrequency(request.getRecurringFrequency())
        .recurringEndDate(request.getRecurringEndDate())
        .autoDebit(request.getAutoDebit())
        .isVerified(request.getIsVerified())
        .createdBy(createdBy)
        .build();

    payment = paymentRepository.save(payment);

    // Auto-process verified payments
    if (Boolean.TRUE.equals(request.getIsVerified())) {
      payment.verifyPayment(createdBy);
      processPayment(payment);
    }

    log.info("Payment created successfully: {}", payment.getPaymentNumber());
    return PaymentResponse.fromEntity(payment);
  }

  /**
   * Process a payment (mark as completed and update related entities)
   */
  public PaymentResponse processPayment(Long paymentId, String processedBy) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

    if (!payment.requiresProcessing()) {
      throw new RuntimeException("Payment cannot be processed: " + payment.getPaymentStatus());
    }

    processPayment(payment);
    payment = paymentRepository.save(payment);

    log.info("Payment processed successfully: {}", payment.getPaymentNumber());
    return PaymentResponse.fromEntity(payment);
  }

  /**
   * Internal method to process payment logic
   */
  private void processPayment(Payment payment) {
    try {
      switch (payment.getPaymentType()) {
        case LOAN_PRINCIPAL:
          processLoanPrincipalPayment(payment);
          break;
        case LOAN_INTEREST:
          processLoanInterestPayment(payment);
          break;
        case LOAN_PENALTY:
          processLoanPenaltyPayment(payment);
          break;
        case SAVINGS_DEPOSIT:
          processSavingsDeposit(payment);
          break;
        case SHARE_CAPITAL:
          processShareCapitalPayment(payment);
          break;
        case MEMBERSHIP_FEE:
          processMembershipFee(payment);
          break;
        default:
          log.info("Processing generic payment: {}", payment.getPaymentType());
          break;
      }

      payment.completePayment();
      log.info("Payment processing completed: {}", payment.getPaymentNumber());

    } catch (Exception e) {
      payment.failPayment("Processing error: " + e.getMessage());
      log.error("Payment processing failed: {}", payment.getPaymentNumber(), e);
      throw new RuntimeException("Payment processing failed: " + e.getMessage());
    }
  }

  private void processLoanPrincipalPayment(Payment payment) {
    if (payment.getLoan() == null) {
      throw new RuntimeException("Loan not found for principal payment");
    }
    // Update loan balance - simplified for now
    log.info("Processing principal payment for loan: {}", payment.getLoan().getLoanNumber());
    // TODO: Implement loan balance update logic when Loan entity has balance fields
  }

  private void processLoanInterestPayment(Payment payment) {
    if (payment.getLoan() == null) {
      throw new RuntimeException("Loan not found for interest payment");
    }
    // Update loan interest balance or mark as paid
    log.info("Processing interest payment for loan: {}", payment.getLoan().getLoanNumber());
  }

  private void processLoanPenaltyPayment(Payment payment) {
    if (payment.getLoan() == null) {
      throw new RuntimeException("Loan not found for penalty payment");
    }
    log.info("Processing penalty payment for loan: {}", payment.getLoan().getLoanNumber());
  }

  private void processSavingsDeposit(Payment payment) {
    if (payment.getSavingAccount() == null) {
      throw new RuntimeException("Savings account not found for deposit");
    }

    // Use saving service to process deposit
    savingService.deposit(payment.getSavingAccount().getId(), payment.getAmount(),
        payment.getDescription(), payment.getCreatedBy());
  }

  private void processShareCapitalPayment(Payment payment) {
    // Update member's share capital
    Member member = payment.getMember();
    BigDecimal currentShareCapital = member.getShareCapital() != null ? member.getShareCapital() : BigDecimal.ZERO;
    member.setShareCapital(currentShareCapital.add(payment.getAmount()));
    memberRepository.save(member);
  }

  private void processMembershipFee(Payment payment) {
    // Update member's membership fee payment
    log.info("Processing membership fee for member: {}", payment.getMember().getName());
  }

  /**
   * Get payment by ID
   */
  @Transactional(readOnly = true)
  public PaymentResponse getPayment(Long id) {
    Payment payment = paymentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    return PaymentResponse.fromEntity(payment);
  }

  /**
   * Get payment by payment number
   */
  @Transactional(readOnly = true)
  public PaymentResponse getPaymentByNumber(String paymentNumber) {
    Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
        .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentNumber));
    return PaymentResponse.fromEntity(payment);
  }

  /**
   * Get payments by member
   */
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getPaymentsByMember(Long memberId, Pageable pageable) {
    return paymentRepository.findByMemberId(memberId, pageable)
        .map(PaymentResponse::fromEntity);
  }

  /**
   * Get payments by loan
   */
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getPaymentsByLoan(Long loanId, Pageable pageable) {
    return paymentRepository.findByLoanId(loanId, pageable)
        .map(PaymentResponse::fromEntity);
  }

  /**
   * Get payments by savings account
   */
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getPaymentsBySavingsAccount(Long savingAccountId, Pageable pageable) {
    return paymentRepository.findBySavingAccountId(savingAccountId, pageable)
        .map(PaymentResponse::fromEntity);
  }

  /**
   * Get all payments with pagination
   */
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getAllPayments(Pageable pageable) {
    return paymentRepository.findAll(pageable)
        .map(PaymentResponse::fromEntity);
  }

  /**
   * Get overdue payments
   */
  @Transactional(readOnly = true)
  public List<PaymentResponse> getOverduePayments() {
    List<PaymentStatus> completedStatuses = List.of(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED);
    return paymentRepository.findOverduePayments(LocalDate.now(), completedStatuses)
        .stream()
        .map(PaymentResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get pending payments
   */
  @Transactional(readOnly = true)
  public List<PaymentResponse> getPendingPayments() {
    List<PaymentStatus> pendingStatuses = List.of(PaymentStatus.PENDING, PaymentStatus.VERIFIED);
    return paymentRepository.findPendingPayments(pendingStatuses)
        .stream()
        .map(PaymentResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Cancel a payment
   */
  public PaymentResponse cancelPayment(Long paymentId, String reason, String cancelledBy) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

    if (!payment.canModify()) {
      throw new RuntimeException("Payment cannot be cancelled: " + payment.getPaymentStatus());
    }

    payment.cancelPayment(reason);
    payment.setUpdatedBy(cancelledBy);
    payment = paymentRepository.save(payment);

    log.info("Payment cancelled: {} by {} for reason: {}", paymentId, cancelledBy, reason);
    return PaymentResponse.fromEntity(payment);
  }

  /**
   * Verify a payment
   */
  public PaymentResponse verifyPayment(Long paymentId, String verifiedBy) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

    if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
      throw new RuntimeException("Only pending payments can be verified");
    }

    payment.verifyPayment(verifiedBy);
    payment = paymentRepository.save(payment);

    // Auto-process if verification is enough
    if (payment.requiresProcessing()) {
      processPayment(payment);
      payment = paymentRepository.save(payment);
    }

    log.info("Payment verified: {} by {}", paymentId, verifiedBy);
    return PaymentResponse.fromEntity(payment);
  }

  /**
   * Get payment statistics
   */
  public static class PaymentStatistics {
    private final Long totalPayments;
    private final BigDecimal totalAmount;
    private final Long pendingPayments;
    private final Long overduePayments;
    private final BigDecimal pendingAmount;
    private final BigDecimal overdueAmount;

    public PaymentStatistics(Long totalPayments, BigDecimal totalAmount, Long pendingPayments,
        Long overduePayments, BigDecimal pendingAmount, BigDecimal overdueAmount) {
      this.totalPayments = totalPayments;
      this.totalAmount = totalAmount;
      this.pendingPayments = pendingPayments;
      this.overduePayments = overduePayments;
      this.pendingAmount = pendingAmount;
      this.overdueAmount = overdueAmount;
    }

    // Getters
    public Long getTotalPayments() {
      return totalPayments;
    }

    public BigDecimal getTotalAmount() {
      return totalAmount;
    }

    public Long getPendingPayments() {
      return pendingPayments;
    }

    public Long getOverduePayments() {
      return overduePayments;
    }

    public BigDecimal getPendingAmount() {
      return pendingAmount;
    }

    public BigDecimal getOverdueAmount() {
      return overdueAmount;
    }
  }

  /**
   * Get payment statistics for a date range
   */
  @Transactional(readOnly = true)
  public PaymentStatistics getPaymentStatistics(LocalDate startDate, LocalDate endDate) {
    // Implementation would calculate various statistics
    // For now, return basic statistics
    return new PaymentStatistics(0L, BigDecimal.ZERO, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
  }
}
