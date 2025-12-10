package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.financial.MemberFinancialDTO;
import com.bansaiyai.bansaiyai.service.MemberFinancialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Member Financials", description = "Endpoints for members to view their financial data")
public class MemberFinancialController {

    private final MemberFinancialService memberFinancialService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER') or hasRole('OFFICER')")
    @Operation(summary = "Get current member's financial data (Shares & Loans)")
    public ResponseEntity<MemberFinancialDTO> getMyFinancials(Principal principal) {
        log.info("Fetching financial data for user: {}", principal.getName());
        MemberFinancialDTO data = memberFinancialService.getMemberFinancialData(principal.getName());
        return ResponseEntity.ok(data);
    }
}
