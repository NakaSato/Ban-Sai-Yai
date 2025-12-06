package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.LoanApplicationRequest;
import com.bansaiyai.bansaiyai.dto.LoanResponse;
import com.bansaiyai.bansaiyai.dto.LoanApprovalRequest;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanService {

  @Autowired
  private LoanRepository loanRepository;

  @Autowired
  private MemberRepository memberRepository;

  private static final BigDecimal MAX_LOAN_TO_SAVINGS_RATIO = new BigDecimal("3.0");
  private static final int MIN_TERM_MONTHS = 1;
  private static final int MAX_TERM_MONTHS = 120;

  public LoanResponse createLoanApplication(LoanApplicationRequest request, String createdBy) {
    Member member = memberRepository.findById(request.getMemberId())
        .orElseThrow(() -> new RuntimeException("Member not found"));

    // Validate member is active
    if (!member.getIsActive()) {
      throw new RuntimeException("Member is not active");
    }

    // Check for existing active loans
    List<Loan> activeLoans = loanRepository.findByMemberIdAndStatus(request.getMemberId(), LoanStatus.ACTIVE);
    if (!activeLoans.isEmpty()) {
      throw new RuntimeException("Member already has an active loan");
    }

    // Validate loan amount
    if (request.getPrincipalAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Loan amount must be positive");
    }

    // Validate term
    if (request.getTermMonths() < MIN_TERM_MONTHS || request.getTermMonths() > MAX_TERM_MONTHS) {
      throw new RuntimeException(
          "Loan term must be between " + MIN_TERM_MONTHS + " and " + MAX_TERM_MONTHS + " months");
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
    return convertToResponse(savedLoan);
  }

  @com.bansaiyai.bansaiyai.security.Audited(action = "LOAN_APPROVAL", entityType = "Loan")
  public LoanResponse approveLoan(Long loanId, LoanApprovalRequest approvalRequest, String approvedBy) {
    Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new RuntimeException("Only pending loans can be approved");
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
    Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.APPROVED) {
      throw new RuntimeException("Only approved loans can be disbursed");
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
    Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new RuntimeException("Only pending loans can be rejected");
    }

    loan.setStatus(LoanStatus.REJECTED);
    loan.setRejectionReason(rejectionReason);
    loan.setApprovedBy(rejectedBy);

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
    Loan loan = loanRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Loan not found"));
    return convertToResponse(loan);
  }

  @Transactional(readOnly = true)
  public LoanResponse getLoanByNumber(String loanNumber) {
    Loan loan = loanRepository.findByLoanNumber(loanNumber)
        .orElseThrow(() -> new RuntimeException("Loan not found"));
    return convertToResponse(loan);
  }

  public LoanResponse updateLoan(Long id, LoanApplicationRequest request, String updatedBy) {
    Loan loan = loanRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Loan not found"));

    // Only allow updates for pending loans
    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new RuntimeException("Only pending loans can be updated");
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
    Loan loan = loanRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Loan not found"));

    // Only allow deletion of pending loans
    if (loan.getStatus() != LoanStatus.PENDING) {
      throw new RuntimeException("Only pending loans can be deleted");
    }

    loanRepository.delete(loan);
  }

  // Business logic methods

  @Transactional(readOnly = true)
  public BigDecimal calculateMonthlyPayment(Loan loan) {
    BigDecimal principal = loan.getApprovedAmount() != null ? loan.getApprovedAmount() : loan.getPrincipalAmount();
    BigDecimal monthlyRate = loan.getInterestRate().divide(new BigDecimal("1200"), 6, BigDecimal.ROUND_HALF_UP);
    int months = loan.getTermMonths();

    if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
      return principal.divide(new BigDecimal(months), 2, BigDecimal.ROUND_HALF_UP);
    }

    BigDecimal factor = monthlyRate.add(BigDecimal.ONE)
        .pow(months)
        .multiply(monthlyRate)
        .divide(
            monthlyRate.add(BigDecimal.ONE).pow(months).subtract(BigDecimal.ONE),
            6, BigDecimal.ROUND_HALF_UP);

    return principal.multiply(factor).setScale(2, BigDecimal.ROUND_HALF_UP);
  }

  @Transactional(readOnly = true)
  public BigDecimal calculateTotalInterest(Loan loan) {
    BigDecimal monthlyPayment = calculateMonthlyPayment(loan);
    return monthlyPayment.multiply(new BigDecimal(loan.getTermMonths()))
        .subtract(loan.getApprovedAmount() != null ? loan.getApprovedAmount() : loan.getPrincipalAmount());
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
}
