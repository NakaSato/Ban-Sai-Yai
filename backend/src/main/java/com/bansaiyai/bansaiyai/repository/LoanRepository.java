package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

  // ============================================
  // UUID-based Query Methods (NEW - For secure API use)
  // ============================================

  /**
   * Find loan by UUID - Primary method for external API use
   */
  Optional<Loan> findByUuid(UUID uuid);

  /**
   * Check if loan exists by UUID
   */
  boolean existsByUuid(UUID uuid);

  /**
   * Delete loan by UUID
   */
  void deleteByUuid(UUID uuid);

  // ============================================
  // Legacy Long-based Methods (Keep for internal use)
  // ============================================

  Optional<Loan> findByLoanNumber(String loanNumber);

  List<Loan> findByMemberIdAndStatus(Long memberId, LoanStatus status);

  Page<Loan> findByMemberId(Long memberId, Pageable pageable);

  Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

  Page<Loan> findByLoanType(LoanType loanType, Pageable pageable);

  Page<Loan> findByLoanTypeAndStatus(LoanType loanType, LoanStatus status, Pageable pageable);

  @Query("SELECT l FROM Loan l WHERE " +
      "l.loanNumber LIKE %:keyword% OR " +
      "l.member.name LIKE %:keyword% OR " +
      "l.purpose LIKE %:keyword%")
  Page<Loan> searchLoans(@Param("keyword") String keyword, Pageable pageable);

  long countByStatus(LoanStatus status);

  long countByLoanTypeAndStatus(LoanType loanType, LoanStatus status);

  @Query("SELECT SUM(l.outstandingBalance) FROM Loan l WHERE l.status = :status")
  BigDecimal sumOutstandingBalancesByStatus(@Param("status") LoanStatus status);

  @Query("SELECT COALESCE(SUM(l.outstandingBalance), 0) FROM Loan l WHERE l.status IN :statuses")
  BigDecimal sumOutstandingBalancesByStatuses(@Param("statuses") List<LoanStatus> statuses);

  default BigDecimal sumOutstandingBalances() {
    return sumOutstandingBalancesByStatuses(List.of(LoanStatus.ACTIVE, LoanStatus.DEFAULTED));
  }

  List<Loan> findByStatusIn(List<LoanStatus> statuses);

  @Query("SELECT l FROM Loan l WHERE l.status = :status AND l.maturityDate < CURRENT_DATE")
  List<Loan> findOverdueLoansByStatus(@Param("status") LoanStatus status);

  List<Loan> findByDisbursementDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

  @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId AND l.status IN :statuses")
  List<Loan> findByMemberIdAndStatusIn(@Param("memberId") Long memberId, @Param("statuses") List<LoanStatus> statuses);

  boolean existsByLoanNumber(String loanNumber);

  boolean existsByMemberIdAndStatus(Long memberId, LoanStatus status);

  // Dashboard query methods
  @Query("SELECT SUM(l.principalAmount) FROM Loan l WHERE l.status IN :statuses")
  java.math.BigDecimal sumLoanAmountByStatus(@Param("statuses") List<LoanStatus> statuses);

  @Query("SELECT COALESCE(SUM(l.outstandingBalance), 0) FROM Loan l WHERE l.status = :status AND l.maturityDate < CURRENT_DATE")
  java.math.BigDecimal sumOverdueAmount();

  List<Loan> findByMemberIdOrderByCreatedAtDesc(Long memberId);

  @Query("SELECT l FROM Loan l WHERE l.status = :status AND l.approvedDate = :date AND l.approvedBy = :officer")
  List<Loan> findByProcessedDateAndProcessedBy(@Param("date") java.time.LocalDate date,
      @Param("officer") String officer);

  @Query("SELECT l FROM Loan l WHERE l.approvedDate BETWEEN :startDate AND :endDate AND l.approvedBy = :officer")
  List<Loan> findByProcessedDateBetweenAndProcessedBy(@Param("startDate") java.time.LocalDate startDate,
      @Param("endDate") java.time.LocalDate endDate, @Param("officer") String officer);

  @Query("SELECT l FROM Loan l WHERE l.status = :status AND l.maturityDate < CURRENT_DATE")
  List<Loan> findOverdueLoans();

  List<Loan> findTop10ByOrderByCreatedAtDesc();
}
