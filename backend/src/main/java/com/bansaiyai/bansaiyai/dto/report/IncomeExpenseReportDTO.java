package com.bansaiyai.bansaiyai.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeExpenseReportDTO {
    private List<ReportItemDTO> incomeItems;
    private List<ReportItemDTO> expenseItems;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;
    private String period;
}
