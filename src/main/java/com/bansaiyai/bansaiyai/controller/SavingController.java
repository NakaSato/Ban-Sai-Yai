package com.bansaiyai.bansaiyai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bansaiyai.bansaiyai.dto.SavingAccountRequest;
import com.bansaiyai.bansaiyai.dto.SavingResponse;
import com.bansaiyai.bansaiyai.service.SavingService;
import com.bansaiyai.bansaiyai.service.SavingService.AccountStatistics;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * REST controller for managing savings accounts.
 * Provides endpoints for account creation, transactions, and management
 * operations.
 */
@RestController
@RequestMapping("/savings")
@Slf4j
public class SavingController {

  private final SavingService savingService;

  // Manual logger for Lombok compatibility
  private static final Logger log = LoggerFactory.getLogger(SavingController.class);

  // Manual constructor
  public SavingController(SavingService savingService) {
    this.savingService = savingService;
  }

  /**
   * Create a new savings account
   */
  @PostMapping("/accounts")
  @PreAuthorize("hasRole('OFFICER') or hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<SavingResponse> createAccount(@Valid @RequestBody SavingAccountRequest request) {
    try {
      String currentUser = getCurrentUsername();
      SavingResponse response = savingService.createAccount(request, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error creating savings account: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get account by ID
   */
  @GetMapping("/accounts/{id}")
  @PreAuthorize("hasRole('OFFICER') or hasRole('ADMIN') or hasRole('PRESIDENT') or @authMemberId.equals(authentication.name)")
  public ResponseEntity<SavingResponse> getAccount(@PathVariable Long id) {
    try {
      SavingResponse response = savingService.getAccount(id);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error getting savings account {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get account by account number
   */
  @GetMapping("/accounts/by-number/{accountNumber}")
  @PreAuthorize("hasRole('OFFICER') or hasRole('ADMIN') or hasRole('PRESIDENT') or @authMemberId.equals(authentication.name)")
  public ResponseEntity<SavingResponse> getAccountByNumber(@PathVariable String accountNumber) {
    try {
      SavingResponse response = savingService.getAccountByNumber(accountNumber);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error getting savings account {}: {}", accountNumber, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get all accounts for a member
   */
  @GetMapping("/accounts/member/{memberId}")
  @PreAuthorize("hasRole('OFFICER') or hasRole('ADMIN') or hasRole('PRESIDENT') or @authMemberId.equals(memberId)")
  public ResponseEntity<Page<SavingResponse>> getAccountsByMember(
      @PathVariable Long memberId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "openingDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    try {
      Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
      Pageable pageable = PageRequest.of(page, size, sort);
      Page<SavingResponse> accounts = savingService.getAccountsByMember(memberId, pageable);
      return ResponseEntity.ok(accounts);
    } catch (Exception e) {
      log.error("Error getting accounts for member {}: {}", memberId, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get all accounts (admin only)
   */
  @GetMapping("/accounts")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<Page<SavingResponse>> getAllAccounts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "openingDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    try {
      Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
      Pageable pageable = PageRequest.of(page, size, sort);
      Page<SavingResponse> accounts = savingService.getAllAccounts(pageable);
      return ResponseEntity.ok(accounts);
    } catch (Exception e) {
      log.error("Error getting all savings accounts: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Deposit to account
   */
  @PostMapping("/accounts/{id}/deposit")
  @PreAuthorize("hasRole('OFFICER') or hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<SavingResponse> deposit(
      @PathVariable Long id,
      @RequestParam BigDecimal amount,
      @RequestParam(required = false) String description) {

    try {
      String currentUser = getCurrentUsername();
      SavingResponse response = savingService.deposit(id, amount, description, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error depositing to account {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Withdraw from account
   */
  @PostMapping("/accounts/{id}/withdraw")
  @PreAuthorize("hasRole('OFFICER') or hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<SavingResponse> withdraw(
      @PathVariable Long id,
      @RequestParam BigDecimal amount,
      @RequestParam(required = false) String description) {

    try {
      String currentUser = getCurrentUsername();
      SavingResponse response = savingService.withdraw(id, amount, description, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error withdrawing from account {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Freeze account
   */
  @PostMapping("/accounts/{id}/freeze")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<SavingResponse> freezeAccount(
      @PathVariable Long id,
      @RequestParam String reason) {

    try {
      String currentUser = getCurrentUsername();
      SavingResponse response = savingService.freezeAccount(id, reason, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error freezing account {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Unfreeze account
   */
  @PostMapping("/accounts/{id}/unfreeze")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<SavingResponse> unfreezeAccount(@PathVariable Long id) {
    try {
      String currentUser = getCurrentUsername();
      SavingResponse response = savingService.unfreezeAccount(id, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error unfreezing account {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Close account
   */
  @PostMapping("/accounts/{id}/close")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<SavingResponse> closeAccount(@PathVariable Long id) {
    try {
      String currentUser = getCurrentUsername();
      SavingResponse response = savingService.closeAccount(id, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error closing account {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Calculate and credit interest to accounts
   */
  @PostMapping("/accounts/calculate-interest")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, String>> calculateInterest(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate asOfDate) {

    try {
      savingService.calculateAndCreditInterest(asOfDate);
      return ResponseEntity.ok(Map.of(
          "message", "Interest calculation completed successfully",
          "asOfDate", asOfDate.toString()));
    } catch (Exception e) {
      log.error("Error calculating interest: {}", e.getMessage());
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  /**
   * Get account statistics
   */
  @GetMapping("/accounts/{id}/statistics")
  @PreAuthorize("hasRole('OFFICER') or hasRole('ADMIN') or hasRole('PRESIDENT') or @authMemberId.equals(authentication.name)")
  public ResponseEntity<AccountStatistics> getAccountStatistics(
      @PathVariable Long id,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

    try {
      if (startDate == null) {
        startDate = LocalDate.now().minusMonths(6);
      }
      if (endDate == null) {
        endDate = LocalDate.now();
      }

      AccountStatistics stats = savingService.getAccountStatistics(id, startDate, endDate);
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      log.error("Error getting statistics for account {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get savings portfolio summary (admin)
   */
  @GetMapping("/portfolio/summary")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getPortfolioSummary() {
    try {
      // This would typically include total deposits, total accounts, interest rates,
      // etc.
      // For now, return a basic summary
      return ResponseEntity.ok(Map.of(
          "message", "Portfolio summary endpoint",
          "note", "Detailed portfolio statistics to be implemented"));
    } catch (Exception e) {
      log.error("Error getting portfolio summary: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get global saving statistics
   */
  @GetMapping("/statistics")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PRESIDENT') or hasRole('SECRETARY')")
  public ResponseEntity<SavingService.SavingStatistics> getSavingStatistics() {
    try {
      SavingService.SavingStatistics stats = savingService.getSavingStatistics();
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      log.error("Error getting saving statistics: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get current user from security context
   */
  private String getCurrentUsername() {
    // In a real implementation, you'd get this from Spring Security context
    return "system"; // Placeholder
  }
}
