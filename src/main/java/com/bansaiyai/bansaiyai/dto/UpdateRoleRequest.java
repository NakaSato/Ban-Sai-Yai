package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for role update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

  @NotBlank(message = "Role is required")
  @Pattern(regexp = "^(PRESIDENT|SECRETARY|OFFICER|MEMBER)$", message = "Role must be one of: PRESIDENT, SECRETARY, OFFICER, MEMBER")
  private String role;
}
