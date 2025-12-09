package com.bansaiyai.bansaiyai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a cash reconciliation request
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashReconciliationRequest {

  @NotNull(message = "Physical count is required")
  @DecimalMin(value = "0.00", message = "Physical count must be non-negative")
  @Digits(integer = 13, fraction = 2, message = "Physical count must have at most 2 decimal places")
  private BigDecimal physicalCount;

  private String notes;
}
