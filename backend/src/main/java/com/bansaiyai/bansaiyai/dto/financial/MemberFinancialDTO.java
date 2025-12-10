package com.bansaiyai.bansaiyai.dto.financial;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberFinancialDTO {
    private ShareAccountDTO shares;
    private List<LoanAccountDTO> loans;
}
