package com.bansaiyai.bansaiyai.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GuarantorRequest {
    private Long memberId; // The member acting as guarantor
    private BigDecimal guaranteedAmount;
    private String relationship;
}
