package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.CashReconciliation;
import com.bansaiyai.bansaiyai.entity.CashReconciliation.ReconciliationStatus;
import com.bansaiyai.bansaiyai.entity.User;
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
 * Repository for CashReconciliation entity operations.
 * Provides methods for querying cash reconciliations with date and status queries.
 */
@Repository
public interface CashReconciliationRepository extends JpaRepository<CashReconciliation, Long> {

  /**
   * Find cash reconciliation by date.
   *
   * @param date the reconciliation date
   * @return an Optional containing the reconciliation if found
   */
  Optional<CashReconciliation> findByDate(LocalDate date);

  /**
   * Find cash reconciliations by status.
   *
   * @param status the reconciliation status
   * @param pageable pagination information
   * @return a page of cash reconciliations with the given status
   */
  Page<CashReconciliation> findByStatus(ReconciliationStatus status, Pageable pageable);

  /**
   * Find all pending cash reconciliations.
   *
   * @return a list of pending cash reconciliations
   */
  @Query("SELECT c FROM CashReconciliation c WHERE c.status = 'PENDING' ORDER BY c.date DESC")
  List<CashReconciliation> findPendingReconciliations();

  /**
   * Find cash reconciliations by date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param pageable pagination information
   * @return a page of cash reconciliations within the date range
   */
  Page<CashReconciliation> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

  /**
   * Find cash reconciliations by officer.
   *
   * @param officer the officer user
   * @param pageable pagination information
   * @return a page of cash reconciliations created by the officer
   */
  Page<CashReconciliation> findByOfficer(User officer, Pageable pageable);

  /**
   * Find cash reconciliations by officer ID.
   *
   * @param officerId the officer user ID
   * @param pageable pagination information
   * @return a page of cash reconciliations created by the officer
   */
  Page<CashReconciliation> findByOfficerId(Long officerId, Pageable pageable);

  /**
   * Find cash reconciliations by secretary.
   *
   * @param secretary the secretary user
   * @param pageable pagination information
   * @return a page of cash reconciliations reviewed by the secretary
   */
  Page<CashReconciliation> findBySecretary(User secretary, Pageable pageable);

  /**
   * Find cash reconciliations with variance (non-zero variance).
   *
   * @return a list of cash reconciliations with variance
   */
  @Query("SELECT c FROM CashReconciliation c WHERE c.variance <> 0 ORDER BY c.date DESC")
  List<CashReconciliation> findReconciliationsWithVariance();

  /**
   * Find cash reconciliations with variance greater than a threshold.
   *
   * @param threshold the variance threshold
   * @return a list of cash reconciliations with variance exceeding the threshold
   */
  @Query("SELECT c FROM CashReconciliation c WHERE ABS(c.variance) > :threshold ORDER BY c.date DESC")
  List<CashReconciliation> findReconciliationsWithVarianceAbove(@Param("threshold") BigDecimal threshold);

  /**
   * Check if a reconciliation exists for a specific date.
   *
   * @param date the reconciliation date
   * @return true if a reconciliation exists for the date, false otherwise
   */
  boolean existsByDate(LocalDate date);

  /**
   * Check if there are any pending reconciliations.
   *
   * @return true if there are pending reconciliations, false otherwise
   */
  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CashReconciliation c " +
         "WHERE c.status = 'PENDING'")
  boolean hasPendingReconciliations();

  /**
   * Check if there are any pending reconciliations for a specific date or earlier.
   *
   * @param date the date to check
   * @return true if there are pending reconciliations on or before the date, false otherwise
   */
  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CashReconciliation c " +
         "WHERE c.status = 'PENDING' AND c.date <= :date")
  boolean hasPendingReconciliationsOnOrBefore(@Param("date") LocalDate date);

  /**
   * Get the most recent reconciliation.
   *
   * @return an Optional containing the most recent reconciliation if found
   */
  @Query("SELECT c FROM CashReconciliation c ORDER BY c.date DESC LIMIT 1")
  Optional<CashReconciliation> findMostRecent();

  /**
   * Get the most recent approved reconciliation.
   *
   * @return an Optional containing the most recent approved reconciliation if found
   */
  @Query("SELECT c FROM CashReconciliation c WHERE c.status = 'APPROVED' ORDER BY c.date DESC LIMIT 1")
  Optional<CashReconciliation> findMostRecentApproved();

  /**
   * Count reconciliations by status.
   *
   * @param status the reconciliation status
   * @return the count of reconciliations with the given status
   */
  long countByStatus(ReconciliationStatus status);

  /**
   * Count reconciliations by date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return the count of reconciliations within the date range
   */
  long countByDateBetween(LocalDate startDate, LocalDate endDate);

  /**
   * Find reconciliations by status and date range.
   *
   * @param status the reconciliation status
   * @param startDate the start date
   * @param endDate the end date
   * @return a list of reconciliations matching the criteria
   */
  @Query("SELECT c FROM CashReconciliation c WHERE c.status = :status " +
         "AND c.date BETWEEN :startDate AND :endDate ORDER BY c.date DESC")
  List<CashReconciliation> findByStatusAndDateBetween(@Param("status") ReconciliationStatus status,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

  /**
   * Get total variance for a date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return the sum of variances within the date range
   */
  @Query("SELECT COALESCE(SUM(c.variance), 0) FROM CashReconciliation c " +
         "WHERE c.date BETWEEN :startDate AND :endDate")
  BigDecimal sumVarianceByDateBetween(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);

  /**
   * Find the last N reconciliations.
   *
   * @param pageable pagination information with limit
   * @return a list of the most recent reconciliations
   */
  @Query("SELECT c FROM CashReconciliation c ORDER BY c.date DESC")
  List<CashReconciliation> findRecentReconciliations(Pageable pageable);
}
