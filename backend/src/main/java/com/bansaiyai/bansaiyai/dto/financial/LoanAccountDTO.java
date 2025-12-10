package com.bansaiyai.bansaiyai.dto.financial;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanAccountDTO {
    private String loanNumber;
    private String loanType;
    private BigDecimal outstandingBalance;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal totalInterestPaid;
    private Integer termMonths;
    private List<FinancialTransactionDTO> transactions;
    private String status;
}
