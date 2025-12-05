package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanObligationDTO {
    private BigDecimal outstandingPrincipal;
    private LocalDate nextDueDate;
    private BigDecimal estimatedInterest;
    private boolean isOverdue;

    public LoanObligationDTO() {
    }

    public LoanObligationDTO(BigDecimal outstandingPrincipal, LocalDate nextDueDate, BigDecimal estimatedInterest, boolean isOverdue) {
        this.outstandingPrincipal = outstandingPrincipal;
        this.nextDueDate = nextDueDate;
        this.estimatedInterest = estimatedInterest;
        this.isOverdue = isOverdue;
    }

    public BigDecimal getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public BigDecimal getEstimatedInterest() {
        return estimatedInterest;
    }

    public void setEstimatedInterest(BigDecimal estimatedInterest) {
        this.estimatedInterest = estimatedInterest;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }
}
