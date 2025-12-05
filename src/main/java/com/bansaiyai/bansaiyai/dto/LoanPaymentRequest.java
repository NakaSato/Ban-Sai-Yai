package com.bansaiyai.bansaiyai.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class LoanPaymentRequest {
    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.00", message = "Principal amount cannot be negative")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest amount is required")
    @DecimalMin(value = "0.00", message = "Interest amount cannot be negative")
    private BigDecimal interestAmount;

    @DecimalMin(value = "0.00", message = "Fine amount cannot be negative")
    private BigDecimal fineAmount;

    private String notes;

    public LoanPaymentRequest() {
    }

    public LoanPaymentRequest(Long memberId, Long loanId, BigDecimal principalAmount, BigDecimal interestAmount, BigDecimal fineAmount, String notes) {
        this.memberId = memberId;
        this.loanId = loanId;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.fineAmount = fineAmount;
        this.notes = notes;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
