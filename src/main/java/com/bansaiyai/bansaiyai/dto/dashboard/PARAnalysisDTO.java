package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;

public class PARAnalysisDTO {
    private int standardCount;
    private int watchCount;
    private int substandardCount;
    private int lossCount;
    private BigDecimal totalAtRisk;

    public PARAnalysisDTO() {
    }

    public PARAnalysisDTO(int standardCount, int watchCount, int substandardCount, int lossCount, BigDecimal totalAtRisk) {
        this.standardCount = standardCount;
        this.watchCount = watchCount;
        this.substandardCount = substandardCount;
        this.lossCount = lossCount;
        this.totalAtRisk = totalAtRisk;
    }

    public int getStandardCount() {
        return standardCount;
    }

    public void setStandardCount(int standardCount) {
        this.standardCount = standardCount;
    }

    public int getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(int watchCount) {
        this.watchCount = watchCount;
    }

    public int getSubstandardCount() {
        return substandardCount;
    }

    public void setSubstandardCount(int substandardCount) {
        this.substandardCount = substandardCount;
    }

    public int getLossCount() {
        return lossCount;
    }

    public void setLossCount(int lossCount) {
        this.lossCount = lossCount;
    }

    public BigDecimal getTotalAtRisk() {
        return totalAtRisk;
    }

    public void setTotalAtRisk(BigDecimal totalAtRisk) {
        this.totalAtRisk = totalAtRisk;
    }
}
