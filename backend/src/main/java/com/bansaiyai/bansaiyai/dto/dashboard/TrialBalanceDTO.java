package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;

public class TrialBalanceDTO {
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal variance;
    private boolean isBalanced;
    private String fiscalPeriod;

    public TrialBalanceDTO() {
    }

    public TrialBalanceDTO(BigDecimal totalDebits, BigDecimal totalCredits, BigDecimal variance, boolean isBalanced, String fiscalPeriod) {
        this.totalDebits = totalDebits;
        this.totalCredits = totalCredits;
        this.variance = variance;
        this.isBalanced = isBalanced;
        this.fiscalPeriod = fiscalPeriod;
    }

    public BigDecimal getTotalDebits() {
        return totalDebits;
    }

    public void setTotalDebits(BigDecimal totalDebits) {
        this.totalDebits = totalDebits;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(BigDecimal totalCredits) {
        this.totalCredits = totalCredits;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }

    public boolean isBalanced() {
        return isBalanced;
    }

    public void setBalanced(boolean balanced) {
        isBalanced = balanced;
    }

    public String getFiscalPeriod() {
        return fiscalPeriod;
    }

    public void setFiscalPeriod(String fiscalPeriod) {
        this.fiscalPeriod = fiscalPeriod;
    }
}
