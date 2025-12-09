package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for loan application requests.
 * Contains validation rules based on business requirements.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationRequest {

  @NotNull(message = "Member ID is required")
  private Long memberId;

  @NotNull(message = "Loan type is required")
  private LoanType loanType;

  @NotNull(message = "Principal amount is required")
  @DecimalMin(value = "1000.00", message = "Minimum loan amount is 1,000 THB")
  @DecimalMax(value = "500000.00", message = "Maximum loan amount is 500,000 THB")
  private BigDecimal principalAmount;

  @NotNull(message = "Loan term is required")
  @Min(value = 1, message = "Minimum loan term is 1 month")
  @Max(value = 120, message = "Maximum loan term is 120 months")
  private Integer termMonths;

  @NotBlank(message = "Loan purpose is required")
  @Size(min = 10, max = 500, message = "Purpose must be between 10 and 500 characters")
  private String purpose;
}
