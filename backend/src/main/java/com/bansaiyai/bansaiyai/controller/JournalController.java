package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.JournalEntryRequest;
import com.bansaiyai.bansaiyai.entity.AccountingEntry;
import com.bansaiyai.bansaiyai.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class JournalController {

    private final AccountingService accountingService;

    @PostMapping("/entries")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
    public ResponseEntity<AccountingEntry> createEntry(@RequestBody JournalEntryRequest request,
            java.security.Principal principal) {
        return ResponseEntity.ok(accountingService.createEntry(request, principal.getName()));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<Map<String, BigDecimal>> getDailySummary(
            @RequestParam(required = false) String date) {
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(accountingService.getDailySummary(targetDate));
    }
}
