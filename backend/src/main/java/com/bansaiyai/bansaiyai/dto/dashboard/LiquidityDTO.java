package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;

public class LiquidityDTO {
    private BigDecimal liquidityRatio;
    private String status; // "CRISIS", "CAUTION", "HEALTHY", "INEFFICIENT"
    private BigDecimal cashAndBank;
    private BigDecimal totalSavings;

    public LiquidityDTO() {
    }

    public LiquidityDTO(BigDecimal liquidityRatio, String status, BigDecimal cashAndBank, BigDecimal totalSavings) {
        this.liquidityRatio = liquidityRatio;
        this.status = status;
        this.cashAndBank = cashAndBank;
        this.totalSavings = totalSavings;
    }

    public BigDecimal getLiquidityRatio() {
        return liquidityRatio;
    }

    public void setLiquidityRatio(BigDecimal liquidityRatio) {
        this.liquidityRatio = liquidityRatio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getCashAndBank() {
        return cashAndBank;
    }

    public void setCashAndBank(BigDecimal cashAndBank) {
        this.cashAndBank = cashAndBank;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }
}
