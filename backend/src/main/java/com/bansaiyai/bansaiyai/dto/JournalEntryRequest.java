package com.bansaiyai.bansaiyai.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class JournalEntryRequest {
    private String type; // "INCOME" or "EXPENSE"
    private String accountCode;
    private String accountName;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
}
