package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.DividendCalculationRequest;
import com.bansaiyai.bansaiyai.entity.DividendDistribution;
import com.bansaiyai.bansaiyai.entity.DividendRecipient;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.DividendService;
import com.bansaiyai.bansaiyai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dividends")
@RequiredArgsConstructor
public class DividendController {

    private final DividendService dividendService;
    private final UserService userService;
    private final com.bansaiyai.bansaiyai.service.ExportService exportService;

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
    public ResponseEntity<DividendDistribution> calculateDividends(
            @Valid @RequestBody DividendCalculationRequest request,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        DividendDistribution dist = dividendService.calculateDividends(
                request.getYear(),
                request.getDividendRate(),
                request.getAverageReturnRate(),
                user);
        return ResponseEntity.ok(dist);
    }

    @PostMapping("/{year}/distribute")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<DividendDistribution> distributeDividends(
            @PathVariable Integer year,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        DividendDistribution dist = dividendService.distributeDividends(year, user);
        return ResponseEntity.ok(dist);
    }

    @GetMapping("/{year}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
    public ResponseEntity<DividendDistribution> getDistribution(@PathVariable Integer year) {
        return ResponseEntity.ok(dividendService.getDistribution(year));
    }

    @GetMapping("/{year}/recipients") // New endpoint for checking details
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
    public ResponseEntity<List<DividendRecipient>> getRecipients(@PathVariable Integer year) {
        DividendDistribution dist = dividendService.getDistribution(year);
        return ResponseEntity.ok(dividendService.getRecipients(dist.getId()));
    }

    @GetMapping("/{year}/recipients/export")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
    public ResponseEntity<byte[]> exportRecipients(@PathVariable Integer year) {
        DividendDistribution dist = dividendService.getDistribution(year);
        List<DividendRecipient> recipients = dividendService.getRecipients(dist.getId());
        String csv = exportService.generateDividendRecipientsCsv(recipients);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=dividend-recipients-" + year + ".csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }
}
