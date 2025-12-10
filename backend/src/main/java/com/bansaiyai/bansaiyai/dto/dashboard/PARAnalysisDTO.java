package com.bansaiyai.bansaiyai.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PARAnalysisDTO {
    private BigDecimal totalPortfolio;
    private BigDecimal par1to30;
    private BigDecimal par31to60;
    private BigDecimal par61to90;
    private BigDecimal parOver90;
    private Double parRatio;
}
