package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CashBoxDTO {
    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal netCash;
    private LocalDate date;

    public CashBoxDTO() {
    }

    public CashBoxDTO(BigDecimal totalIn, BigDecimal totalOut, BigDecimal netCash, LocalDate date) {
        this.totalIn = totalIn;
        this.totalOut = totalOut;
        this.netCash = netCash;
        this.date = date;
    }

    public BigDecimal getTotalIn() {
        return totalIn;
    }

    public void setTotalIn(BigDecimal totalIn) {
        this.totalIn = totalIn;
    }

    public BigDecimal getTotalOut() {
        return totalOut;
    }

    public void setTotalOut(BigDecimal totalOut) {
        this.totalOut = totalOut;
    }

    public BigDecimal getNetCash() {
        return netCash;
    }

    public void setNetCash(BigDecimal netCash) {
        this.netCash = netCash;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
