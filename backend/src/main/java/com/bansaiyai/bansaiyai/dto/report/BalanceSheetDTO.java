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
public class BalanceSheetDTO {
    private List<ReportItemDTO> assets;
    private List<ReportItemDTO> liabilities;
    private List<ReportItemDTO> equity;

    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;

    private String asOfDate;
}
