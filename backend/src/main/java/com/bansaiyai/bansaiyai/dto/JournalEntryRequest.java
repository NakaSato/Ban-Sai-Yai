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

    public String getType() {
        return type;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
}
