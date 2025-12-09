package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.GuarantorAccessEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for guarantor access operations
 * Provides endpoints for viewing guaranteed loans and relationship-based access
 * control
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class GuarantorController {

  private final GuarantorAccessEvaluator guarantorAccessEvaluator;

  /**
   * Get all loans guaranteed by a specific member
   * Only the member themselves or users with proper permissions can access this
   * endpoint
   */
  @GetMapping("/members/{id}/guaranteed-loans")
  @PreAuthorize("hasRole('MEMBER') or hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getGuaranteedLoans(
      @PathVariable Long id,
      Authentication authentication) {

    try {
      User currentUser = getCurrentUser(authentication);

      // Members can only view their own guaranteed loans
      if (currentUser.getRole() == User.Role.MEMBER &&
          !currentUser.getMember().getId().equals(id)) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Access denied");
        error.put("message", "You can only view your own guaranteed loans");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
      }

      List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(id);

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("guaranteedLoans", guaranteedLoans);
      responseBody.put("count", guaranteedLoans.size());
      responseBody.put("memberId", id);

      return ResponseEntity.ok(responseBody);

    } catch (Exception e) {
      log.error("Error fetching guaranteed loans for member: {}", id, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to fetch guaranteed loans");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Get a specific loan with guarantor access control
   * Users can view the loan if they are:
   * 1. The borrower (loan owner)
   * 2. An active guarantor for the loan
   * 3. Users with administrative privileges (Secretary, President)
   */
  @GetMapping("/loans/{id}")
  @PreAuthorize("hasRole('MEMBER') or hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getLoanById(
      @PathVariable Long id,
      Authentication authentication) {

    try {
      User currentUser = getCurrentUser(authentication);
      Loan loan = getLoanById(id);

      if (loan == null) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Not found");
        error.put("message", "Loan not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
      }

      // Check access rights
      boolean canView = false;
      if (currentUser.getRole() == User.Role.MEMBER) {
        // Members can view if they are borrower or guarantor
        canView = guarantorAccessEvaluator.canViewLoan(currentUser, id);
      } else {
        // Officers, Secretaries, Presidents can view any loan
        canView = true;
      }

      if (!canView) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Access denied");
        error.put("message", "You do not have permission to view this loan");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
      }

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("loan", loan);
      responseBody.put("canView", true);
      responseBody.put("accessType", getAccessType(currentUser, loan));

      return ResponseEntity.ok(responseBody);

    } catch (Exception e) {
      log.error("Error fetching loan: {}", id, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to fetch loan");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Helper method to determine access type for logging/debugging
   */
  private String getAccessType(User user, Loan loan) {
    if (user.getMember() != null && loan.getMember() != null &&
        user.getMember().getId().equals(loan.getMember().getId())) {
      return "BORROWER";
    } else if (guarantorAccessEvaluator.isActiveGuarantor(
        user.getMember().getId(), loan.getId())) {
      return "GUARANTOR";
    } else if (user.getRole() != User.Role.MEMBER) {
      return "ADMINISTRATIVE";
    }
    return "NONE";
  }

  /**
   * Helper method to get loan by ID (would typically be injected from service)
   * For demo purposes, we'll create a mock implementation
   */
  private Loan getLoanById(Long id) {
    // In a real implementation, this would use LoanService or LoanRepository
    // For now, we'll return null to demonstrate the access control logic
    return null;
  }

  /**
   * Helper method to get current user from authentication
   * In a real implementation, you'd fetch this from database
   */
  private User getCurrentUser(Authentication authentication) {
    // This is a simplified approach. In a real application, you'd:
    // 1. Get the current user from database
    // 2. Return the actual User object

    // For demo purposes, we'll create a mock user
    User mockUser = new User();
    mockUser.setUsername(authentication.getName());

    // Set role based on the authenticated user
    // In reality, this would come from the database
    mockUser.setRole(User.Role.MEMBER); // Default to member for demo

    return mockUser;
  }
}
