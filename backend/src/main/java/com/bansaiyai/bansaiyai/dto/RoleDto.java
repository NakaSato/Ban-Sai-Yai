package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for role information with permissions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
  private Long id;
  private String roleName;
  private String description;
  private List<PermissionDto> permissions;

  /**
   * DTO for permission information within roles.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PermissionDto {
    private Long id;
    private String permissionSlug;
    private String description;
  }
}
