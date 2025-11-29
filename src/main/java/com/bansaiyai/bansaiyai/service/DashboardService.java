package com.bansaiyai.bansaiyai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.SavingAccount;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.SavingRepository;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for dashboard operations.
 * Provides aggregated data for different user roles and dashboard views.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

  // Manual logger for Lombok compatibility
  private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

  private final MemberRepository memberRepository;
  private final LoanRepository loanRepository;
  private final SavingRepository savingRepository;
  private final PaymentRepository paymentRepository;

  // Manual constructor
  public DashboardService(MemberRepository memberRepository, LoanRepository loanRepository,
      SavingRepository savingRepository, PaymentRepository paymentRepository) {
    this.memberRepository = memberRepository;
    this.loanRepository = loanRepository;
    this.savingRepository = savingRepository;
    this.paymentRepository = paymentRepository;
  }

  /**
   * Get admin dashboard statistics
   */
  public AdminDashboardStats getAdminDashboardStats() {
    try {
      LocalDate today = LocalDate.now();
      LocalDate startOfMonth = today.withDayOfMonth(1);
      LocalDate startOfLastMonth = startOfMonth.minusMonths(1);

      // Member statistics
      long totalMembers = memberRepository.count();
      long activeMembers = memberRepository.countByIsActive(true);

      // Loan statistics
      long totalLoans = loanRepository.count();
      long activeLoans = loanRepository.countByStatus(LoanStatus.ACTIVE);
      long pendingLoans = loanRepository.countByStatus(LoanStatus.PENDING);

      BigDecimal totalLoanPortfolio = loanRepository
          .sumLoanAmountByStatus(List.of(LoanStatus.ACTIVE, LoanStatus.APPROVED));
      BigDecimal overdueLoanAmount = loanRepository.sumOverdueAmount();

      // Savings statistics
      long totalSavingsAccounts = savingRepository.count();
      BigDecimal totalSavings = savingRepository.sumTotalSavings();

      // Payment statistics
      List<PaymentStatus> completedStatuses = List.of(PaymentStatus.COMPLETED);
      List<PaymentStatus> pendingStatuses = List.of(PaymentStatus.PENDING, PaymentStatus.VERIFIED);
      List<PaymentStatus> overduePaymentStatuses = List.of(PaymentStatus.OVERDUE);

      long completedPayments = paymentRepository.countByPaymentStatusIn(completedStatuses);
      long pendingPayments = paymentRepository.countByPaymentStatusIn(pendingStatuses);
      long overduePayments = paymentRepository.countByPaymentStatusIn(overduePaymentStatuses);

      BigDecimal totalPaymentsThisMonth = paymentRepository.sumPaymentsByDateRange(startOfMonth, today);
      BigDecimal totalPaymentsLastMonth = paymentRepository.sumPaymentsByDateRange(startOfLastMonth,
          startOfMonth.minusDays(1));

      // Calculate growth rates
      double memberGrowthRate = calculateGrowthRate(activeMembers, totalMembers);
      double paymentGrowthRate = calculateGrowthRate(
          totalPaymentsThisMonth != null ? totalPaymentsThisMonth : BigDecimal.ZERO,
          totalPaymentsLastMonth != null ? totalPaymentsLastMonth : BigDecimal.ZERO);

      return AdminDashboardStats.builder()
          .totalMembers(totalMembers)
          .activeMembers(activeMembers)
          .memberGrowthRate(memberGrowthRate)
          .totalLoans(totalLoans)
          .activeLoans(activeLoans)
          .pendingLoans(pendingLoans)
          .totalLoanPortfolio(totalLoanPortfolio != null ? totalLoanPortfolio : BigDecimal.ZERO)
          .overdueLoanAmount(overdueLoanAmount != null ? overdueLoanAmount : BigDecimal.ZERO)
          .totalSavingsAccounts(totalSavingsAccounts)
          .totalSavings(totalSavings != null ? totalSavings : BigDecimal.ZERO)
          .completedPayments(completedPayments)
          .pendingPayments(pendingPayments)
          .overduePayments(overduePayments)
          .totalPaymentsThisMonth(totalPaymentsThisMonth != null ? totalPaymentsThisMonth : BigDecimal.ZERO)
          .paymentGrowthRate(paymentGrowthRate)
          .lastUpdated(LocalDateTime.now())
          .build();

    } catch (Exception e) {
      log.error("Error getting admin dashboard stats: {}", e.getMessage());
      return AdminDashboardStats.empty();
    }
  }

  /**
   * Get member dashboard statistics
   */
  public MemberDashboardStats getMemberDashboardStats(Long memberId) {
    try {
      Member member = memberRepository.findById(memberId).orElse(null);
      if (member == null) {
        return MemberDashboardStats.empty();
      }

      // Loan information
      List<Loan> memberLoans = loanRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
      long activeLoans = memberLoans.stream().mapToLong(loan -> loan.getStatus() == LoanStatus.ACTIVE ? 1 : 0).sum();
      BigDecimal totalLoanBalance = memberLoans.stream()
          .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
          .map(Loan::getPrincipalAmount) // Using getPrincipalAmount method
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Savings information
      List<SavingAccount> memberSavings = savingRepository.findByMemberId(memberId);
      BigDecimal totalSavingsBalance = memberSavings.stream()
          .map(SavingAccount::getBalance) // Using getBalance method
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Recent payments
      List<Payment> recentPayments = paymentRepository.findTop10ByMemberIdOrderByPaymentDateDesc(memberId);
      BigDecimal totalPaymentsThisMonth = paymentRepository.sumPaymentsByMemberAndDateRange(
          memberId, LocalDate.now().withDayOfMonth(1), LocalDate.now());

      return MemberDashboardStats.builder()
          .memberId(memberId)
          .memberName(member.getName())
          .memberStatus(member.getIsActive() ? "Active" : "Inactive") // Using getIsActive() method
          .totalLoans(memberLoans.size())
          .activeLoans((int) activeLoans)
          .totalLoanBalance(totalLoanBalance)
          .totalSavingsAccounts(memberSavings.size())
          .totalSavingsBalance(totalSavingsBalance)
          .recentPayments(recentPayments.stream()
              .limit(5)
              .map(this::createPaymentSummary)
              .collect(Collectors.toList()))
          .totalPaymentsThisMonth(totalPaymentsThisMonth != null ? totalPaymentsThisMonth : BigDecimal.ZERO)
          .nextLoanPaymentDate(calculateNextLoanPaymentDate(memberLoans))
          .lastUpdated(LocalDateTime.now())
          .build();

    } catch (Exception e) {
      log.error("Error getting member dashboard stats for {}: {}", memberId, e.getMessage());
      return MemberDashboardStats.empty();
    }
  }

  /**
   * Get officer dashboard statistics
   */
  public OfficerDashboardStats getOfficerDashboardStats(String officerUsername) {
    try {
      LocalDate today = LocalDate.now();
      LocalDate startOfMonth = today.withDayOfMonth(1);

      // Tasks pending approval
      long pendingLoanApplications = loanRepository.countByStatus(LoanStatus.PENDING);
      long pendingPayments = paymentRepository.countByPaymentStatusIn(
          List.of(PaymentStatus.PENDING, PaymentStatus.VERIFIED));

      // Today's activities
      long loansProcessedToday = loanRepository.findByProcessedDateAndProcessedBy(today, officerUsername).size();
      long paymentsProcessedToday = paymentRepository.countByProcessedDate(today);

      // Monthly performance
      long loansProcessedThisMonth = loanRepository.findByProcessedDateBetweenAndProcessedBy(
          startOfMonth, today, officerUsername).size();
      BigDecimal paymentsProcessedThisMonth = paymentRepository.sumPaymentsByProcessedByAndDateRange(
          officerUsername, startOfMonth, today);

      // Overdue items requiring attention
      List<Loan> overdueLoans = loanRepository.findOverdueLoans();
      List<Payment> overduePayments = paymentRepository.findOverduePayments(today,
          List.of(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED));

      return OfficerDashboardStats.builder()
          .officerUsername(officerUsername)
          .pendingLoanApplications(pendingLoanApplications)
          .pendingPayments(pendingPayments)
          .loansProcessedToday(loansProcessedToday)
          .paymentsProcessedToday(paymentsProcessedToday)
          .loansProcessedThisMonth(loansProcessedThisMonth)
          .paymentsProcessedThisMonth(paymentsProcessedThisMonth != null ? paymentsProcessedThisMonth : BigDecimal.ZERO)
          .overdueLoansRequiringAttention((long) overdueLoans.size())
          .overduePaymentsRequiringAttention((long) overduePayments.size())
          .lastUpdated(LocalDateTime.now())
          .build();

    } catch (Exception e) {
      log.error("Error getting officer dashboard stats for {}: {}", officerUsername, e.getMessage());
      return OfficerDashboardStats.empty();
    }
  }

  /**
   * Get system health indicators
   */
  public SystemHealthIndicators getSystemHealthIndicators() {
    try {
      // System performance metrics
      long totalMembers = memberRepository.count();
      long activeLoans = loanRepository.countByStatus(LoanStatus.ACTIVE);
      long overduePayments = paymentRepository.countByPaymentStatusIn(List.of(PaymentStatus.OVERDUE));

      // Calculate health scores
      double loanDelinquencyRate = totalMembers > 0 ? (double) overduePayments / totalMembers * 100 : 0;
      double systemLoad = calculateSystemLoad();
      boolean databaseHealth = checkDatabaseHealth();
      boolean paymentProcessorHealth = checkPaymentProcessorHealth();

      return SystemHealthIndicators.builder()
          .overallHealthStatus(calculateOverallHealth(databaseHealth, paymentProcessorHealth, loanDelinquencyRate))
          .loanDelinquencyRate(loanDelinquencyRate)
          .systemLoad(systemLoad)
          .databaseHealth(databaseHealth)
          .paymentProcessorHealth(paymentProcessorHealth)
          .totalActiveTransactions(getCurrentActiveTransactions())
          .lastHealthCheck(LocalDateTime.now())
          .build();

    } catch (Exception e) {
      log.error("Error getting system health indicators: {}", e.getMessage());
      return SystemHealthIndicators.empty();
    }
  }

  // Helper methods

  private double calculateGrowthRate(Number current, Number previous) {
    if (previous == null || previous.doubleValue() == 0) {
      return current != null ? 100.0 : 0.0;
    }
    double currentValue = current != null ? current.doubleValue() : 0;
    double previousValue = previous.doubleValue();
    return ((currentValue - previousValue) / previousValue) * 100;
  }

  private double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
    if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
      return current != null ? 100.0 : 0.0;
    }
    BigDecimal currentValue = current != null ? current : BigDecimal.ZERO;
    return currentValue.subtract(previous)
        .divide(previous, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .doubleValue();
  }

  private PaymentSummary createPaymentSummary(Payment payment) {
    return PaymentSummary.builder()
        .paymentId(payment.getId())
        .paymentNumber(payment.getPaymentNumber())
        .paymentType(payment.getPaymentType().getDisplayName())
        .amount(payment.getAmount())
        .status(payment.getPaymentStatus().getDisplayName())
        .paymentDate(payment.getPaymentDate())
        .build();
  }

  private LocalDate calculateNextLoanPaymentDate(List<Loan> loans) {
    return loans.stream()
        .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE && loan.getMaturityDate() != null)
        .map(Loan::getMaturityDate)
        .min(LocalDate::compareTo)
        .orElse(null);
  }

  private double calculateSystemLoad() {
    // Simplified system load calculation
    // In a real implementation, this would check CPU, memory, database connections
    return Math.random() * 100; // Placeholder
  }

  private boolean checkDatabaseHealth() {
    // Simplified database health check
    // In a real implementation, this would check connection, query performance,
    // etc.
    return true; // Placeholder
  }

  private boolean checkPaymentProcessorHealth() {
    // Simplified payment processor health check
    // In a real implementation, this would check payment gateways, etc.
    return true; // Placeholder
  }

  private long getCurrentActiveTransactions() {
    // Count of currently processing transactions
    return paymentRepository.countByPaymentStatusIn(List.of(PaymentStatus.PROCESSING));
  }

  private String calculateOverallHealth(boolean databaseHealth, boolean paymentHealth, double delinquencyRate) {
    if (!databaseHealth || !paymentHealth) {
      return "CRITICAL";
    } else if (delinquencyRate > 10) {
      return "WARNING";
    } else if (delinquencyRate > 5) {
      return "CAUTION";
    } else {
      return "HEALTHY";
    }
  }

  // Dashboard data classes

  @lombok.Data
  @lombok.Builder
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class AdminDashboardStats {
    private Long totalMembers;
    private Long activeMembers;
    private Double memberGrowthRate;
    private Long totalLoans;
    private Long activeLoans;
    private Long pendingLoans;
    private BigDecimal totalLoanPortfolio;
    private BigDecimal overdueLoanAmount;
    private Long totalSavingsAccounts;
    private BigDecimal totalSavings;
    private Long completedPayments;
    private Long pendingPayments;
    private Long overduePayments;
    private BigDecimal totalPaymentsThisMonth;
    private Double paymentGrowthRate;
    private LocalDateTime lastUpdated;

    // Manual builder for Lombok compatibility
    public static AdminDashboardStatsBuilder builder() {
      return new AdminDashboardStatsBuilder();
    }

    public static class AdminDashboardStatsBuilder {
      private Long totalMembers;
      private Long activeMembers;
      private Double memberGrowthRate;
      private Long totalLoans;
      private Long activeLoans;
      private Long pendingLoans;
      private BigDecimal totalLoanPortfolio;
      private BigDecimal overdueLoanAmount;
      private Long totalSavingsAccounts;
      private BigDecimal totalSavings;
      private Long completedPayments;
      private Long pendingPayments;
      private Long overduePayments;
      private BigDecimal totalPaymentsThisMonth;
      private Double paymentGrowthRate;
      private LocalDateTime lastUpdated;

      public AdminDashboardStatsBuilder totalMembers(Long totalMembers) {
        this.totalMembers = totalMembers;
        return this;
      }

      public AdminDashboardStatsBuilder activeMembers(Long activeMembers) {
        this.activeMembers = activeMembers;
        return this;
      }

      public AdminDashboardStatsBuilder memberGrowthRate(Double memberGrowthRate) {
        this.memberGrowthRate = memberGrowthRate;
        return this;
      }

      public AdminDashboardStatsBuilder totalLoans(Long totalLoans) {
        this.totalLoans = totalLoans;
        return this;
      }

      public AdminDashboardStatsBuilder activeLoans(Long activeLoans) {
        this.activeLoans = activeLoans;
        return this;
      }

      public AdminDashboardStatsBuilder pendingLoans(Long pendingLoans) {
        this.pendingLoans = pendingLoans;
        return this;
      }

      public AdminDashboardStatsBuilder totalLoanPortfolio(BigDecimal totalLoanPortfolio) {
        this.totalLoanPortfolio = totalLoanPortfolio;
        return this;
      }

      public AdminDashboardStatsBuilder overdueLoanAmount(BigDecimal overdueLoanAmount) {
        this.overdueLoanAmount = overdueLoanAmount;
        return this;
      }

      public AdminDashboardStatsBuilder totalSavingsAccounts(Long totalSavingsAccounts) {
        this.totalSavingsAccounts = totalSavingsAccounts;
        return this;
      }

      public AdminDashboardStatsBuilder totalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
        return this;
      }

      public AdminDashboardStatsBuilder completedPayments(Long completedPayments) {
        this.completedPayments = completedPayments;
        return this;
      }

      public AdminDashboardStatsBuilder pendingPayments(Long pendingPayments) {
        this.pendingPayments = pendingPayments;
        return this;
      }

      public AdminDashboardStatsBuilder overduePayments(Long overduePayments) {
        this.overduePayments = overduePayments;
        return this;
      }

      public AdminDashboardStatsBuilder totalPaymentsThisMonth(BigDecimal totalPaymentsThisMonth) {
        this.totalPaymentsThisMonth = totalPaymentsThisMonth;
        return this;
      }

      public AdminDashboardStatsBuilder paymentGrowthRate(Double paymentGrowthRate) {
        this.paymentGrowthRate = paymentGrowthRate;
        return this;
      }

      public AdminDashboardStatsBuilder lastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
      }

      public AdminDashboardStats build() {
        AdminDashboardStats stats = new AdminDashboardStats();
        stats.totalMembers = this.totalMembers;
        stats.activeMembers = this.activeMembers;
        stats.memberGrowthRate = this.memberGrowthRate;
        stats.totalLoans = this.totalLoans;
        stats.activeLoans = this.activeLoans;
        stats.pendingLoans = this.pendingLoans;
        stats.totalLoanPortfolio = this.totalLoanPortfolio;
        stats.overdueLoanAmount = this.overdueLoanAmount;
        stats.totalSavingsAccounts = this.totalSavingsAccounts;
        stats.totalSavings = this.totalSavings;
        stats.completedPayments = this.completedPayments;
        stats.pendingPayments = this.pendingPayments;
        stats.overduePayments = this.overduePayments;
        stats.totalPaymentsThisMonth = this.totalPaymentsThisMonth;
        stats.paymentGrowthRate = this.paymentGrowthRate;
        stats.lastUpdated = this.lastUpdated;
        return stats;
      }
    }

    public static AdminDashboardStats empty() {
      return AdminDashboardStats.builder()
          .totalMembers(0L)
          .activeMembers(0L)
          .memberGrowthRate(0.0)
          .totalLoans(0L)
          .activeLoans(0L)
          .pendingLoans(0L)
          .totalLoanPortfolio(BigDecimal.ZERO)
          .overdueLoanAmount(BigDecimal.ZERO)
          .totalSavingsAccounts(0L)
          .totalSavings(BigDecimal.ZERO)
          .completedPayments(0L)
          .pendingPayments(0L)
          .overduePayments(0L)
          .totalPaymentsThisMonth(BigDecimal.ZERO)
          .paymentGrowthRate(0.0)
          .lastUpdated(LocalDateTime.now())
          .build();
    }
  }

  @lombok.Data
  @lombok.Builder
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class MemberDashboardStats {
    private Long memberId;
    private String memberName;
    private String memberStatus;
    private Integer totalLoans;
    private Integer activeLoans;
    private BigDecimal totalLoanBalance;
    private Integer totalSavingsAccounts;
    private BigDecimal totalSavingsBalance;
    private List<PaymentSummary> recentPayments;
    private BigDecimal totalPaymentsThisMonth;
    private LocalDate nextLoanPaymentDate;
    private LocalDateTime lastUpdated;

    // Manual builder for Lombok compatibility
    public static MemberDashboardStatsBuilder builder() {
      return new MemberDashboardStatsBuilder();
    }

    public static class MemberDashboardStatsBuilder {
      private Long memberId;
      private String memberName;
      private String memberStatus;
      private Integer totalLoans;
      private Integer activeLoans;
      private BigDecimal totalLoanBalance;
      private Integer totalSavingsAccounts;
      private BigDecimal totalSavingsBalance;
      private List<PaymentSummary> recentPayments;
      private BigDecimal totalPaymentsThisMonth;
      private LocalDate nextLoanPaymentDate;
      private LocalDateTime lastUpdated;

      public MemberDashboardStatsBuilder memberId(Long memberId) {
        this.memberId = memberId;
        return this;
      }

      public MemberDashboardStatsBuilder memberName(String memberName) {
        this.memberName = memberName;
        return this;
      }

      public MemberDashboardStatsBuilder memberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
        return this;
      }

      public MemberDashboardStatsBuilder totalLoans(Integer totalLoans) {
        this.totalLoans = totalLoans;
        return this;
      }

      public MemberDashboardStatsBuilder activeLoans(Integer activeLoans) {
        this.activeLoans = activeLoans;
        return this;
      }

      public MemberDashboardStatsBuilder totalLoanBalance(BigDecimal totalLoanBalance) {
        this.totalLoanBalance = totalLoanBalance;
        return this;
      }

      public MemberDashboardStatsBuilder totalSavingsAccounts(Integer totalSavingsAccounts) {
        this.totalSavingsAccounts = totalSavingsAccounts;
        return this;
      }

      public MemberDashboardStatsBuilder totalSavingsBalance(BigDecimal totalSavingsBalance) {
        this.totalSavingsBalance = totalSavingsBalance;
        return this;
      }

      public MemberDashboardStatsBuilder recentPayments(List<PaymentSummary> recentPayments) {
        this.recentPayments = recentPayments;
        return this;
      }

      public MemberDashboardStatsBuilder totalPaymentsThisMonth(BigDecimal totalPaymentsThisMonth) {
        this.totalPaymentsThisMonth = totalPaymentsThisMonth;
        return this;
      }

      public MemberDashboardStatsBuilder nextLoanPaymentDate(LocalDate nextLoanPaymentDate) {
        this.nextLoanPaymentDate = nextLoanPaymentDate;
        return this;
      }

      public MemberDashboardStatsBuilder lastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
      }

      public MemberDashboardStats build() {
        MemberDashboardStats stats = new MemberDashboardStats();
        stats.memberId = this.memberId;
        stats.memberName = this.memberName;
        stats.memberStatus = this.memberStatus;
        stats.totalLoans = this.totalLoans;
        stats.activeLoans = this.activeLoans;
        stats.totalLoanBalance = this.totalLoanBalance;
        stats.totalSavingsAccounts = this.totalSavingsAccounts;
        stats.totalSavingsBalance = this.totalSavingsBalance;
        stats.recentPayments = this.recentPayments;
        stats.totalPaymentsThisMonth = this.totalPaymentsThisMonth;
        stats.nextLoanPaymentDate = this.nextLoanPaymentDate;
        stats.lastUpdated = this.lastUpdated;
        return stats;
      }
    }

    public static MemberDashboardStats empty() {
      return MemberDashboardStats.builder()
          .memberId(0L)
          .memberName("")
          .memberStatus("Unknown")
          .totalLoans(0)
          .activeLoans(0)
          .totalLoanBalance(BigDecimal.ZERO)
          .totalSavingsAccounts(0)
          .totalSavingsBalance(BigDecimal.ZERO)
          .recentPayments(List.of())
          .totalPaymentsThisMonth(BigDecimal.ZERO)
          .nextLoanPaymentDate(null)
          .lastUpdated(LocalDateTime.now())
          .build();
    }
  }

  @lombok.Data
  @lombok.Builder
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class OfficerDashboardStats {
    private String officerUsername;
    private Long pendingLoanApplications;
    private Long pendingPayments;
    private Long loansProcessedToday;
    private Long paymentsProcessedToday;
    private Long loansProcessedThisMonth;
    private BigDecimal paymentsProcessedThisMonth;
    private Long overdueLoansRequiringAttention;
    private Long overduePaymentsRequiringAttention;
    private LocalDateTime lastUpdated;

    // Manual builder for Lombok compatibility
    public static OfficerDashboardStatsBuilder builder() {
      return new OfficerDashboardStatsBuilder();
    }

    public static class OfficerDashboardStatsBuilder {
      private String officerUsername;
      private Long pendingLoanApplications;
      private Long pendingPayments;
      private Long loansProcessedToday;
      private Long paymentsProcessedToday;
      private Long loansProcessedThisMonth;
      private BigDecimal paymentsProcessedThisMonth;
      private Long overdueLoansRequiringAttention;
      private Long overduePaymentsRequiringAttention;
      private LocalDateTime lastUpdated;

      public OfficerDashboardStatsBuilder officerUsername(String officerUsername) {
        this.officerUsername = officerUsername;
        return this;
      }

      public OfficerDashboardStatsBuilder pendingLoanApplications(Long pendingLoanApplications) {
        this.pendingLoanApplications = pendingLoanApplications;
        return this;
      }

      public OfficerDashboardStatsBuilder pendingPayments(Long pendingPayments) {
        this.pendingPayments = pendingPayments;
        return this;
      }

      public OfficerDashboardStatsBuilder loansProcessedToday(Long loansProcessedToday) {
        this.loansProcessedToday = loansProcessedToday;
        return this;
      }

      public OfficerDashboardStatsBuilder paymentsProcessedToday(Long paymentsProcessedToday) {
        this.paymentsProcessedToday = paymentsProcessedToday;
        return this;
      }

      public OfficerDashboardStatsBuilder loansProcessedThisMonth(Long loansProcessedThisMonth) {
        this.loansProcessedThisMonth = loansProcessedThisMonth;
        return this;
      }

      public OfficerDashboardStatsBuilder paymentsProcessedThisMonth(BigDecimal paymentsProcessedThisMonth) {
        this.paymentsProcessedThisMonth = paymentsProcessedThisMonth;
        return this;
      }

      public OfficerDashboardStatsBuilder overdueLoansRequiringAttention(Long overdueLoansRequiringAttention) {
        this.overdueLoansRequiringAttention = overdueLoansRequiringAttention;
        return this;
      }

      public OfficerDashboardStatsBuilder overduePaymentsRequiringAttention(Long overduePaymentsRequiringAttention) {
        this.overduePaymentsRequiringAttention = overduePaymentsRequiringAttention;
        return this;
      }

      public OfficerDashboardStatsBuilder lastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
      }

      public OfficerDashboardStats build() {
        OfficerDashboardStats stats = new OfficerDashboardStats();
        stats.officerUsername = this.officerUsername;
        stats.pendingLoanApplications = this.pendingLoanApplications;
        stats.pendingPayments = this.pendingPayments;
        stats.loansProcessedToday = this.loansProcessedToday;
        stats.paymentsProcessedToday = this.paymentsProcessedToday;
        stats.loansProcessedThisMonth = this.loansProcessedThisMonth;
        stats.paymentsProcessedThisMonth = this.paymentsProcessedThisMonth;
        stats.overdueLoansRequiringAttention = this.overdueLoansRequiringAttention;
        stats.overduePaymentsRequiringAttention = this.overduePaymentsRequiringAttention;
        stats.lastUpdated = this.lastUpdated;
        return stats;
      }
    }

    public static OfficerDashboardStats empty() {
      return OfficerDashboardStats.builder()
          .officerUsername("")
          .pendingLoanApplications(0L)
          .pendingPayments(0L)
          .loansProcessedToday(0L)
          .paymentsProcessedToday(0L)
          .loansProcessedThisMonth(0L)
          .paymentsProcessedThisMonth(BigDecimal.ZERO)
          .overdueLoansRequiringAttention(0L)
          .overduePaymentsRequiringAttention(0L)
          .lastUpdated(LocalDateTime.now())
          .build();
    }
  }

  @lombok.Data
  @lombok.Builder
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class SystemHealthIndicators {
    private String overallHealthStatus;
    private Double loanDelinquencyRate;
    private Double systemLoad;
    private Boolean databaseHealth;
    private Boolean paymentProcessorHealth;
    private Long totalActiveTransactions;
    private LocalDateTime lastHealthCheck;

    // Manual builder for Lombok compatibility
    public static SystemHealthIndicatorsBuilder builder() {
      return new SystemHealthIndicatorsBuilder();
    }

    public static class SystemHealthIndicatorsBuilder {
      private String overallHealthStatus;
      private Double loanDelinquencyRate;
      private Double systemLoad;
      private Boolean databaseHealth;
      private Boolean paymentProcessorHealth;
      private Long totalActiveTransactions;
      private LocalDateTime lastHealthCheck;

      public SystemHealthIndicatorsBuilder overallHealthStatus(String overallHealthStatus) {
        this.overallHealthStatus = overallHealthStatus;
        return this;
      }

      public SystemHealthIndicatorsBuilder loanDelinquencyRate(Double loanDelinquencyRate) {
        this.loanDelinquencyRate = loanDelinquencyRate;
        return this;
      }

      public SystemHealthIndicatorsBuilder systemLoad(Double systemLoad) {
        this.systemLoad = systemLoad;
        return this;
      }

      public SystemHealthIndicatorsBuilder databaseHealth(Boolean databaseHealth) {
        this.databaseHealth = databaseHealth;
        return this;
      }

      public SystemHealthIndicatorsBuilder paymentProcessorHealth(Boolean paymentProcessorHealth) {
        this.paymentProcessorHealth = paymentProcessorHealth;
        return this;
      }

      public SystemHealthIndicatorsBuilder totalActiveTransactions(Long totalActiveTransactions) {
        this.totalActiveTransactions = totalActiveTransactions;
        return this;
      }

      public SystemHealthIndicatorsBuilder lastHealthCheck(LocalDateTime lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
        return this;
      }

      public SystemHealthIndicators build() {
        SystemHealthIndicators indicators = new SystemHealthIndicators();
        indicators.overallHealthStatus = this.overallHealthStatus;
        indicators.loanDelinquencyRate = this.loanDelinquencyRate;
        indicators.systemLoad = this.systemLoad;
        indicators.databaseHealth = this.databaseHealth;
        indicators.paymentProcessorHealth = this.paymentProcessorHealth;
        indicators.totalActiveTransactions = this.totalActiveTransactions;
        indicators.lastHealthCheck = this.lastHealthCheck;
        return indicators;
      }
    }

    public static SystemHealthIndicators empty() {
      return SystemHealthIndicators.builder()
          .overallHealthStatus("UNKNOWN")
          .loanDelinquencyRate(0.0)
          .systemLoad(0.0)
          .databaseHealth(false)
          .paymentProcessorHealth(false)
          .totalActiveTransactions(0L)
          .lastHealthCheck(LocalDateTime.now())
          .build();
    }
  }

  @lombok.Data
  @lombok.Builder
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class PaymentSummary {
    private Long paymentId;
    private String paymentNumber;
    private String paymentType;
    private BigDecimal amount;
    private String status;
    private LocalDate paymentDate;

    // Manual builder for Lombok compatibility
    public static PaymentSummaryBuilder builder() {
      return new PaymentSummaryBuilder();
    }

    public static class PaymentSummaryBuilder {
      private Long paymentId;
      private String paymentNumber;
      private String paymentType;
      private BigDecimal amount;
      private String status;
      private LocalDate paymentDate;

      public PaymentSummaryBuilder paymentId(Long paymentId) {
        this.paymentId = paymentId;
        return this;
      }

      public PaymentSummaryBuilder paymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
        return this;
      }

      public PaymentSummaryBuilder paymentType(String paymentType) {
        this.paymentType = paymentType;
        return this;
      }

      public PaymentSummaryBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
      }

      public PaymentSummaryBuilder status(String status) {
        this.status = status;
        return this;
      }

      public PaymentSummaryBuilder paymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
        return this;
      }

      public PaymentSummary build() {
        PaymentSummary summary = new PaymentSummary();
        summary.paymentId = this.paymentId;
        summary.paymentNumber = this.paymentNumber;
        summary.paymentType = this.paymentType;
        summary.amount = this.amount;
        summary.status = this.status;
        summary.paymentDate = this.paymentDate;
        return summary;
      }
    }
  }
}
