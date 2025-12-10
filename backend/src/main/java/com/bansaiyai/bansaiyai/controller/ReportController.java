package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.report.MonthlyReportDTO;
import com.bansaiyai.bansaiyai.dto.report.OverdueLoanDTO;
import com.bansaiyai.bansaiyai.service.ReportService;
import com.bansaiyai.bansaiyai.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ExportService exportService;

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<MonthlyReportDTO> getMonthlyReport(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(reportService.generateMonthlyReport(month, year));
    }

    @GetMapping("/monthly/export")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<byte[]> exportMonthlyReport(
            @RequestParam int month,
            @RequestParam int year) {
        MonthlyReportDTO report = reportService.generateMonthlyReport(month, year);
        String csv = exportService.generateMonthlyReportCsv(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=monthly-report-" + year + "-" + month + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @GetMapping("/overdue-loans")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<List<OverdueLoanDTO>> getOverdueLoans() {
        return ResponseEntity.ok(reportService.getOverdueLoans());
    }

    @GetMapping("/overdue-loans/export")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<byte[]> exportOverdueLoans() {
        List<OverdueLoanDTO> loans = reportService.getOverdueLoans();
        String csv = exportService.generateOverdueLoansCsv(loans);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=overdue-loans.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @GetMapping("/income-expense")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<com.bansaiyai.bansaiyai.dto.report.IncomeExpenseReportDTO> getIncomeExpenseReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateIncomeExpenseReport(startDate, endDate));
    }

    @GetMapping("/income-expense/export")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<byte[]> exportIncomeExpenseReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        com.bansaiyai.bansaiyai.dto.report.IncomeExpenseReportDTO report = reportService
                .generateIncomeExpenseReport(startDate, endDate);
        String csv = exportService.generateIncomeExpenseReportCsv(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income-expense.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @GetMapping("/balance-sheet")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<com.bansaiyai.bansaiyai.dto.report.BalanceSheetDTO> getBalanceSheet(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate asOfDate) {
        return ResponseEntity.ok(reportService.generateBalanceSheet(asOfDate));
    }

    @GetMapping("/balance-sheet/export")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<byte[]> exportBalanceSheet(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate asOfDate) {
        com.bansaiyai.bansaiyai.dto.report.BalanceSheetDTO report = reportService.generateBalanceSheet(asOfDate);
        String csv = exportService.generateBalanceSheetCsv(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance-sheet.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @GetMapping("/member/{memberId}/statement")
    @PreAuthorize("hasAnyRole('OFFICER', 'MANAGER', 'PRESIDENT') or @userContext.isCurrentMember(#memberId)")
    public ResponseEntity<com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO> getMemberStatement(
            @PathVariable Long memberId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {

        if (startDate == null)
            startDate = java.time.LocalDate.now().minusMonths(1);
        if (endDate == null)
            endDate = java.time.LocalDate.now();

        return ResponseEntity.ok(reportService.generateMemberStatement(memberId, startDate, endDate));
    }

    @GetMapping("/member/{memberId}/statement/export")
    @PreAuthorize("hasAnyRole('OFFICER', 'MANAGER', 'PRESIDENT') or @userContext.isCurrentMember(#memberId)")
    public ResponseEntity<byte[]> exportMemberStatement(
            @PathVariable Long memberId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {

        if (startDate == null)
            startDate = java.time.LocalDate.now().minusMonths(1);
        if (endDate == null)
            endDate = java.time.LocalDate.now();

        com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO statement = reportService
                .generateMemberStatement(memberId, startDate, endDate);
        String csv = exportService.generateMemberStatementCsv(statement);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement-" + memberId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }
}
