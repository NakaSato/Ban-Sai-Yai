package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.AccountingEntry;
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
import com.bansaiyai.bansaiyai.repository.SavingTransactionRepository;
import com.bansaiyai.bansaiyai.dto.DashboardDTO;
import com.bansaiyai.bansaiyai.entity.SavingTransaction;
import java.util.ArrayList;
import java.util.Comparator;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

  private final MemberRepository memberRepository;
  private final LoanRepository loanRepository;
  private final SavingRepository savingRepository;
  private final PaymentRepository paymentRepository;
  private final SavingTransactionRepository savingTransactionRepository;
  private final com.bansaiyai.bansaiyai.repository.AccountingRepository accountingRepository;

  /**
   * Get current fiscal period status
   */
  public com.bansaiyai.bansaiyai.dto.dashboard.FiscalPeriodDTO getCurrentFiscalPeriod() {
    try {
      // For now, return current month and OPEN status
      // In a real implementation, this would query system_config table
      LocalDate now = LocalDate.now();
      String period = now.getMonth().toString() + " " + now.getYear();
      String status = "OPEN"; // Default to OPEN

      return new com.bansaiyai.bansaiyai.dto.dashboard.FiscalPeriodDTO(period, status);
    } catch (Exception e) {
      log.error("Error getting fiscal period: {}", e.getMessage());
      return new com.bansaiyai.bansaiyai.dto.dashboard.FiscalPeriodDTO("Unknown", "CLOSED");
    }
  }

  /**
   * Search members by query string
   * Searches across member_id, name, and id_card fields
   */
  public List<com.bansaiyai.bansaiyai.dto.dashboard.MemberSearchResultDTO> searchMembers(String query, int limit) {
    try {
      if (query == null || query.trim().isEmpty()) {
        return List.of();
      }

      // Search using repository method
      List<Member> members = memberRepository.searchMembers(query.trim());

      // Convert to DTOs and limit results
      return members.stream()
          .limit(limit)
          .map(this::convertToSearchResultDTO)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error searching members with query '{}': {}", query, e.getMessage());
      return List.of();
    }
  }

  /**
   * Get member financial information for teller action card
   * Includes savings balance, loan principal, and loan status
   */
  public com.bansaiyai.bansaiyai.dto.dashboard.MemberFinancialsDTO getMemberFinancials(Long memberId) {
    try {
      // Get member's savings balance
      BigDecimal savingsBalance = savingRepository.sumBalancesByMemberId(memberId);
      if (savingsBalance == null) {
        savingsBalance = BigDecimal.ZERO;
      }

      // Get member's active loan information
      List<Loan> activeLoans = loanRepository.findByMemberIdAndStatus(memberId, LoanStatus.ACTIVE);

      BigDecimal loanPrincipal = BigDecimal.ZERO;
      String loanStatus = "NO_LOAN";

      if (!activeLoans.isEmpty()) {
        // Get the most recent active loan
        Loan activeLoan = activeLoans.get(0);
        loanPrincipal = activeLoan.getOutstandingBalance();
        loanStatus = activeLoan.getStatus().name();
      }

      return new com.bansaiyai.bansaiyai.dto.dashboard.MemberFinancialsDTO(
          savingsBalance,
          loanPrincipal,
          loanStatus);
    } catch (Exception e) {
      log.error("Error getting member financials for member {}: {}", memberId, e.getMessage());
      return new com.bansaiyai.bansaiyai.dto.dashboard.MemberFinancialsDTO(
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          "ERROR");
    }
  }

  /**
   * Calculate cash box tally for the current date
   * Includes total inflows, outflows, and net cash
   */
  public com.bansaiyai.bansaiyai.dto.dashboard.CashBoxDTO calculateCashBoxTally() {
    return calculateCashBoxTally(LocalDate.now());
  }

  /**
   * Calculate cash box tally for a specific date
   * Includes total inflows, outflows, and net cash
   */
  public com.bansaiyai.bansaiyai.dto.dashboard.CashBoxDTO calculateCashBoxTally(LocalDate date) {
    try {
      // Calculate total inflows from savings transactions (deposits)
      BigDecimal savingsInflows = savingTransactionRepository.findAll().stream()
          .filter(txn -> txn.getTransactionDate().equals(date))
          .filter(txn -> txn.isCredit() && !txn.getIsReversed())
          .map(com.bansaiyai.bansaiyai.entity.SavingTransaction::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Calculate total outflows from savings transactions (withdrawals)
      BigDecimal savingsOutflows = savingTransactionRepository.findAll().stream()
          .filter(txn -> txn.getTransactionDate().equals(date))
          .filter(txn -> txn.isDebit() && !txn.getIsReversed())
          .map(com.bansaiyai.bansaiyai.entity.SavingTransaction::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Calculate inflows from loan payments (principal, interest, fees)
      BigDecimal loanPaymentInflows = paymentRepository.findAll().stream()
          .filter(payment -> payment.getPaymentDate() != null && payment.getPaymentDate().equals(date))
          .filter(payment -> payment.getPaymentStatus() == com.bansaiyai.bansaiyai.entity.enums.PaymentStatus.COMPLETED)
          .filter(payment -> payment.isLoanPayment())
          .map(payment -> {
            BigDecimal total = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
            if (payment.getInterestAmount() != null) {
              total = total.add(payment.getInterestAmount());
            }
            if (payment.getFeeAmount() != null) {
              total = total.add(payment.getFeeAmount());
            }
            if (payment.getPenaltyAmount() != null) {
              total = total.add(payment.getPenaltyAmount());
            }
            return total;
          })
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Calculate outflows from loan disbursements
      BigDecimal loanDisbursementOutflows = loanRepository.findAll().stream()
          .filter(loan -> loan.getDisbursementDate() != null && loan.getDisbursementDate().equals(date))
          .filter(loan -> loan.getStatus() == com.bansaiyai.bansaiyai.entity.enums.LoanStatus.ACTIVE)
          .map(com.bansaiyai.bansaiyai.entity.Loan::getPrincipalAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Calculate totals
      BigDecimal totalIn = savingsInflows.add(loanPaymentInflows);
      BigDecimal totalOut = savingsOutflows.add(loanDisbursementOutflows);
      BigDecimal netCash = totalIn.subtract(totalOut);

      return new com.bansaiyai.bansaiyai.dto.dashboard.CashBoxDTO(
          totalIn,
          totalOut,
          netCash,
          date);
    } catch (Exception e) {
      log.error("Error calculating cash box tally for date {}: {}", date, e.getMessage());
      return new com.bansaiyai.bansaiyai.dto.dashboard.CashBoxDTO(
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          date);
    }
  }

  /**
   * Convert Member entity to MemberSearchResultDTO
   */
  private com.bansaiyai.bansaiyai.dto.dashboard.MemberSearchResultDTO convertToSearchResultDTO(Member member) {
    return new com.bansaiyai.bansaiyai.dto.dashboard.MemberSearchResultDTO(
        member.getId(),
        member.getName().split(" ")[0], // First name (simplified)
        member.getName().contains(" ") ? member.getName().substring(member.getName().indexOf(" ") + 1) : "", // Last
                                                                                                             // name
        member.getPhotoPath() != null ? member.getPhotoPath() : "/default-avatar.png",
        member.getIsActive() ? "Active" : "Inactive");
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

      // Calculate monthly revenue (interest + fees + penalties)
      BigDecimal monthlyRevenue = paymentRepository.sumRevenueByDateRange(startOfMonth, today);

      // Calculate new members this month
      long newMembersThisMonth = memberRepository.countByCreatedAtBetween(startOfMonth.atStartOfDay(),
          today.atTime(23, 59, 59));

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
          .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
          .newMembersThisMonth(newMembersThisMonth)
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

      log.debug("System health check: {} members, {} active loans, {} overdue payments",
          totalMembers, activeLoans, overduePayments);

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

  /**
   * Get recent activities for the dashboard
   */
  public List<DashboardDTO.ActivityItem> getRecentActivities(int limit) {
    List<DashboardDTO.ActivityItem> activities = new ArrayList<>();

    // Fetch recent members
    List<Member> recentMembers = memberRepository.findTop10ByOrderByCreatedAtDesc();
    activities.addAll(recentMembers.stream()
        .map(this::mapMemberToActivity)
        .collect(Collectors.toList()));

    // Fetch recent loans
    List<Loan> recentLoans = loanRepository.findTop10ByOrderByCreatedAtDesc();
    activities.addAll(recentLoans.stream()
        .map(this::mapLoanToActivity)
        .collect(Collectors.toList()));

    // Fetch recent transactions
    List<SavingTransaction> recentTransactions = savingTransactionRepository
        .findRecentTransactions(org.springframework.data.domain.PageRequest.of(0, 10));
    activities.addAll(recentTransactions.stream()
        .map(this::mapTransactionToActivity)
        .collect(Collectors.toList()));

    // Sort by timestamp desc and limit
    return activities.stream()
        .sorted(Comparator.comparing(DashboardDTO.ActivityItem::getTimestamp).reversed())
        .limit(limit)
        .collect(Collectors.toList());
  }

  private DashboardDTO.ActivityItem mapMemberToActivity(Member member) {
    return DashboardDTO.ActivityItem.builder()
        .id("MEM-" + member.getId())
        .type(DashboardDTO.ActivityType.MEMBER)
        .action("New Member Joined")
        .description("Member " + member.getName() + " joined the cooperative.")
        .performedBy(member.getCreatedBy())
        .timestamp(member.getCreatedAt())
        .entityId(String.valueOf(member.getId()))
        .entityName(member.getName())
        .build();
  }

  private DashboardDTO.ActivityItem mapLoanToActivity(Loan loan) {
    return DashboardDTO.ActivityItem.builder()
        .id("LOAN-" + loan.getId())
        .type(DashboardDTO.ActivityType.LOAN)
        .action("Loan Application")
        .description("Loan application for " + loan.getMember().getName() + " (" + loan.getLoanType() + ")")
        .performedBy(loan.getCreatedBy())
        .timestamp(loan.getCreatedAt())
        .entityId(String.valueOf(loan.getId()))
        .entityName(loan.getLoanNumber())
        .build();
  }

  private DashboardDTO.ActivityItem mapTransactionToActivity(SavingTransaction transaction) {
    return DashboardDTO.ActivityItem.builder()
        .id("TXN-" + transaction.getId())
        .type(DashboardDTO.ActivityType.SAVINGS)
        .action(transaction.getTransactionType().name())
        .description(transaction.getDescription())
        .performedBy(transaction.getCreatedBy())
        .timestamp(transaction.getTransactionDate().atStartOfDay())
        .entityId(String.valueOf(transaction.getId()))
        .entityName(transaction.getTransactionNumber())
        .build();
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
    private BigDecimal monthlyRevenue;
    private Long newMembersThisMonth;
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
      private BigDecimal monthlyRevenue;
      private Long newMembersThisMonth;
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

      public AdminDashboardStatsBuilder monthlyRevenue(BigDecimal monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
        return this;
      }

      public AdminDashboardStatsBuilder newMembersThisMonth(Long newMembersThisMonth) {
        this.newMembersThisMonth = newMembersThisMonth;
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
        stats.monthlyRevenue = this.monthlyRevenue;
        stats.newMembersThisMonth = this.newMembersThisMonth;
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
          .monthlyRevenue(BigDecimal.ZERO)
          .newMembersThisMonth(0L)
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

  /**
   * Get member growth chart data
   */
  public DashboardDTO.ChartData getMemberGrowthChartData(String period) {
    // Mock implementation for now - replace with actual DB queries later
    List<String> labels = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun");
    List<Number> data = List.of(12, 19, 3, 5, 2, 3);

    DashboardDTO.DataSet dataset = DashboardDTO.DataSet.builder()
        .label("New Members")
        .data(data)
        .borderColor("rgb(75, 192, 192)")
        .backgroundColor("rgba(75, 192, 192, 0.5)")
        .fill(true)
        .build();

    return DashboardDTO.ChartData.builder()
        .labels(labels)
        .datasets(List.of(dataset))
        .build();
  }

  /**
   * Get loan portfolio chart data
   */
  public DashboardDTO.ChartData getLoanPortfolioChartData(String period) {
    // Mock implementation
    List<String> labels = List.of("Personal", "Business", "Education", "Emergency");
    List<Number> data = List.of(12, 19, 3, 5);

    DashboardDTO.DataSet dataset = DashboardDTO.DataSet.builder()
        .label("# of Loans")
        .data(data)
        .backgroundColor("#FF6384") // Simplified for single color or handle array in frontend
        .build();

    return DashboardDTO.ChartData.builder()
        .labels(labels)
        .datasets(List.of(dataset))
        .build();
  }

  /**
   * Get savings growth chart data
   */
  public DashboardDTO.ChartData getSavingsGrowthChartData(String period) {
    // Mock implementation
    List<String> labels = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun");
    List<Number> data = List.of(50000, 75000, 100000, 120000, 150000, 180000);

    DashboardDTO.DataSet dataset = DashboardDTO.DataSet.builder()
        .label("Total Savings (THB)")
        .data(data)
        .backgroundColor("rgba(53, 162, 235, 0.5)")
        .build();

    return DashboardDTO.ChartData.builder()
        .labels(labels)
        .datasets(List.of(dataset))
        .build();
  }

  /**
   * Get recent transactions for Officer Dashboard transaction feed
   * Combines savings transactions and loan payments with member information
   */
  public List<com.bansaiyai.bansaiyai.dto.dashboard.TransactionDTO> getRecentTransactions(int limit) {
    try {
      List<com.bansaiyai.bansaiyai.dto.dashboard.TransactionDTO> transactions = new ArrayList<>();

      // Get recent savings transactions
      List<SavingTransaction> savingTransactions = savingTransactionRepository.findAll().stream()
          .filter(txn -> !txn.getIsReversed())
          .sorted((a, b) -> {
            // Sort by transaction date descending, then by created date
            int dateCompare = b.getTransactionDate().compareTo(a.getTransactionDate());
            if (dateCompare != 0)
              return dateCompare;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
          })
          .limit(limit * 2) // Get more to ensure we have enough after combining
          .collect(Collectors.toList());

      // Convert savings transactions to DTOs
      for (SavingTransaction txn : savingTransactions) {
        String memberName = txn.getSavingAccount() != null && txn.getSavingAccount().getMember() != null
            ? txn.getSavingAccount().getMember().getName()
            : "Unknown Member";

        LocalDateTime timestamp = txn.getTransactionDate().atStartOfDay();
        if (txn.getCreatedAt() != null) {
          timestamp = txn.getCreatedAt();
        }

        transactions.add(new com.bansaiyai.bansaiyai.dto.dashboard.TransactionDTO(
            txn.getId(),
            timestamp,
            memberName,
            txn.getTransactionType().name(),
            txn.getAmount()));
      }

      // Get recent loan payments
      List<Payment> payments = paymentRepository.findAll().stream()
          .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
          .filter(p -> p.isLoanPayment())
          .sorted((a, b) -> {
            // Sort by payment date descending
            if (a.getPaymentDate() == null)
              return 1;
            if (b.getPaymentDate() == null)
              return -1;
            int dateCompare = b.getPaymentDate().compareTo(a.getPaymentDate());
            if (dateCompare != 0)
              return dateCompare;
            if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
              return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
            return 0;
          })
          .limit(limit * 2)
          .collect(Collectors.toList());

      // Convert payments to DTOs
      for (Payment payment : payments) {
        String memberName = payment.getMember() != null
            ? payment.getMember().getName()
            : "Unknown Member";

        LocalDateTime timestamp = payment.getPaymentDate() != null
            ? payment.getPaymentDate().atStartOfDay()
            : LocalDateTime.now();
        if (payment.getCreatedAt() != null) {
          timestamp = payment.getCreatedAt();
        }

        transactions.add(new com.bansaiyai.bansaiyai.dto.dashboard.TransactionDTO(
            payment.getId(),
            timestamp,
            memberName,
            payment.getPaymentType().name(),
            payment.getAmount()));
      }

      // Sort all transactions by timestamp descending and limit
      return transactions.stream()
          .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
          .limit(limit)
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("Error getting recent transactions: {}", e.getMessage());
      return List.of();
    }
  }

  /**
   * Calculate trial balance for the current fiscal period
   * Returns total debits, credits, variance, and balanced status
   */
  public com.bansaiyai.bansaiyai.dto.dashboard.TrialBalanceDTO calculateTrialBalance() {
    try {
      // Get current fiscal period
      com.bansaiyai.bansaiyai.dto.dashboard.FiscalPeriodDTO fiscalPeriod = getCurrentFiscalPeriod();
      String period = fiscalPeriod.getPeriod();

      // Convert period format from "MONTH YEAR" to "YYYY-MM" for database query
      String fiscalPeriodKey = convertPeriodToKey(period);

      // Calculate total debits and credits for the fiscal period
      BigDecimal totalDebits = accountingRepository.sumDebitsByFiscalPeriod(fiscalPeriodKey);
      BigDecimal totalCredits = accountingRepository.sumCreditsByFiscalPeriod(fiscalPeriodKey);

      // Handle null values
      if (totalDebits == null) {
        totalDebits = BigDecimal.ZERO;
      }
      if (totalCredits == null) {
        totalCredits = BigDecimal.ZERO;
      }

      // Calculate variance (debits - credits)
      BigDecimal variance = totalDebits.subtract(totalCredits);

      // Check if balanced (variance is zero)
      boolean isBalanced = variance.compareTo(BigDecimal.ZERO) == 0;

      return new com.bansaiyai.bansaiyai.dto.dashboard.TrialBalanceDTO(
          totalDebits,
          totalCredits,
          variance,
          isBalanced,
          period);
    } catch (Exception e) {
      log.error("Error calculating trial balance: {}", e.getMessage());
      return new com.bansaiyai.bansaiyai.dto.dashboard.TrialBalanceDTO(
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          true,
          "Unknown");
    }
  }

  /**
   * Convert period string from "MONTH YEAR" format to "YYYY-MM" format
   * Example: "AUGUST 2023" -> "2023-08"
   */
  private String convertPeriodToKey(String period) {
    try {
      String[] parts = period.split(" ");
      if (parts.length != 2) {
        // If format is unexpected, use current date
        LocalDate now = LocalDate.now();
        return String.format("%d-%02d", now.getYear(), now.getMonthValue());
      }

      String monthStr = parts[0].toUpperCase();
      String yearStr = parts[1];

      // Map month name to number
      int monthNum = switch (monthStr) {
        case "JANUARY" -> 1;
        case "FEBRUARY" -> 2;
        case "MARCH" -> 3;
        case "APRIL" -> 4;
        case "MAY" -> 5;
        case "JUNE" -> 6;
        case "JULY" -> 7;
        case "AUGUST" -> 8;
        case "SEPTEMBER" -> 9;
        case "OCTOBER" -> 10;
        case "NOVEMBER" -> 11;
        case "DECEMBER" -> 12;
        default -> LocalDate.now().getMonthValue();
      };

      return String.format("%s-%02d", yearStr, monthNum);
    } catch (Exception e) {
      log.error("Error converting period to key: {}", e.getMessage());
      LocalDate now = LocalDate.now();
      return String.format("%d-%02d", now.getYear(), now.getMonthValue());
    }
  }

  /**
   * Count unclassified transactions (transactions without accounting entries)
   * An unclassified transaction is one that doesn't have a corresponding
   * AccountingEntry
   */
  public int countUnclassifiedTransactions() {
    try {
      // Get all saving transaction IDs
      List<Long> savingTransactionIds = savingTransactionRepository.findAll().stream()
          .filter(txn -> !txn.getIsReversed())
          .map(SavingTransaction::getId)
          .collect(Collectors.toList());

      // Get all payment IDs
      List<Long> paymentIds = paymentRepository.findAll().stream()
          .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
          .map(Payment::getId)
          .collect(Collectors.toList());

      // Get all accounting entries that reference transactions
      List<AccountingEntry> accountingEntries = accountingRepository.findAll();

      // Extract referenced transaction IDs from accounting entries
      List<Long> classifiedSavingTransactionIds = accountingEntries.stream()
          .filter(entry -> "SAVINGS".equals(entry.getReferenceType())
              || "SAVING_TRANSACTION".equals(entry.getReferenceType()))
          .map(AccountingEntry::getReferenceId)
          .filter(id -> id != null)
          .collect(Collectors.toList());

      List<Long> classifiedPaymentIds = accountingEntries.stream()
          .filter(
              entry -> "PAYMENT".equals(entry.getReferenceType()) || "LOAN_PAYMENT".equals(entry.getReferenceType()))
          .map(AccountingEntry::getReferenceId)
          .filter(id -> id != null)
          .collect(Collectors.toList());

      // Count unclassified transactions
      long unclassifiedSavingTransactions = savingTransactionIds.stream()
          .filter(id -> !classifiedSavingTransactionIds.contains(id))
          .count();

      long unclassifiedPayments = paymentIds.stream()
          .filter(id -> !classifiedPaymentIds.contains(id))
          .count();

      return (int) (unclassifiedSavingTransactions + unclassifiedPayments);
    } catch (Exception e) {
      log.error("Error counting unclassified transactions: {}", e.getMessage());
      return 0;
    }
  }

  /**
   * Get quick actions based on user role
   */
  public List<DashboardDTO.QuickAction> getQuickActions(String role) {
    List<DashboardDTO.QuickAction> actions = new ArrayList<>();

    if (role == null) {
      return actions;
    }

    switch (role) {
      case "PRESIDENT":
      case "SECRETARY":
      case "OFFICER":
        actions.add(DashboardDTO.QuickAction.builder()
            .id("approve-loans")
            .title("Approve Loans")
            .description("Review pending loan applications")
            .icon("CheckCircle")
            .route("/loans/approvals")
            .permission("LOAN_APPROVE")
            .priority(1)
            .build());
        actions.add(DashboardDTO.QuickAction.builder()
            .id("verify-payments")
            .title("Verify Payments")
            .description("Verify incoming payments")
            .icon("Payment")
            .route("/payments/verification")
            .permission("PAYMENT_VERIFY")
            .priority(2)
            .build());
        actions.add(DashboardDTO.QuickAction.builder()
            .id("add-member")
            .title("Add Member")
            .description("Register a new member")
            .icon("PersonAdd")
            .route("/members/new")
            .permission("MEMBER_CREATE")
            .priority(3)
            .build());
        break;
      case "MEMBER":
        actions.add(DashboardDTO.QuickAction.builder()
            .id("apply-loan")
            .title("Apply for Loan")
            .description("Submit a new loan application")
            .icon("MonetizationOn")
            .route("/loans/apply")
            .permission("LOAN_CREATE")
            .priority(1)
            .build());
        actions.add(DashboardDTO.QuickAction.builder()
            .id("view-statement")
            .title("View Statement")
            .description("View account statement")
            .icon("Receipt")
            .route("/statements")
            .permission("STATEMENT_VIEW")
            .priority(2)
            .build());
        break;
    }

    return actions;
  }

  /**
   * Generate financial statement previews for Secretary Dashboard
   * Includes income vs expenses bar chart and asset distribution pie chart
   */
  public com.bansaiyai.bansaiyai.dto.dashboard.FinancialPreviewsDTO generateFinancialPreviews() {
    try {
      // Get current fiscal period
      com.bansaiyai.bansaiyai.dto.dashboard.FiscalPeriodDTO fiscalPeriod = getCurrentFiscalPeriod();
      String period = fiscalPeriod.getPeriod();
      String fiscalPeriodKey = convertPeriodToKey(period);

      // Get all accounting entries for the fiscal period
      List<AccountingEntry> entries = accountingRepository.findByFiscalPeriod(fiscalPeriodKey);

      // Calculate income data (4xxx = Income accounts)
      BigDecimal interestIncome = entries.stream()
          .filter(e -> e.getAccountCode() != null && e.getAccountCode().startsWith("4"))
          .map(e -> {
            BigDecimal credit = e.getCredit() != null ? e.getCredit() : BigDecimal.ZERO;
            BigDecimal debit = e.getDebit() != null ? e.getDebit() : BigDecimal.ZERO;
            return credit.subtract(debit); // Net income (credits - debits)
          })
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Calculate expenses (5xxx = Expense accounts)
      BigDecimal expenses = entries.stream()
          .filter(e -> e.getAccountCode() != null && e.getAccountCode().startsWith("5"))
          .map(e -> {
            BigDecimal debit = e.getDebit() != null ? e.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = e.getCredit() != null ? e.getCredit() : BigDecimal.ZERO;
            return debit.subtract(credit); // Net expenses (debits - credits)
          })
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Calculate asset distribution (1xxx = Asset accounts)
      BigDecimal cashAndBank = entries.stream()
          .filter(e -> e.getAccountCode() != null &&
              (e.getAccountCode().startsWith("100") || e.getAccountCode().startsWith("101")))
          .map(e -> {
            BigDecimal debit = e.getDebit() != null ? e.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = e.getCredit() != null ? e.getCredit() : BigDecimal.ZERO;
            return debit.subtract(credit); // Net asset balance (debits - credits)
          })
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal loansReceivable = entries.stream()
          .filter(e -> e.getAccountCode() != null && e.getAccountCode().startsWith("12"))
          .map(e -> {
            BigDecimal debit = e.getDebit() != null ? e.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = e.getCredit() != null ? e.getCredit() : BigDecimal.ZERO;
            return debit.subtract(credit);
          })
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal otherAssets = entries.stream()
          .filter(e -> e.getAccountCode() != null &&
              e.getAccountCode().startsWith("1") &&
              !e.getAccountCode().startsWith("100") &&
              !e.getAccountCode().startsWith("101") &&
              !e.getAccountCode().startsWith("12"))
          .map(e -> {
            BigDecimal debit = e.getDebit() != null ? e.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = e.getCredit() != null ? e.getCredit() : BigDecimal.ZERO;
            return debit.subtract(credit);
          })
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Build income chart data
      List<String> incomeLabels = List.of("Income", "Expenses");
      java.util.Map<String, Object> incomeDatasets = new java.util.HashMap<>();
      incomeDatasets.put("income", interestIncome);
      incomeDatasets.put("expenses", expenses);

      com.bansaiyai.bansaiyai.dto.dashboard.FinancialPreviewsDTO.IncomeChartData incomeChartData = new com.bansaiyai.bansaiyai.dto.dashboard.FinancialPreviewsDTO.IncomeChartData(
          incomeLabels,
          incomeDatasets);

      // Build balance sheet chart data
      List<String> balanceLabels = List.of("Cash & Bank", "Loans Receivable", "Other Assets");
      java.util.Map<String, Object> balanceDatasets = new java.util.HashMap<>();
      balanceDatasets.put("cashAndBank", cashAndBank);
      balanceDatasets.put("loansReceivable", loansReceivable);
      balanceDatasets.put("otherAssets", otherAssets);

      com.bansaiyai.bansaiyai.dto.dashboard.FinancialPreviewsDTO.BalanceSheetChartData balanceChartData = new com.bansaiyai.bansaiyai.dto.dashboard.FinancialPreviewsDTO.BalanceSheetChartData(
          balanceLabels,
          balanceDatasets);

      return new com.bansaiyai.bansaiyai.dto.dashboard.FinancialPreviewsDTO(
          incomeChartData,
          balanceChartData);
    } catch (Exception e) {
      log.error("Error generating financial previews: {}", e.getMessage());
      // Return empty data on error
      return new com.bansaiyai.bansaiyai.dto.dashboard.FinancialPreviewsDTO();
    }
  }
}
