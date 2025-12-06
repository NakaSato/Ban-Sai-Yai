package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.AccountingEntry;
import com.bansaiyai.bansaiyai.entity.CashReconciliation;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.AccountingRepository;
import com.bansaiyai.bansaiyai.repository.CashReconciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing cash reconciliation operations.
 * Handles variance calculation, day close authorization, and discrepancy
 * approval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CashReconciliationService {

  private final CashReconciliationRepository cashReconciliationRepository;
  private final AccountingRepository accountingRepository;
  private final AuditService auditService;

  private static final String CASH_ACCOUNT_CODE = "1001"; // Typical cash account code

  /**
   * Calculate database balance for a specific date
   * 
   * @param date the date to calculate balance for
   * @return the database balance from accounting entries
   */
  @Transactional(readOnly = true)
  public BigDecimal calculateDatabaseBalance(LocalDate date) {
    log.debug("Calculating database balance for date: {}", date);

    // Sum all debit entries for cash account up to the given date
    BigDecimal totalDebits = accountingRepository
        .findByAccountCodeAndTransactionDateBefore(CASH_ACCOUNT_CODE, date)
        .stream()
        .map(entry -> entry.getDebit() != null ? entry.getDebit() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Sum all credit entries for cash account up to given date
    BigDecimal totalCredits = accountingRepository
        .findByAccountCodeAndTransactionDateBefore(CASH_ACCOUNT_CODE, date)
        .stream()
        .map(entry -> entry.getCredit() != null ? entry.getCredit() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // For cash account: balance = debits - credits
    BigDecimal balance = totalDebits.subtract(totalCredits);

    log.debug("Database balance calculated: {} (Debits: {}, Credits: {})",
        balance, totalDebits, totalCredits);

    return balance;
  }

  /**
   * Create a new cash reconciliation
   * 
   * @param physicalCount the physical cash count
   * @param officer       the officer creating the reconciliation
   * @param notes         optional notes
   * @return the created reconciliation
   */
  @Transactional
  public CashReconciliation createReconciliation(BigDecimal physicalCount, User officer, String notes) {
    log.info("Creating cash reconciliation for officer: {}", officer.getUsername());

    LocalDate today = LocalDate.now();

    // Check if reconciliation already exists for today
    if (cashReconciliationRepository.existsByDate(today)) {
      throw new IllegalStateException("Cash reconciliation already exists for date: " + today);
    }

    // Calculate database balance
    BigDecimal databaseBalance = calculateDatabaseBalance(today);

    // Calculate variance
    BigDecimal variance = physicalCount.subtract(databaseBalance);

    // Create reconciliation
    CashReconciliation reconciliation = CashReconciliation.builder()
        .date(today)
        .officer(officer)
        .physicalCount(physicalCount)
        .databaseBalance(databaseBalance)
        .variance(variance)
        .status(CashReconciliation.ReconciliationStatus.PENDING)
        .build();

    CashReconciliation savedReconciliation = cashReconciliationRepository.save(reconciliation);

    // Log the action
    auditService.logAction(
        officer,
        "CREATE_CASH_RECONCILIATION",
        "CashReconciliation",
        savedReconciliation.getReconciliationId(),
        null,
        Map.of(
            "physicalCount", physicalCount.toString(),
            "databaseBalance", databaseBalance.toString(),
            "variance", variance.toString(),
            "notes", notes != null ? notes : ""));

    log.info("Cash reconciliation created with ID: {}, variance: {}",
        savedReconciliation.getReconciliationId(), variance);

    return savedReconciliation;
  }

  /**
   * Check if reconciliation has variance
   * 
   * @param reconciliation the reconciliation to check
   * @return true if variance exists (non-zero), false otherwise
   */
  public boolean hasVariance(CashReconciliation reconciliation) {
    return reconciliation.getVariance() != null &&
        reconciliation.getVariance().compareTo(BigDecimal.ZERO) != 0;
  }

  /**
   * Approve a discrepancy with accounting entry
   * 
   * @param reconciliationId the reconciliation ID
   * @param secretary        the secretary approving
   * @param notes            approval notes
   * @return the updated reconciliation
   */
  @Transactional
  public CashReconciliation approveDiscrepancy(Long reconciliationId, User secretary, String notes) {
    log.info("Approving discrepancy for reconciliation: {} by secretary: {}",
        reconciliationId, secretary.getUsername());

    CashReconciliation reconciliation = cashReconciliationRepository.findById(reconciliationId)
        .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found: " + reconciliationId));

    if (reconciliation.getStatus() != CashReconciliation.ReconciliationStatus.PENDING) {
      throw new IllegalStateException("Reconciliation is not pending: " + reconciliationId);
    }

    if (!hasVariance(reconciliation)) {
      throw new IllegalStateException("No variance to approve: " + reconciliationId);
    }

    // Create accounting entry for the variance
    createVarianceAccountingEntry(reconciliation, secretary);

    // Update reconciliation
    reconciliation.setStatus(CashReconciliation.ReconciliationStatus.APPROVED);
    reconciliation.setSecretary(secretary);
    reconciliation.setSecretaryNotes(notes);
    reconciliation.setApprovedAt(LocalDateTime.now());

    CashReconciliation savedReconciliation = cashReconciliationRepository.save(reconciliation);

    // Log the action
    auditService.logAction(
        secretary,
        "APPROVE_DISCREPANCY",
        "CashReconciliation",
        reconciliationId,
        null,
        Map.of(
            "variance", reconciliation.getVariance().toString(),
            "notes", notes != null ? notes : ""));

    log.info("Discrepancy approved for reconciliation: {}", reconciliationId);

    return savedReconciliation;
  }

  /**
   * Reject a discrepancy
   * 
   * @param reconciliationId the reconciliation ID
   * @param secretary        the secretary rejecting
   * @param notes            rejection reason
   * @return the updated reconciliation
   */
  @Transactional
  public CashReconciliation rejectDiscrepancy(Long reconciliationId, User secretary, String notes) {
    log.info("Rejecting discrepancy for reconciliation: {} by secretary: {}",
        reconciliationId, secretary.getUsername());

    CashReconciliation reconciliation = cashReconciliationRepository.findById(reconciliationId)
        .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found: " + reconciliationId));

    if (reconciliation.getStatus() != CashReconciliation.ReconciliationStatus.PENDING) {
      throw new IllegalStateException("Reconciliation is not pending: " + reconciliationId);
    }

    // Update reconciliation
    reconciliation.setStatus(CashReconciliation.ReconciliationStatus.REJECTED);
    reconciliation.setSecretary(secretary);
    reconciliation.setSecretaryNotes(notes);
    reconciliation.setApprovedAt(LocalDateTime.now());

    CashReconciliation savedReconciliation = cashReconciliationRepository.save(reconciliation);

    // Log the action
    auditService.logAction(
        secretary,
        "REJECT_DISCREPANCY",
        "CashReconciliation",
        reconciliationId,
        null,
        Map.of(
            "variance", reconciliation.getVariance().toString(),
            "notes", notes != null ? notes : ""));

    log.info("Discrepancy rejected for reconciliation: {}", reconciliationId);

    return savedReconciliation;
  }

  /**
   * Check if day can be closed (no pending reconciliations with variance)
   * 
   * @return true if day can be closed, false otherwise
   */
  @Transactional(readOnly = true)
  public boolean canCloseDay() {
    boolean hasPendingWithVariance = cashReconciliationRepository.findPendingReconciliations()
        .stream()
        .anyMatch(this::hasVariance);

    return !hasPendingWithVariance;
  }

  /**
   * Get pending reconciliations for secretary review
   * 
   * @return list of pending reconciliations
   */
  @Transactional(readOnly = true)
  public List<CashReconciliation> getPendingReconciliations() {
    return cashReconciliationRepository.findPendingReconciliations();
  }

  /**
   * Get reconciliation by ID
   * 
   * @param id the reconciliation ID
   * @return the reconciliation
   */
  @Transactional(readOnly = true)
  public CashReconciliation getReconciliationById(Long id) {
    return cashReconciliationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found: " + id));
  }

  /**
   * Create accounting entry for variance adjustment
   */
  private void createVarianceAccountingEntry(CashReconciliation reconciliation, User secretary) {
    BigDecimal variance = reconciliation.getVariance();
    String fiscalPeriod = reconciliation.getDate().getYear() + "-" +
        String.format("%02d", reconciliation.getDate().getMonthValue());

    AccountingEntry entry;
    String description;

    if (variance.compareTo(BigDecimal.ZERO) > 0) {
      // Physical count > Database balance - credit cash account
      description = "Cash reconciliation variance adjustment - excess cash";
      entry = new AccountingEntry(
          fiscalPeriod,
          CASH_ACCOUNT_CODE,
          "Cash",
          BigDecimal.ZERO,
          variance,
          reconciliation.getDate(),
          description);
    } else {
      // Physical count < Database balance - debit cash account
      description = "Cash reconciliation variance adjustment - cash shortage";
      entry = new AccountingEntry(
          fiscalPeriod,
          CASH_ACCOUNT_CODE,
          "Cash",
          variance.abs(),
          BigDecimal.ZERO,
          reconciliation.getDate(),
          description);
    }

    entry.setReferenceType("CASH_RECONCILIATION");
    entry.setReferenceId(reconciliation.getReconciliationId());

    accountingRepository.save(entry);

    log.debug("Created variance accounting entry: {}", entry.getDescription());
  }
}
