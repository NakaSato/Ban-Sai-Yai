package com.bansaiyai.bansaiyai.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CompositePaymentRequest {
    private Long memberId;
    private BigDecimal shareAmount;
    private BigDecimal loanPaymentAmount;
    private Long loanId; // Optional, required if loanPaymentAmount > 0
    private String notes;
}
