package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.LoanApplicationRequest;
import com.bansaiyai.bansaiyai.dto.LoanResponse;
import com.bansaiyai.bansaiyai.dto.MemberLoanApplicationRequest;
import com.bansaiyai.bansaiyai.dto.LoanApprovalRequest;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.service.LoanService;
import com.bansaiyai.bansaiyai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/loans")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoanController {

  @Autowired
  private LoanService loanService;

  @Autowired
  private UserService userService;

  /**
   * Get current authenticated username from security context
   */
  private String getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null ? authentication.getName() : "system";
  }

  @PostMapping("/apply")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<LoanResponse> applyForLoan(@RequestBody LoanApplicationRequest request) {
    try {
      String createdBy = getCurrentUsername();
      LoanResponse loanResponse = loanService.createLoanApplication(request, createdBy);
      return ResponseEntity.ok(loanResponse);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Member self-service loan application endpoint.
   * Allows members to apply for loans using their own authenticated identity.
   * The member ID is derived from the authenticated user context.
   */
  @PostMapping("/member-apply")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public ResponseEntity<LoanResponse> applyForLoanAsMember(
      @RequestBody MemberLoanApplicationRequest request,
      Authentication authentication) {
    try {
      String username = authentication.getName();
      User currentUser = userService.findByUsername(username)
          .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

      if (currentUser.getMember() == null) {
        return ResponseEntity.badRequest().build();
      }

      // Convert MemberLoanApplicationRequest to LoanApplicationRequest
      // Use the authenticated member's ID instead of accepting from request
      LoanApplicationRequest loanRequest = LoanApplicationRequest.builder()
          .memberId(currentUser.getMember().getId())
          .loanType(request.getLoanType())
          .principalAmount(request.getPrincipalAmount())
          .termMonths(request.getTermMonths())
          .purpose(request.getPurpose())
          .guarantors(request.getGuarantors())
          .build();

      LoanResponse loanResponse = loanService.createLoanApplication(loanRequest, username);
      return ResponseEntity.ok(loanResponse);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/{loanId}/approve")
  @PreAuthorize("hasRole('ROLE_PRESIDENT')")
  public ResponseEntity<LoanResponse> approveLoan(@PathVariable Long loanId,
      @RequestBody LoanApprovalRequest approvalRequest) {
    try {
      String approvedBy = getCurrentUsername();
      LoanResponse loanResponse = loanService.approveLoan(loanId, approvalRequest, approvedBy);
      return ResponseEntity.ok(loanResponse);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/{loanId}/disburse")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<LoanResponse> disburseLoan(@PathVariable Long loanId) {
    try {
      String disbursedBy = getCurrentUsername();
      LoanResponse loanResponse = loanService.disburseLoan(loanId, disbursedBy);
      return ResponseEntity.ok(loanResponse);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/{loanId}/reject")
  @PreAuthorize("hasRole('ROLE_PRESIDENT')")
  public ResponseEntity<LoanResponse> rejectLoan(@PathVariable Long loanId,
      @RequestParam(value = "rejectionReason") String rejectionReason) {
    try {
      if (rejectionReason == null || rejectionReason.isBlank()) {
        return ResponseEntity.badRequest().build();
      }
      String rejectedBy = getCurrentUsername();
      LoanResponse loanResponse = loanService.rejectLoan(loanId, rejectionReason, rejectedBy);
      return ResponseEntity.ok(loanResponse);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<Page<LoanResponse>> getAllLoans(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort) {
    try {
      Sort sortObj = Sort.by(Sort.Direction.fromString(sort.contains("desc") ? "DESC" : "ASC"),
          sort.split(",")[0]);
      Pageable pageable = PageRequest.of(page, size, sortObj);
      Page<LoanResponse> loans = loanService.getAllLoans(pageable);
      return ResponseEntity.ok(loans);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get loan by UUID (SECURE - prevents ID enumeration)
   */
  @GetMapping("/{uuid}")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER')")
  public ResponseEntity<LoanResponse> getLoanById(@PathVariable UUID uuid) {
    try {
      LoanResponse loan = loanService.getLoanByUuid(uuid);
      return ResponseEntity.ok(loan);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/number/{loanNumber}")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER', 'ROLE_MEMBER')")
  public ResponseEntity<LoanResponse> getLoanByNumber(@PathVariable String loanNumber) {
    try {
      LoanResponse loan = loanService.getLoanByNumber(loanNumber);
      return ResponseEntity.ok(loan);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/member/{memberId}")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER', 'ROLE_MEMBER')")
  public ResponseEntity<Page<LoanResponse>> getLoansByMember(
      @PathVariable Long memberId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort) {
    try {
      Sort sortObj = Sort.by(Sort.Direction.fromString(sort.contains("desc") ? "DESC" : "ASC"),
          sort.split(",")[0]);
      Pageable pageable = PageRequest.of(page, size, sortObj);
      Page<LoanResponse> loans = loanService.getLoansByMember(memberId, pageable);
      return ResponseEntity.ok(loans);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/status/{status}")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<Page<LoanResponse>> getLoansByStatus(
      @PathVariable LoanStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort) {
    try {
      Sort sortObj = Sort.by(Sort.Direction.fromString(sort.contains("desc") ? "DESC" : "ASC"),
          sort.split(",")[0]);
      Pageable pageable = PageRequest.of(page, size, sortObj);
      Page<LoanResponse> loans = loanService.getLoansByStatus(status, pageable);
      return ResponseEntity.ok(loans);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/type/{loanType}")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<Page<LoanResponse>> getLoansByType(
      @PathVariable LoanType loanType,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort) {
    try {
      Sort sortObj = Sort.by(Sort.Direction.fromString(sort.contains("desc") ? "DESC" : "ASC"),
          sort.split(",")[0]);
      Pageable pageable = PageRequest.of(page, size, sortObj);
      Page<LoanResponse> loans = loanService.getLoansByType(loanType, pageable);
      return ResponseEntity.ok(loans);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<Page<LoanResponse>> searchLoans(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort) {
    try {
      Sort sortObj = Sort.by(Sort.Direction.fromString(sort.contains("desc") ? "DESC" : "ASC"),
          sort.split(",")[0]);
      Pageable pageable = PageRequest.of(page, size, sortObj);
      Page<LoanResponse> loans = loanService.searchLoans(keyword, pageable);
      return ResponseEntity.ok(loans);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{loanId}")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<LoanResponse> updateLoan(@PathVariable Long loanId,
      @RequestBody LoanApplicationRequest request) {
    try {
      String updatedBy = getCurrentUsername();
      LoanResponse loanResponse = loanService.updateLoan(loanId, request, updatedBy);
      return ResponseEntity.ok(loanResponse);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{loanId}")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY')")
  public ResponseEntity<Void> deleteLoan(@PathVariable Long loanId) {
    try {
      loanService.deleteLoan(loanId);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/statistics")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<LoanService.LoanStatistics> getLoanStatistics() {
    try {
      LoanService.LoanStatistics statistics = loanService.getLoanStatistics();
      return ResponseEntity.ok(statistics);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/eligibility/{memberId}")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<Boolean> checkEligibility(@PathVariable Long memberId,
      @RequestParam java.math.BigDecimal requestedAmount) {
    try {
      boolean eligible = loanService.isEligibleForNewLoan(memberId, requestedAmount);
      return ResponseEntity.ok(eligible);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{loanId}/payoff")
  @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT') or @loanSecurity.isLoanOwner(authentication, #loanId)")
  public ResponseEntity<java.util.Map<String, java.math.BigDecimal>> getPayoffQuote(
      @PathVariable Long loanId,
      @RequestParam(required = false) java.time.LocalDate date) {
    java.time.LocalDate targetDate = date != null ? date : java.time.LocalDate.now();
    return ResponseEntity.ok(loanService.calculatePayoff(loanId, targetDate));
  }

  @GetMapping("/pending")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER')")
  public ResponseEntity<Page<LoanResponse>> getPendingLoans(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort) {
    try {
      Sort sortObj = Sort.by(Sort.Direction.fromString(sort.contains("desc") ? "DESC" : "ASC"),
          sort.split(",")[0]);
      Pageable pageable = PageRequest.of(page, size, sortObj);
      Page<LoanResponse> loans = loanService.getLoansByStatus(LoanStatus.PENDING, pageable);
      return ResponseEntity.ok(loans);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/active")
  @PreAuthorize("hasAnyRole('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER', 'ROLE_MEMBER')")
  public ResponseEntity<Page<LoanResponse>> getActiveLoans(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort) {
    try {
      Sort sortObj = Sort.by(Sort.Direction.fromString(sort.contains("desc") ? "DESC" : "ASC"),
          sort.split(",")[0]);
      Pageable pageable = PageRequest.of(page, size, sortObj);
      Page<LoanResponse> loans = loanService.getLoansByStatus(LoanStatus.ACTIVE, pageable);
      return ResponseEntity.ok(loans);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{loanId}/history")
  @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT') or @loanSecurity.isLoanOwner(authentication, #loanId)")
  public ResponseEntity<java.util.List<com.bansaiyai.bansaiyai.dto.LoanBalanceDTO>> getLoanHistory(
      @PathVariable Long loanId) {
    try {
      java.util.List<com.bansaiyai.bansaiyai.dto.LoanBalanceDTO> history = loanService.getLoanHistory(loanId);
      return ResponseEntity.ok(history);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
