package com.bansaiyai.bansaiyai.dto.report;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MonthlyReportDTO {
    private String month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netIncome;
    private int newLoansCount;
    private int closedLoansCount;
}
