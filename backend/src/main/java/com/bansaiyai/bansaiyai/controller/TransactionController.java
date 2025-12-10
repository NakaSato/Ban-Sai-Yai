package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.TransactionResponse;
import com.bansaiyai.bansaiyai.entity.SavingTransaction;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER') or hasRole('PRESIDENT')")
    public ResponseEntity<List<SavingTransaction>> getPendingTransactions() {
        return ResponseEntity.ok(transactionService.getPendingTransactions());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('PRESIDENT')")
    public ResponseEntity<TransactionResponse> approveTransaction(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User approver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return ResponseEntity.ok(transactionService.approveTransaction(id, approver));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('PRESIDENT')")
    public ResponseEntity<TransactionResponse> rejectTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User approver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String reason = body.getOrDefault("reason", "Rejected by manager");
        return ResponseEntity.ok(transactionService.rejectTransaction(id, approver, reason));
    }
}
