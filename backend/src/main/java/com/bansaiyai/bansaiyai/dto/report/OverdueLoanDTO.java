package com.bansaiyai.bansaiyai.dto.report;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OverdueLoanDTO {
    private String loanNumber;
    private String memberName;
    private BigDecimal outstandingBalance;
    private LocalDate lastPaymentDate;
    private int daysOverdue;
}
