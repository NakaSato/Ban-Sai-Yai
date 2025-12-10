package com.bansaiyai.bansaiyai.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoanBalanceDTO {
    private Long id;
    private LocalDate balanceDate;
    private BigDecimal outstandingBalance;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal penaltyPaid;
    private BigDecimal interestAccrued;
    private Boolean isCurrent;
    private Long paymentCount;
}
