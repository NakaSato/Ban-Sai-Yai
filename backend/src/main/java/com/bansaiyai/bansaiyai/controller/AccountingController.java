package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.service.AccountingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AccountingController {

    private final AccountingService accountingService;

    @PostMapping("/close-month")
    @PreAuthorize("hasRole('SECRETARY') or hasRole('PRESIDENT')")
    public ResponseEntity<String> closeMonth(@RequestBody CloseMonthRequest request,
            java.security.Principal principal) {
        String result = accountingService.closeMonth(request.getMonth(), request.getYear(), principal.getName());
        return ResponseEntity.ok(result);
    }

    @Data
    public static class CloseMonthRequest {
        private int month;
        private int year;
    }

    // ==========================================
    // Chart of Accounts Endpoints
    // ==========================================

    @PostMapping("/account")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<com.bansaiyai.bansaiyai.entity.Account> createAccount(
            @RequestBody com.bansaiyai.bansaiyai.entity.Account account) {
        return ResponseEntity.ok(accountingService.createAccount(account));
    }

    @org.springframework.web.bind.annotation.PutMapping("/account/{code}")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<com.bansaiyai.bansaiyai.entity.Account> updateAccount(
            @org.springframework.web.bind.annotation.PathVariable String code,
            @RequestBody com.bansaiyai.bansaiyai.entity.Account account) {
        return ResponseEntity.ok(accountingService.updateAccount(code, account));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/account/{code}")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<Void> deleteAccount(@org.springframework.web.bind.annotation.PathVariable String code) {
        accountingService.deleteAccount(code);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
    public ResponseEntity<java.util.List<com.bansaiyai.bansaiyai.entity.Account>> getAllAccounts() {
        return ResponseEntity.ok(accountingService.getAllAccounts());
    }

    // ==========================================
    // General Transactions (Income/Expense)
    // ==========================================

    @PostMapping("/transactions/general")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT')")
    public ResponseEntity<com.bansaiyai.bansaiyai.entity.AccountingEntry> logGeneralTransaction(
            @RequestBody com.bansaiyai.bansaiyai.dto.GeneralTransactionRequest request) {
        // Convert DTO to JournalEntryRequest format expected by service if needed,
        // or add method overload in Service.
        // For now, let's adapt here or update Service. Service expects
        // JournalEntryRequest.

        com.bansaiyai.bansaiyai.dto.JournalEntryRequest journalRequest = new com.bansaiyai.bansaiyai.dto.JournalEntryRequest();
        journalRequest.setAccountCode(request.getAccountCode());
        journalRequest.setAccountName(request.getAccountName());
        journalRequest.setAmount(request.getAmount());
        journalRequest.setDescription(request.getDescription());
        journalRequest.setTransactionDate(request.getTransactionDate());
        journalRequest.setType(request.getType());

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";

        com.bansaiyai.bansaiyai.entity.AccountingEntry entry = accountingService.createEntry(journalRequest, username);
        return ResponseEntity.ok(entry);
    }
}
