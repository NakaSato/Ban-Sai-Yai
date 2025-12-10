package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.LoanApplicationRequest;
import com.bansaiyai.bansaiyai.dto.LoanBalanceDTO;
import com.bansaiyai.bansaiyai.dto.LoanResponse;
import com.bansaiyai.bansaiyai.dto.LoanApprovalRequest;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.exception.BusinessException;
import com.bansaiyai.bansaiyai.exception.ResourceNotFoundException;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class LoanService {

  private final LoanRepository loanRepository;
  private final MemberRepository memberRepository;
  private final com.bansaiyai.bansaiyai.repository.GuarantorRepository guarantorRepository;
  private final com.bansaiyai.bansaiyai.repository.LoanBalanceRepository loanBalanceRepository;
  private final SystemConfigService systemConfigService;
  private static final BigDecimal MAX_LOAN_TO_SAVINGS_RATIO = new BigDecimal("3.0");
  private static final int MIN_TERM_MONTHS = 1;
  private static final int MAX_TERM_MONTHS = 120;

  /**
   * Get the maximum loan to savings ratio allowed.
   * 
   * @return the max loan to savings ratio
   */
  public static BigDecimal getMaxLoanToSavingsRatio() {
    return MAX_LOAN_TO_SAVINGS_RATIO;
  }

  // ============================================
  // UUID-based Methods (NEW - For secure API use)
  // ============================================

  /**
   * Get loan by UUID - Secure method for external API use
   */
  @Transactional(readOnly = true)
  public LoanResponse getLoanByUuid(UUID uuid) {
    log.debug("Getting loan by UUID: {}", uuid);
    Loan loan = loanRepository.findByUuid(uuid)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "uuid", uuid.toString()));
    return convertToResponse(loan);
  }

  /**
   * Delete loan by UUID - Secure method for external API use
   */
  public void deleteLoanByUuid(UUID uuid) {
    log.info("Deleting loan by UUID: {}", uuid);

    Loan loan = loanRepository.findByUuid(uuid)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "uuid", uuid.toString()));

    // Only allow deletion of pending loans
    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new BusinessException("Only pending loans can be deleted", "INVALID_LOAN_STATUS");
    }

    loanRepository.delete(loan);
    log.info("Loan UUID: {} deleted successfully", uuid);
  }

  // ============================================
  // Existing methods below
  // ============================================

  public LoanResponse createLoanApplication(LoanApplicationRequest request, String createdBy) {
    log.info("Creating loan application for member ID: {}", request.getMemberId());

    Member member = memberRepository.findById(request.getMemberId())
        .orElseThrow(() -> new ResourceNotFoundException("Member", "id", request.getMemberId()));

    // Validate member is active
    if (!member.getIsActive()) {
      throw new BusinessException("Member is not active", "MEMBER_INACTIVE");
    }

    // Check for existing active loans
    List<Loan> activeLoans = loanRepository.findByMemberIdAndStatus(request.getMemberId(), LoanStatus.ACTIVE);
    if (!activeLoans.isEmpty()) {
      throw new BusinessException("Member already has an active loan", "ACTIVE_LOAN_EXISTS");
    }

    // Validate loan amount
    if (request.getPrincipalAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException("Loan amount must be positive", "INVALID_AMOUNT");
    }

    // Validate term
    if (request.getTermMonths() < MIN_TERM_MONTHS || request.getTermMonths() > MAX_TERM_MONTHS) {
      throw new BusinessException(
          "Loan term must be between " + MIN_TERM_MONTHS + " and " + MAX_TERM_MONTHS + " months",
          "INVALID_TERM");
    }

    // Generate loan number
    String loanNumber = generateLoanNumber();

    Loan loan = Loan.builder()
        .loanNumber(loanNumber)
        .member(member)
        .loanType(request.getLoanType())
        .principalAmount(request.getPrincipalAmount())
        .interestRate(calculateInterestRate(request.getLoanType()))
        .termMonths(request.getTermMonths())
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(request.getTermMonths()))
        .maturityDate(LocalDate.now().plusMonths(request.getTermMonths()))
        .purpose(request.getPurpose())
        .status(LoanStatus.PENDING)
        .createdBy(createdBy)
        .build();

    Loan savedLoan = loanRepository.save(loan);

    // Process Guarantors
    if (request.getGuarantors() != null && !request.getGuarantors().isEmpty()) {
      if (request.getGuarantors().size() > 2) { // Logic: Max 2 guarantors per loan
        throw new BusinessException("Maximum 2 guarantors allowed per loan", "MAX_GUARANTORS_EXCEEDED");
      }

      for (com.bansaiyai.bansaiyai.dto.GuarantorRequest gReq : request.getGuarantors()) {
        Member guarantorMember = memberRepository.findById(gReq.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Guarantor Member", "id", gReq.getMemberId()));

        // Validation 1: Guarantor cannot be the borrower
        if (guarantorMember.getId().equals(member.getId())) {
          throw new BusinessException("Borrower cannot be their own guarantor", "INVALID_GUARANTOR");
        }

        // Validation 2: Guarantor Active Status
        if (!guarantorMember.getIsActive()) {
          throw new BusinessException("Guarantor must be an active member", "GUARANTOR_INACTIVE");
        }

        // Validation 3: Guarantor Limits (e.g., max 3 active guarantees)
        long activeGuarantees = guarantorRepository.countActiveByMemberId(guarantorMember.getId());
        if (activeGuarantees >= 3) {
          throw new BusinessException("Guarantor has reached the limit of active guarantees (3)",
              "GUARANTOR_LIMIT_REACHED");
        }

        // Create and Save Guarantor Entity
        com.bansaiyai.bansaiyai.entity.Guarantor guarantorEntity = com.bansaiyai.bansaiyai.entity.Guarantor.builder()
            .loan(savedLoan)
            .member(guarantorMember)
            .guaranteedAmount(gReq.getGuaranteedAmount())
            .relationship(gReq.getRelationship())
            .isActive(true)
            .guaranteeStartDate(LocalDate.now())
            // .guarantorNumber(generateGuarantorNumber()) // Handled by @PrePersist
            .build();

        guarantorRepository.save(guarantorEntity);
      }
    }

    log.info("Loan application created with number: {}", loanNumber);
    return convertToResponse(savedLoan);
  }

  @com.bansaiyai.bansaiyai.security.Audited(action = "LOAN_APPROVAL", entityType = "Loan")
  public LoanResponse approveLoan(Long loanId, LoanApprovalRequest approvalRequest, String approvedBy) {
    log.info("Approving loan ID: {} by {}", loanId, approvedBy);

    Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new BusinessException("Only pending loans can be approved", "INVALID_LOAN_STATUS");
    }

    // Update loan with approval details
    loan.setApprovedAmount(approvalRequest.getApprovedAmount());
    loan.setApprovedDate(LocalDate.now());
    loan.setApprovalNotes(approvalRequest.getApprovalNotes());
    loan.setApprovedBy(approvedBy);
    loan.setStatus(LoanStatus.APPROVED);

    if (approvalRequest.getCollateralValue() != null) {
      loan.setCollateralValue(approvalRequest.getCollateralValue());
    }

    Loan savedLoan = loanRepository.save(loan);
    return convertToResponse(savedLoan);
  }

  public LoanResponse disburseLoan(Long loanId, String disbursedBy) {
    log.info("Disbursing loan ID: {} by {}", loanId, disbursedBy);

    Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

    if (loan.getStatus() != LoanStatus.APPROVED) {
      throw new BusinessException("Only approved loans can be disbursed", "INVALID_LOAN_STATUS");
    }

    // Update loan with disbursement details
    loan.setDisbursementDate(LocalDate.now());
    loan.setDisbursedBy(disbursedBy);
    loan.setStatus(LoanStatus.ACTIVE);
    loan.setOutstandingBalance(loan.getApprovedAmount() != null ? loan.getApprovedAmount() : loan.getPrincipalAmount());
    loan.setStartDate(LocalDate.now());
    loan.setEndDate(LocalDate.now().plusMonths(loan.getTermMonths()));

    // Generate disbursement reference
    loan.setDisbursementReference(generateDisbursementReference());

    Loan savedLoan = loanRepository.save(loan);
    return convertToResponse(savedLoan);
  }

  public LoanResponse rejectLoan(Long loanId, String rejectionReason, String rejectedBy) {
    log.info("Rejecting loan ID: {} by {}", loanId, rejectedBy);

    Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new BusinessException("Only pending loans can be rejected", "INVALID_LOAN_STATUS");
    }

    loan.setStatus(LoanStatus.REJECTED);
    loan.setRejectionReason(rejectionReason);
    loan.setApprovedBy(rejectedBy);

    Loan savedLoan = loanRepository.save(loan);
    return convertToResponse(savedLoan);
  }

  public LoanResponse completeLoan(Long loanId, String completedBy) {
    log.info("Completing loan ID: {} by {}", loanId, completedBy);

    Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

    if (loan.getStatus() != LoanStatus.ACTIVE) {
      throw new BusinessException("Only active loans can be completed", "INVALID_LOAN_STATUS");
    }

    loan.setStatus(LoanStatus.COMPLETED);
    loan.setUpdatedBy(completedBy);

    // Deactivate guarantors
    if (loan.getGuarantors() != null) {
      loan.getGuarantors().forEach(guarantor -> {
        guarantor.setIsActive(false);
        if (guarantor.getGuaranteeEndDate() == null) {
          guarantor.setGuaranteeEndDate(LocalDate.now());
        }
      });
    }

    Loan savedLoan = loanRepository.save(loan);
    return convertToResponse(savedLoan);
  }

  @Transactional(readOnly = true)
  public Page<LoanResponse> getAllLoans(Pageable pageable) {
    return loanRepository.findAll(pageable)
        .map(this::convertToResponse);
  }

  @Transactional(readOnly = true)
  public Page<LoanResponse> getLoansByStatus(LoanStatus status, Pageable pageable) {
    return loanRepository.findByStatus(status, pageable)
        .map(this::convertToResponse);
  }

  @Transactional(readOnly = true)
  public Page<LoanResponse> getLoansByMember(Long memberId, Pageable pageable) {
    return loanRepository.findByMemberId(memberId, pageable)
        .map(this::convertToResponse);
  }

  @Transactional(readOnly = true)
  public Page<LoanResponse> searchLoans(String keyword, Pageable pageable) {
    return loanRepository.searchLoans(keyword, pageable)
        .map(this::convertToResponse);
  }

  @Transactional(readOnly = true)
  public Page<LoanResponse> getLoansByType(LoanType loanType, Pageable pageable) {
    return loanRepository.findByLoanType(loanType, pageable)
        .map(this::convertToResponse);
  }

  @Transactional(readOnly = true)
  public LoanResponse getLoanById(Long id) {
    log.debug("Getting loan by ID: {}", id);
    Loan loan = loanRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
    return convertToResponse(loan);
  }

  @Transactional(readOnly = true)
  public LoanResponse getLoanByNumber(String loanNumber) {
    log.debug("Getting loan by number: {}", loanNumber);
    Loan loan = loanRepository.findByLoanNumber(loanNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "loanNumber", loanNumber));
    return convertToResponse(loan);
  }

  public LoanResponse updateLoan(Long id, LoanApplicationRequest request, String updatedBy) {
    log.info("Updating loan ID: {} by {}", id, updatedBy);

    Loan loan = loanRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));

    // Only allow updates for pending loans
    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new BusinessException("Only pending loans can be updated", "INVALID_LOAN_STATUS");
    }

    // Update fields
    loan.setLoanType(request.getLoanType());
    loan.setPrincipalAmount(request.getPrincipalAmount());
    loan.setInterestRate(calculateInterestRate(request.getLoanType()));
    loan.setTermMonths(request.getTermMonths());
    loan.setEndDate(LocalDate.now().plusMonths(request.getTermMonths()));
    loan.setMaturityDate(LocalDate.now().plusMonths(request.getTermMonths()));
    loan.setPurpose(request.getPurpose());
    loan.setUpdatedBy(updatedBy);

    Loan savedLoan = loanRepository.save(loan);
    return convertToResponse(savedLoan);
  }

  public void deleteLoan(Long id) {
    log.info("Deleting loan ID: {}", id);

    Loan loan = loanRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));

    // Only allow deletion of pending loans
    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new BusinessException("Only pending loans can be deleted", "INVALID_LOAN_STATUS");
    }

    loanRepository.delete(loan);
    log.info("Loan ID: {} deleted successfully", id);
  }

  // Business logic methods

  @Transactional(readOnly = true)
  public BigDecimal calculateMonthlyPayment(Loan loan) {
    BigDecimal principal = loan.getApprovedAmount() != null ? loan.getApprovedAmount() : loan.getPrincipalAmount();
    BigDecimal monthlyRate = loan.getInterestRate().divide(new BigDecimal("1200"), 6, RoundingMode.HALF_UP);
    int months = loan.getTermMonths();

    if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
      return principal.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
    }

    BigDecimal factor = monthlyRate.add(BigDecimal.ONE)
        .pow(months)
        .multiply(monthlyRate)
        .divide(
            monthlyRate.add(BigDecimal.ONE).pow(months).subtract(BigDecimal.ONE),
            6, RoundingMode.HALF_UP);

    return principal.multiply(factor).setScale(2, RoundingMode.HALF_UP);
  }

  @Transactional(readOnly = true)
  public BigDecimal calculateTotalInterest(Loan loan) {
    BigDecimal monthlyPayment = calculateMonthlyPayment(loan);
    return monthlyPayment.multiply(new BigDecimal(loan.getTermMonths()))
        .subtract(loan.getApprovedAmount() != null ? loan.getApprovedAmount() : loan.getPrincipalAmount());
  }

  /**
   * Calculate payoff amount for a loan on a specific date.
   * Logic: Payoff = Outstanding Principal + Accrued Interest + Penalties
   */
  @Transactional(readOnly = true)
  public java.util.Map<String, BigDecimal> calculatePayoff(Long loanId, LocalDate targetDate) {
    Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

    BigDecimal outstanding = loan.getOutstandingBalance();
    BigDecimal penalty = loan.getPenaltyAmount() != null ? loan.getPenaltyAmount() : BigDecimal.ZERO;

    // Calculate Accrued Interest since last payment (or start date)
    // Formula: Principal * Rate * Days / 365 / 100
    LocalDate lastPaymentDate = LocalDate.now(); // Simplified for MVP, should be loan.getLastPaymentDate()
    long days = java.time.temporal.ChronoUnit.DAYS.between(loan.getStartDate(), targetDate); // Simplified

    // Better Logic: Interest = Outstanding * (Rate/100) * (AvailableDays/365)
    BigDecimal annualRate = loan.getInterestRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    BigDecimal dailyRate = annualRate.divide(new BigDecimal("365"), 8, RoundingMode.HALF_UP);
    BigDecimal accruedInterest = outstanding.multiply(dailyRate).multiply(new BigDecimal(days)).setScale(2,
        RoundingMode.HALF_UP);

    BigDecimal totalPayoff = outstanding.add(accruedInterest).add(penalty);

    java.util.Map<String, BigDecimal> result = new java.util.HashMap<>();
    result.put("principal", outstanding);
    result.put("interest", accruedInterest);
    result.put("penalty", penalty);
    result.put("total", totalPayoff);

    return result;
  }

  @Transactional(readOnly = true)
  public boolean isEligibleForNewLoan(Long memberId, BigDecimal requestedAmount) {
    Member member = memberRepository.findById(memberId).orElse(null);
    if (member == null || !member.getIsActive()) {
      return false;
    }

    // Check existing active loans
    List<Loan> activeLoans = loanRepository.findByMemberIdAndStatus(memberId, LoanStatus.ACTIVE);
    if (!activeLoans.isEmpty()) {
      return false;
    }

    // Check savings requirement (simplified logic)
    // In real scenario, would check member's savings account balance
    return true;
  }

  @Transactional(readOnly = true)
  public LoanStatistics getLoanStatistics() {
    return LoanStatistics.builder()
        .totalLoans(loanRepository.count())
        .activeLoans(loanRepository.countByStatus(LoanStatus.ACTIVE))
        .pendingLoans(loanRepository.countByStatus(LoanStatus.PENDING))
        .completedLoans(loanRepository.countByStatus(LoanStatus.COMPLETED))
        .totalOutstanding(
            loanRepository.sumOutstandingBalances() != null ? loanRepository.sumOutstandingBalances() : BigDecimal.ZERO)
        .businessLoans(loanRepository.countByLoanTypeAndStatus(LoanType.BUSINESS, LoanStatus.ACTIVE))
        .personalLoans(loanRepository.countByLoanTypeAndStatus(LoanType.PERSONAL, LoanStatus.ACTIVE))
        .educationLoans(loanRepository.countByLoanTypeAndStatus(LoanType.EDUCATION, LoanStatus.ACTIVE))
        .build();
  }

  // Helper methods

  private String generateLoanNumber() {
    String prefix = "LN";
    String year = String.valueOf(LocalDate.now().getYear());
    String month = String.format("%02d", LocalDate.now().getMonthValue());
    Random random = new Random();
    String sequence = String.format("%04d", random.nextInt(10000));
    return prefix + year + month + sequence;
  }

  private String generateDisbursementReference() {
    return "DIS" + System.currentTimeMillis();
  }

  private BigDecimal calculateInterestRate(LoanType loanType) {
    switch (loanType) {
      case BUSINESS:
        return new BigDecimal("12.0");
      case EDUCATION:
        return new BigDecimal("8.0");
      case HOUSING:
        return new BigDecimal("10.0");
      case EMERGENCY:
        return new BigDecimal("15.0");
      case PERSONAL:
        return new BigDecimal("11.0");
      default:
        return new BigDecimal("10.0");
    }
  }

  private LoanResponse convertToResponse(Loan loan) {
    return LoanResponse.builder()
        .id(loan.getId())
        .loanNumber(loan.getLoanNumber())
        .memberId(loan.getMember() != null ? loan.getMember().getId() : null)
        .memberName(loan.getMember() != null ? loan.getMember().getName() : null)
        .loanType(loan.getLoanType())
        .principalAmount(loan.getPrincipalAmount())
        .approvedAmount(loan.getApprovedAmount())
        .interestRate(loan.getInterestRate())
        .termMonths(loan.getTermMonths())
        .startDate(loan.getStartDate())
        .endDate(loan.getEndDate())
        .maturityDate(loan.getMaturityDate())
        .disbursementDate(loan.getDisbursementDate())
        .outstandingBalance(loan.getOutstandingBalance())
        .paidPrincipal(loan.getPaidPrincipal())
        .paidInterest(loan.getPaidInterest())
        .penaltyAmount(loan.getPenaltyAmount())
        .status(loan.getStatus())
        .purpose(loan.getPurpose())
        .approvalNotes(loan.getApprovalNotes())
        .rejectionReason(loan.getRejectionReason())
        .collateralValue(loan.getCollateralValue())
        .disbursementReference(loan.getDisbursementReference())
        .createdAt(loan.getCreatedAt() != null ? loan.getCreatedAt().toLocalDate() : null)
        .createdBy(loan.getCreatedBy())
        .updatedAt(loan.getUpdatedAt() != null ? loan.getUpdatedAt().toLocalDate() : null)
        .updatedBy(loan.getUpdatedBy())
        .monthlyPayment(loan.getId() != null ? calculateMonthlyPayment(loan) : null)
        .totalInterest(loan.getId() != null ? calculateTotalInterest(loan) : null)
        .build();
  }

  // Inner class for statistics
  public static class LoanStatistics {
    private final long totalLoans;
    private final long activeLoans;
    private final long pendingLoans;
    private final long completedLoans;
    private final BigDecimal totalOutstanding;
    private final long businessLoans;
    private final long personalLoans;
    private final long educationLoans;

    public LoanStatistics(long totalLoans, long activeLoans, long pendingLoans, long completedLoans,
        BigDecimal totalOutstanding, long businessLoans, long personalLoans, long educationLoans) {
      this.totalLoans = totalLoans;
      this.activeLoans = activeLoans;
      this.pendingLoans = pendingLoans;
      this.completedLoans = completedLoans;
      this.totalOutstanding = totalOutstanding;
      this.businessLoans = businessLoans;
      this.personalLoans = personalLoans;
      this.educationLoans = educationLoans;
    }

    public static LoanStatisticsBuilder builder() {
      return new LoanStatisticsBuilder();
    }

    // Getters
    public long getTotalLoans() {
      return totalLoans;
    }

    public long getActiveLoans() {
      return activeLoans;
    }

    public long getPendingLoans() {
      return pendingLoans;
    }

    public long getCompletedLoans() {
      return completedLoans;
    }

    public BigDecimal getTotalOutstanding() {
      return totalOutstanding;
    }

    public long getBusinessLoans() {
      return businessLoans;
    }

    public long getPersonalLoans() {
      return personalLoans;
    }

    public long getEducationLoans() {
      return educationLoans;
    }

    public static class LoanStatisticsBuilder {
      private long totalLoans;
      private long activeLoans;
      private long pendingLoans;
      private long completedLoans;
      private BigDecimal totalOutstanding;
      private long businessLoans;
      private long personalLoans;
      private long educationLoans;

      public LoanStatisticsBuilder totalLoans(long totalLoans) {
        this.totalLoans = totalLoans;
        return this;
      }

      public LoanStatisticsBuilder activeLoans(long activeLoans) {
        this.activeLoans = activeLoans;
        return this;
      }

      public LoanStatisticsBuilder pendingLoans(long pendingLoans) {
        this.pendingLoans = pendingLoans;
        return this;
      }

      public LoanStatisticsBuilder completedLoans(long completedLoans) {
        this.completedLoans = completedLoans;
        return this;
      }

      public LoanStatisticsBuilder totalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
        return this;
      }

      public LoanStatisticsBuilder businessLoans(long businessLoans) {
        this.businessLoans = businessLoans;
        return this;
      }

      public LoanStatisticsBuilder personalLoans(long personalLoans) {
        this.personalLoans = personalLoans;
        return this;
      }

      public LoanStatisticsBuilder educationLoans(long educationLoans) {
        this.educationLoans = educationLoans;
        return this;
      }

      public LoanStatistics build() {
        return new LoanStatistics(totalLoans, activeLoans, pendingLoans, completedLoans, totalOutstanding,
            businessLoans, personalLoans, educationLoans);
      }
    }
  }

  @Transactional(readOnly = true)
  public List<LoanBalanceDTO> getLoanHistory(Long loanId) {
    log.debug("Getting loan history for loan ID: {}", loanId);

    // Verify loan exists
    if (!loanRepository.existsById(loanId)) {
      throw new ResourceNotFoundException("Loan", "id", loanId);
    }

    return loanBalanceRepository.findByLoanIdOrderByBalanceDateDesc(loanId).stream()
        .map(this::convertToBalanceDTO)
        .toList();
  }

  private LoanBalanceDTO convertToBalanceDTO(com.bansaiyai.bansaiyai.entity.LoanBalance balance) {
    return LoanBalanceDTO.builder()
        .id(balance.getId())
        .balanceDate(balance.getBalanceDate())
        .outstandingBalance(balance.getOutstandingBalance())
        .principalPaid(balance.getPrincipalPaid())
        .interestPaid(balance.getInterestPaid())
        .penaltyPaid(balance.getPenaltyPaid())
        .interestAccrued(balance.getInterestAccrued())
        .isCurrent(balance.getIsCurrent())
        .paymentCount(balance.getPaymentCount() != null ? balance.getPaymentCount().longValue() : 0L)
        .build();
  }
}
