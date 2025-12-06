package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.Guarantor;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Guarantor entity operations.
 * Provides methods for querying guarantors and their relationships with loans and members.
 */
@Repository
public interface GuarantorRepository extends JpaRepository<Guarantor, Long> {

  /**
   * Check if a guarantor relationship exists for a specific loan and member.
   * This is used for relationship-based access control.
   *
   * @param loanId the loan ID
   * @param memberId the member ID
   * @return true if the member is a guarantor for the loan, false otherwise
   */
  @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Guarantor g " +
         "WHERE g.loan.id = :loanId AND g.member.id = :memberId")
  boolean existsByLoanIdAndMemberId(@Param("loanId") Long loanId, @Param("memberId") Long memberId);

  /**
   * Check if an active guarantor relationship exists for a specific loan and member.
   *
   * @param loanId the loan ID
   * @param memberId the member ID
   * @return true if the member is an active guarantor for the loan, false otherwise
   */
  @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Guarantor g " +
         "WHERE g.loan.id = :loanId AND g.member.id = :memberId AND g.isActive = true")
  boolean existsActiveByLoanIdAndMemberId(@Param("loanId") Long loanId, @Param("memberId") Long memberId);

  /**
   * Find a guarantor by guarantor number.
   *
   * @param guarantorNumber the guarantor number
   * @return an Optional containing the guarantor if found
   */
  Optional<Guarantor> findByGuarantorNumber(String guarantorNumber);

  /**
   * Find all guarantors for a specific loan.
   *
   * @param loan the loan
   * @return a list of guarantors for the loan
   */
  List<Guarantor> findByLoan(Loan loan);

  /**
   * Find all guarantors for a specific loan by loan ID.
   *
   * @param loanId the loan ID
   * @return a list of guarantors for the loan
   */
  @Query("SELECT g FROM Guarantor g WHERE g.loan.id = :loanId")
  List<Guarantor> findByLoanId(@Param("loanId") Long loanId);

  /**
   * Find all active guarantors for a specific loan.
   *
   * @param loanId the loan ID
   * @return a list of active guarantors for the loan
   */
  @Query("SELECT g FROM Guarantor g WHERE g.loan.id = :loanId AND g.isActive = true")
  List<Guarantor> findActiveByLoanId(@Param("loanId") Long loanId);

  /**
   * Find all loans guaranteed by a specific member.
   *
   * @param member the member
   * @return a list of guarantors representing loans guaranteed by the member
   */
  List<Guarantor> findByMember(Member member);

  /**
   * Find all loans guaranteed by a specific member by member ID.
   *
   * @param memberId the member ID
   * @return a list of guarantors representing loans guaranteed by the member
   */
  @Query("SELECT g FROM Guarantor g WHERE g.member.id = :memberId")
  List<Guarantor> findByMemberId(@Param("memberId") Long memberId);

  /**
   * Find all active loans guaranteed by a specific member.
   *
   * @param memberId the member ID
   * @return a list of active guarantors representing loans guaranteed by the member
   */
  @Query("SELECT g FROM Guarantor g WHERE g.member.id = :memberId AND g.isActive = true")
  List<Guarantor> findActiveByMemberId(@Param("memberId") Long memberId);

  /**
   * Find guarantors by loan and member.
   *
   * @param loan the loan
   * @param member the member
   * @return an Optional containing the guarantor if found
   */
  Optional<Guarantor> findByLoanAndMember(Loan loan, Member member);

  /**
   * Find guarantors by loan ID and member ID.
   *
   * @param loanId the loan ID
   * @param memberId the member ID
   * @return an Optional containing the guarantor if found
   */
  @Query("SELECT g FROM Guarantor g WHERE g.loan.id = :loanId AND g.member.id = :memberId")
  Optional<Guarantor> findByLoanIdAndMemberId(@Param("loanId") Long loanId, @Param("memberId") Long memberId);

  /**
   * Find all active guarantors.
   *
   * @param pageable pagination information
   * @return a page of active guarantors
   */
  Page<Guarantor> findByIsActive(Boolean isActive, Pageable pageable);

  /**
   * Count guarantors for a specific loan.
   *
   * @param loanId the loan ID
   * @return the count of guarantors for the loan
   */
  @Query("SELECT COUNT(g) FROM Guarantor g WHERE g.loan.id = :loanId")
  long countByLoanId(@Param("loanId") Long loanId);

  /**
   * Count active guarantors for a specific loan.
   *
   * @param loanId the loan ID
   * @return the count of active guarantors for the loan
   */
  @Query("SELECT COUNT(g) FROM Guarantor g WHERE g.loan.id = :loanId AND g.isActive = true")
  long countActiveByLoanId(@Param("loanId") Long loanId);

  /**
   * Count loans guaranteed by a specific member.
   *
   * @param memberId the member ID
   * @return the count of loans guaranteed by the member
   */
  @Query("SELECT COUNT(g) FROM Guarantor g WHERE g.member.id = :memberId")
  long countByMemberId(@Param("memberId") Long memberId);

  /**
   * Count active loans guaranteed by a specific member.
   *
   * @param memberId the member ID
   * @return the count of active loans guaranteed by the member
   */
  @Query("SELECT COUNT(g) FROM Guarantor g WHERE g.member.id = :memberId AND g.isActive = true")
  long countActiveByMemberId(@Param("memberId") Long memberId);

  /**
   * Get total guaranteed amount for a member.
   *
   * @param memberId the member ID
   * @return the sum of guaranteed amounts for the member
   */
  @Query("SELECT COALESCE(SUM(g.guaranteedAmount), 0) FROM Guarantor g " +
         "WHERE g.member.id = :memberId AND g.isActive = true")
  BigDecimal sumGuaranteedAmountByMemberId(@Param("memberId") Long memberId);

  /**
   * Find guarantors with guarantee end date before a specific date.
   *
   * @param date the date to check
   * @return a list of guarantors with guarantee end date before the date
   */
  @Query("SELECT g FROM Guarantor g WHERE g.guaranteeEndDate < :date AND g.isActive = true")
  List<Guarantor> findExpiredGuarantees(@Param("date") LocalDate date);

  /**
   * Find guarantors with guarantee start date after a specific date.
   *
   * @param date the date to check
   * @return a list of guarantors with guarantee start date after the date
   */
  @Query("SELECT g FROM Guarantor g WHERE g.guaranteeStartDate > :date")
  List<Guarantor> findFutureGuarantees(@Param("date") LocalDate date);

  /**
   * Check if a guarantor number exists.
   *
   * @param guarantorNumber the guarantor number
   * @return true if the guarantor number exists, false otherwise
   */
  boolean existsByGuarantorNumber(String guarantorNumber);

  /**
   * Find guarantors by relationship type.
   *
   * @param relationship the relationship type
   * @return a list of guarantors with the relationship type
   */
  List<Guarantor> findByRelationship(String relationship);

  /**
   * Find the most recent guarantors.
   *
   * @param pageable pagination information with limit
   * @return a list of the most recent guarantors
   */
  @Query("SELECT g FROM Guarantor g ORDER BY g.createdAt DESC")
  List<Guarantor> findRecentGuarantors(Pageable pageable);
}
