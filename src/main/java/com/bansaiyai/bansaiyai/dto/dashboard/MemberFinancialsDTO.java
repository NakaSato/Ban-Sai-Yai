package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;

public class MemberFinancialsDTO {
    private BigDecimal savingsBalance;
    private BigDecimal loanPrincipal;
    private String loanStatus;

    public MemberFinancialsDTO() {
    }

    public MemberFinancialsDTO(BigDecimal savingsBalance, BigDecimal loanPrincipal, String loanStatus) {
        this.savingsBalance = savingsBalance;
        this.loanPrincipal = loanPrincipal;
        this.loanStatus = loanStatus;
    }

    public BigDecimal getSavingsBalance() {
        return savingsBalance;
    }

    public void setSavingsBalance(BigDecimal savingsBalance) {
        this.savingsBalance = savingsBalance;
    }

    public BigDecimal getLoanPrincipal() {
        return loanPrincipal;
    }

    public void setLoanPrincipal(BigDecimal loanPrincipal) {
        this.loanPrincipal = loanPrincipal;
    }

    public String getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(String loanStatus) {
        this.loanStatus = loanStatus;
    }
}
