package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.dashboard.*;
import com.bansaiyai.bansaiyai.dto.DepositRequest;
import com.bansaiyai.bansaiyai.dto.LoanPaymentRequest;
import com.bansaiyai.bansaiyai.dto.TransactionResponse;
import com.bansaiyai.bansaiyai.service.DashboardService;
import com.bansaiyai.bansaiyai.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private final DashboardService dashboardService;
    private final TransactionService transactionService;

    public DashboardController(DashboardService dashboardService, TransactionService transactionService) {
        this.dashboardService = dashboardService;
        this.transactionService = transactionService;
    }

    // Global endpoints
    @GetMapping("/fiscal-period")
    public ResponseEntity<FiscalPeriodDTO> getCurrentFiscalPeriod() {
        return ResponseEntity.ok(dashboardService.getCurrentFiscalPeriod());
    }

    @GetMapping("/members/search")
    public ResponseEntity<List<MemberSearchResultDTO>> searchMembers(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.searchMembers(q, limit));
    }

    // Officer endpoints
    @GetMapping("/officer/cash-box")
    @PreAuthorize("hasAnyRole('OFFICER', 'PRESIDENT')")
    public ResponseEntity<CashBoxDTO> getCashBoxTally() {
        return ResponseEntity.ok(dashboardService.calculateCashBoxTally());
    }

    @GetMapping("/officer/recent-transactions")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentTransactions(limit));
    }

    @GetMapping("/members/{id}/financials")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
    public ResponseEntity<MemberFinancialsDTO> getMemberFinancials(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.getMemberFinancials(id));
    }

    // Secretary endpoints
    @GetMapping("/secretary/trial-balance")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<TrialBalanceDTO> getTrialBalance() {
        return ResponseEntity.ok(dashboardService.calculateTrialBalance());
    }

    @GetMapping("/secretary/unclassified-count")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<UnclassifiedCountDTO> getUnclassifiedCount() {
        int count = dashboardService.countUnclassifiedTransactions();
        return ResponseEntity.ok(new UnclassifiedCountDTO(count));
    }

    @GetMapping("/secretary/financial-previews")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<FinancialPreviewsDTO> getFinancialPreviews() {
        return ResponseEntity.ok(dashboardService.generateFinancialPreviews());
    }

    // President endpoints
    @GetMapping("/president/par-analysis")
    @PreAuthorize("hasRole('PRESIDENT')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get PAR Analysis", description = "Retrieves Portfolio At Risk analysis including ratio and buckets.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<PARAnalysisDTO> getPARAnalysis() {
        return ResponseEntity.ok(dashboardService.calculatePARAnalysis());
    }

    @GetMapping("/president/par-details")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<Map<String, Object>> getPARDetails(@RequestParam String category) {
        // TODO: Implement details view if needed, for now Analysis covers the main
        // requirement
        return ResponseEntity.ok(Map.of("category", category, "members", List.of()));
    }

    @GetMapping("/president/liquidity")
    @PreAuthorize("hasRole('PRESIDENT')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Liquidity Ratio", description = "Calculates current liquidity ratio based on cash, bank, and savings.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<LiquidityDTO> getLiquidityRatio() {
        return ResponseEntity.ok(dashboardService.calculateLiquidityRatio());
    }

    @GetMapping("/president/membership-trends")
    @PreAuthorize("hasRole('PRESIDENT')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Membership Trends", description = "Retrieves membership growth trends over the specified number of months.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<MembershipTrendsDTO> getMembershipTrends(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(dashboardService.getMembershipTrends(months));
    }

    // Member endpoints
    @GetMapping("/member/passbook")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<PassbookDTO> getPassbook(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Implement in future task
        return ResponseEntity.ok(new PassbookDTO());
    }

    @GetMapping("/member/loan-obligation")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<LoanObligationDTO> getLoanObligation(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Implement in future task
        return ResponseEntity.ok(new LoanObligationDTO());
    }

    @GetMapping("/member/dividend-estimate")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<DividendEstimateDTO> getDividendEstimate(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Implement in future task
        return ResponseEntity.ok(new DividendEstimateDTO());
    }

    // Transaction endpoints for Officer Dashboard
    @PostMapping("/transactions/deposit")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
    public ResponseEntity<TransactionResponse> processDeposit(@Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.processDeposit(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions/loan-payment")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
    public ResponseEntity<TransactionResponse> processLoanPayment(@Valid @RequestBody LoanPaymentRequest request) {
        TransactionResponse response = transactionService.processLoanPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/loans/{loanId}/minimum-interest")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
    public ResponseEntity<Map<String, BigDecimal>> getMinimumInterest(@PathVariable Long loanId) {
        BigDecimal minimumInterest = transactionService.calculateMinimumInterest(loanId);
        return ResponseEntity.ok(Map.of("minimumInterest", minimumInterest));
    }
}
