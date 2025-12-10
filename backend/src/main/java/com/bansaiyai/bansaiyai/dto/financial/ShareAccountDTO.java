package com.bansaiyai.bansaiyai.dto.financial;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShareAccountDTO {
    private BigDecimal totalAccumulatedShares;
    private Boolean monthlyPaymentStatus; // true if paid for current month
    private List<FinancialTransactionDTO> transactions;
}
