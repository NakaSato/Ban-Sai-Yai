package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;

public class DividendEstimateDTO {
    private BigDecimal totalShares;
    private BigDecimal projectedRate;
    private BigDecimal estimatedDividend;

    public DividendEstimateDTO() {
    }

    public DividendEstimateDTO(BigDecimal totalShares, BigDecimal projectedRate, BigDecimal estimatedDividend) {
        this.totalShares = totalShares;
        this.projectedRate = projectedRate;
        this.estimatedDividend = estimatedDividend;
    }

    public BigDecimal getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(BigDecimal totalShares) {
        this.totalShares = totalShares;
    }

    public BigDecimal getProjectedRate() {
        return projectedRate;
    }

    public void setProjectedRate(BigDecimal projectedRate) {
        this.projectedRate = projectedRate;
    }

    public BigDecimal getEstimatedDividend() {
        return estimatedDividend;
    }

    public void setEstimatedDividend(BigDecimal estimatedDividend) {
        this.estimatedDividend = estimatedDividend;
    }
}
