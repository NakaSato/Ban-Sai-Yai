package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.CashReconciliationRequest;
import com.bansaiyai.bansaiyai.dto.CashReconciliationResponse;
import com.bansaiyai.bansaiyai.dto.DiscrepancyApprovalRequest;
import com.bansaiyai.bansaiyai.entity.CashReconciliation;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.CashReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for cash reconciliation operations
 */
@RestController
@RequestMapping("/api/cash-reconciliation")
@RequiredArgsConstructor
@Slf4j
public class CashReconciliationController {

  private final CashReconciliationService cashReconciliationService;
  private final com.bansaiyai.bansaiyai.security.UserContext userContext;

  /**
   * Create a new cash reconciliation (Officer only)
   */
  @PostMapping
  @PreAuthorize("hasRole('OFFICER')")
  public ResponseEntity<Map<String, Object>> createReconciliation(
      @Valid @RequestBody CashReconciliationRequest request) {

    try {
      User officer = getCurrentUser();

      CashReconciliation reconciliation = cashReconciliationService.createReconciliation(
          request.getPhysicalCount(),
          officer,
          request.getNotes());

      CashReconciliationResponse response = CashReconciliationResponse.fromEntity(reconciliation);

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("reconciliation", response);
      responseBody.put("message", "Cash reconciliation created successfully");
      responseBody.put("hasVariance", cashReconciliationService.hasVariance(reconciliation));

      return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

    } catch (IllegalStateException e) {
      log.error("Error creating cash reconciliation: {}", e.getMessage());
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Validation failed");
      error.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (Exception e) {
      log.error("Unexpected error creating cash reconciliation", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to create cash reconciliation");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Get pending reconciliations for Secretary review
   */
  @GetMapping("/pending")
  @PreAuthorize("hasRole('SECRETARY')")
  public ResponseEntity<Map<String, Object>> getPendingReconciliations() {
    try {
      List<CashReconciliation> pendingReconciliations = cashReconciliationService.getPendingReconciliations();

      List<CashReconciliationResponse> responses = pendingReconciliations.stream()
          .map(CashReconciliationResponse::fromEntity)
          .collect(Collectors.toList());

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("pendingReconciliations", responses);
      responseBody.put("count", responses.size());

      return ResponseEntity.ok(responseBody);

    } catch (Exception e) {
      log.error("Error fetching pending reconciliations", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to fetch pending reconciliations");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Approve a discrepancy (Secretary only)
   */
  @PostMapping("/{id}/approve")
  @PreAuthorize("hasRole('SECRETARY')")
  public ResponseEntity<Map<String, Object>> approveDiscrepancy(
      @PathVariable Long id,
      @Valid @RequestBody DiscrepancyApprovalRequest request) {

    try {
      if (!"APPROVE".equals(request.getAction())) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Invalid action");
        error.put("message", "Action must be 'APPROVE'");
        return ResponseEntity.badRequest().body(error);
      }

      User secretary = getCurrentUser();

      CashReconciliation reconciliation = cashReconciliationService.approveDiscrepancy(
          id, secretary, request.getNotes());

      CashReconciliationResponse response = CashReconciliationResponse.fromEntity(reconciliation);

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("reconciliation", response);
      responseBody.put("message", "Discrepancy approved successfully");

      return ResponseEntity.ok(responseBody);

    } catch (IllegalArgumentException e) {
      log.error("Error approving discrepancy: {}", e.getMessage());
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Not found");
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (IllegalStateException e) {
      log.error("Error approving discrepancy: {}", e.getMessage());
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Validation failed");
      error.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (Exception e) {
      log.error("Unexpected error approving discrepancy", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to approve discrepancy");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Reject a discrepancy (Secretary only)
   */
  @PostMapping("/{id}/reject")
  @PreAuthorize("hasRole('SECRETARY')")
  public ResponseEntity<Map<String, Object>> rejectDiscrepancy(
      @PathVariable Long id,
      @Valid @RequestBody DiscrepancyApprovalRequest request) {

    try {
      if (!"REJECT".equals(request.getAction())) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Invalid action");
        error.put("message", "Action must be 'REJECT'");
        return ResponseEntity.badRequest().body(error);
      }

      if (request.getNotes() == null || request.getNotes().trim().isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Validation failed");
        error.put("message", "Rejection reason is required");
        return ResponseEntity.badRequest().body(error);
      }

      User secretary = getCurrentUser();

      CashReconciliation reconciliation = cashReconciliationService.rejectDiscrepancy(
          id, secretary, request.getNotes());

      CashReconciliationResponse response = CashReconciliationResponse.fromEntity(reconciliation);

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("reconciliation", response);
      responseBody.put("message", "Discrepancy rejected successfully");

      return ResponseEntity.ok(responseBody);

    } catch (IllegalArgumentException e) {
      log.error("Error rejecting discrepancy: {}", e.getMessage());
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Not found");
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (IllegalStateException e) {
      log.error("Error rejecting discrepancy: {}", e.getMessage());
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Validation failed");
      error.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (Exception e) {
      log.error("Unexpected error rejecting discrepancy", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to reject discrepancy");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Check if day can be closed
   */
  @GetMapping("/can-close-day")
  @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
  public ResponseEntity<Map<String, Object>> canCloseDay() {
    try {
      boolean canClose = cashReconciliationService.canCloseDay();

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("canCloseDay", canClose);
      responseBody.put("message", canClose ? "Day can be closed - no pending reconciliations with variance"
          : "Day cannot be closed - pending reconciliations with variance exist");

      return ResponseEntity.ok(responseBody);

    } catch (Exception e) {
      log.error("Error checking if day can be closed", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to check day close status");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Get reconciliation by ID
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
  public ResponseEntity<Map<String, Object>> getReconciliationById(@PathVariable Long id) {
    try {
      CashReconciliation reconciliation = cashReconciliationService.getReconciliationById(id);
      CashReconciliationResponse response = CashReconciliationResponse.fromEntity(reconciliation);

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("reconciliation", response);

      return ResponseEntity.ok(responseBody);

    } catch (IllegalArgumentException e) {
      log.error("Error fetching reconciliation: {}", e.getMessage());
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Not found");
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
      log.error("Unexpected error fetching reconciliation", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to fetch reconciliation");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Helper method to get current user from authentication
   * In a real implementation, you'd fetch this from database
   */
  /**
   * Helper method to get current user from security context
   */
  private User getCurrentUser() {
    return userContext.getCurrentUser();
  }
}
