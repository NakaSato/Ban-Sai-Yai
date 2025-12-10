package com.bansaiyai.bansaiyai.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RiskProfileDTO {
    private Long memberId;
    private String memberName;
    private BigDecimal totalDebt;
    private BigDecimal totalGuaranteed;
    private BigDecimal netLiability;
    private String riskStatus; // LOW, MEDIUM, HIGH
}
