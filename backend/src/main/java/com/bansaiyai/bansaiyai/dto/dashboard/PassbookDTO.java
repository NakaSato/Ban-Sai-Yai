package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PassbookDTO {
    private BigDecimal balance;
    private LocalDate lastActivity;

    public PassbookDTO() {
    }

    public PassbookDTO(BigDecimal balance, LocalDate lastActivity) {
        this.balance = balance;
        this.lastActivity = lastActivity;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDate getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDate lastActivity) {
        this.lastActivity = lastActivity;
    }
}
