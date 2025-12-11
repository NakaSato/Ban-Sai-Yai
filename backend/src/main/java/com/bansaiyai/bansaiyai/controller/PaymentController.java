package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.PaymentRequest;
import com.bansaiyai.bansaiyai.dto.PaymentResponse;
import com.bansaiyai.bansaiyai.dto.CompositePaymentRequest;
import com.bansaiyai.bansaiyai.dto.CompositeTransactionResponse;
import com.bansaiyai.bansaiyai.service.PaymentService;
import com.bansaiyai.bansaiyai.service.TransactionService;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing payment operations.
 * Provides endpoints for payment processing, verification, and management.
 */
@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final TransactionService transactionService;
  private final UserRepository userRepository;
  private final com.bansaiyai.bansaiyai.security.UserContext userContext;

  /**
   * Create a new payment
   */
  @PostMapping
  @PreAuthorize("hasRole('OFFICER')")
  public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
    try {
      String currentUser = userContext.getCurrentUsername();
      PaymentResponse response = paymentService.createPayment(request, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error creating payment: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/process")
  @PreAuthorize("hasRole('OFFICER')")
  public ResponseEntity<CompositeTransactionResponse> processCompositePaymentLegacy(
      @Valid @RequestBody CompositePaymentRequest request) {
    return processCompositePayment(request); // Reuse the existing logic
  }

  /**
   * Process a composite payment (Share + Loan)
   */
  @PostMapping("/composite")
  @PreAuthorize("hasRole('OFFICER')")
  public ResponseEntity<CompositeTransactionResponse> processCompositePayment(
      @Valid @RequestBody CompositePaymentRequest request) {
    try {
      String username = userContext.getCurrentUsername();
      User user = userRepository.findByUsername(username)
          .orElseThrow(() -> new RuntimeException("User not found: " + username));

      CompositeTransactionResponse response = transactionService.processCompositePayment(request, user);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error processing composite payment: {}", e.getMessage());
      return ResponseEntity.badRequest().body(CompositeTransactionResponse.builder()
          .status("FAILED")
          .message(e.getMessage())
          .build());
    }
  }

  /**
   * Get payment by ID
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
    try {
      PaymentResponse response = paymentService.getPayment(id);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error getting payment {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get payment by payment number
   */
  @GetMapping("/by-number/{paymentNumber}")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
  public ResponseEntity<PaymentResponse> getPaymentByNumber(@PathVariable String paymentNumber) {
    try {
      PaymentResponse response = paymentService.getPaymentByNumber(paymentNumber);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error getting payment {}: {}", paymentNumber, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get payments by member
   */
  @GetMapping("/member/{memberId}")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT') or @userContext.isCurrentMember(#memberId)")
  public ResponseEntity<Page<PaymentResponse>> getPaymentsByMember(
      @PathVariable Long memberId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "paymentDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    try {
      Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
      Pageable pageable = PageRequest.of(page, size, sort);
      Page<PaymentResponse> payments = paymentService.getPaymentsByMember(memberId, pageable);
      return ResponseEntity.ok(payments);
    } catch (Exception e) {
      log.error("Error getting payments for member {}: {}", memberId, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get payments by loan
   */
  @GetMapping("/loan/{loanId}")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
  public ResponseEntity<Page<PaymentResponse>> getPaymentsByLoan(
      @PathVariable Long loanId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "paymentDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    try {
      Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
      Pageable pageable = PageRequest.of(page, size, sort);
      Page<PaymentResponse> payments = paymentService.getPaymentsByLoan(loanId, pageable);
      return ResponseEntity.ok(payments);
    } catch (Exception e) {
      log.error("Error getting payments for loan {}: {}", loanId, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get payments by savings account
   */
  @GetMapping("/savings/{savingAccountId}")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
  public ResponseEntity<Page<PaymentResponse>> getPaymentsBySavingsAccount(
      @PathVariable Long savingAccountId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "paymentDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    try {
      Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
      Pageable pageable = PageRequest.of(page, size, sort);
      Page<PaymentResponse> payments = paymentService.getPaymentsBySavingsAccount(savingAccountId, pageable);
      return ResponseEntity.ok(payments);
    } catch (Exception e) {
      log.error("Error getting payments for savings account {}: {}", savingAccountId, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get all payments (admin only)
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
  public ResponseEntity<Page<PaymentResponse>> getAllPayments(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "paymentDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    try {
      Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
      Pageable pageable = PageRequest.of(page, size, sort);
      Page<PaymentResponse> payments = paymentService.getAllPayments(pageable);
      return ResponseEntity.ok(payments);
    } catch (Exception e) {
      log.error("Error getting all payments: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Process a payment
   */
  @PostMapping("/{id}/process")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
  public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long id) {
    try {
      String currentUser = userContext.getCurrentUsername();
      PaymentResponse response = paymentService.processPayment(id, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error processing payment {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Verify a payment
   */
  @PostMapping("/{id}/verify")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<PaymentResponse> verifyPayment(@PathVariable Long id) {
    try {
      String currentUser = userContext.getCurrentUsername();
      PaymentResponse response = paymentService.verifyPayment(id, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error verifying payment {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Cancel a payment
   */
  @PostMapping("/{id}/cancel")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<PaymentResponse> cancelPayment(
      @PathVariable Long id,
      @RequestParam String reason) {
    try {
      String currentUser = userContext.getCurrentUsername();
      PaymentResponse response = paymentService.cancelPayment(id, reason, currentUser);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error cancelling payment {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get overdue payments
   */
  @GetMapping("/overdue")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
  public ResponseEntity<List<PaymentResponse>> getOverduePayments() {
    try {
      List<PaymentResponse> payments = paymentService.getOverduePayments();
      return ResponseEntity.ok(payments);
    } catch (Exception e) {
      log.error("Error getting overdue payments: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get pending payments
   */
  @GetMapping("/pending")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
  public ResponseEntity<List<PaymentResponse>> getPendingPayments() {
    try {
      List<PaymentResponse> payments = paymentService.getPendingPayments();
      return ResponseEntity.ok(payments);
    } catch (Exception e) {
      log.error("Error getting pending payments: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get payment statistics
   */
  @GetMapping("/statistics")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<PaymentService.PaymentStatistics> getPaymentStatistics(
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
    try {
      if (startDate == null) {
        startDate = LocalDate.now().minusMonths(1);
      }
      if (endDate == null) {
        endDate = LocalDate.now();
      }

      PaymentService.PaymentStatistics statistics = paymentService.getPaymentStatistics(startDate, endDate);
      return ResponseEntity.ok(statistics);
    } catch (Exception e) {
      log.error("Error getting payment statistics: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get payments by date range
   */
  @GetMapping("/by-date-range")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getPaymentsByDateRange(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      // This would typically use the repository date range query
      // For now, return a basic response
      return ResponseEntity.ok(Map.of(
          "message", "Date range payments endpoint",
          "startDate", startDate.toString(),
          "endDate", endDate.toString(),
          "note", "Detailed date range query to be implemented"));
    } catch (Exception e) {
      log.error("Error getting payments by date range: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get payment summary for a member
   */
  @GetMapping("/member/{memberId}/summary")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT') or @userContext.isCurrentMember(#memberId)")
  public ResponseEntity<Map<String, Object>> getMemberPaymentSummary(@PathVariable Long memberId) {
    try {
      // This would typically calculate summary statistics
      // For now, return a basic summary
      return ResponseEntity.ok(Map.of(
          "memberId", memberId,
          "message", "Member payment summary endpoint",
          "note", "Detailed payment summary to be implemented"));
    } catch (Exception e) {
      log.error("Error getting member payment summary: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Search payments
   */
  @GetMapping("/search")
  @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> searchPayments(
      @RequestParam String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      // This would typically implement a search query
      // For now, return a basic response
      return ResponseEntity.ok(Map.of(
          "query", query,
          "message", "Payment search endpoint",
          "note", "Search functionality to be implemented"));
    } catch (Exception e) {
      log.error("Error searching payments: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }
}
