package com.bansaiyai.bansaiyai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DTO for discrepancy approval/rejection request
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscrepancyApprovalRequest {

  @NotNull(message = "Action is required")
  private String action; // "APPROVE" or "REJECT"

  @Size(max = 500, message = "Notes must not exceed 500 characters")
  private String notes;
}
