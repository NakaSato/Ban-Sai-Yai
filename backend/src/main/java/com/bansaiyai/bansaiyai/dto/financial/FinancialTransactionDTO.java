package com.bansaiyai.bansaiyai.dto.financial;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialTransactionDTO {
    private LocalDate date;
    private String period; // e.g. "10/2025"
    private BigDecimal amount;
    private String receiptNumber;
    private String type; // e.g. "SHARE", "LOAN_PRINCIPAL", "LOAN_INTEREST"
    private Integer installmentNo; // Nullable, for loans
    private BigDecimal balanceAfter;
    private String description;
}
