package com.bansaiyai.bansaiyai.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatementDTO {
    private String memberName;
    private String memberId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<StatementItem> items;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal endingBalance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatementItem {
        private LocalDate date;
        private String description;
        private String type; // DEPOSIT, WITHDRAWAL, LOAN_PAYMENT, etc.
        private BigDecimal debit; // Money Out / Expense / Loan Payment
        private BigDecimal credit; // Money In / Deposit / Loan Disbursement
        private BigDecimal balance; // Running Balance (mostly for Savings)
    }
}
