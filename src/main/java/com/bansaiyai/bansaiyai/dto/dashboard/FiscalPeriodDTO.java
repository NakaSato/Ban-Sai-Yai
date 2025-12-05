package com.bansaiyai.bansaiyai.dto.dashboard;

public class FiscalPeriodDTO {
    private String period; // "August 2023"
    private String status; // "OPEN" or "CLOSED"

    public FiscalPeriodDTO() {
    }

    public FiscalPeriodDTO(String period, String status) {
        this.period = period;
        this.status = status;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
