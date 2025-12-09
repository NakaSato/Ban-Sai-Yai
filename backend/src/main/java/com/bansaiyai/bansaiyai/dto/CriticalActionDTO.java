package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for critical actions (DELETE/OVERRIDE operations) in audit log.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriticalActionDTO {
  private Long id;
  private String username;
  private String action;
  private String entityType;
  private String entityId;
  private String details;
  private LocalDateTime timestamp;
  private String ipAddress;
  private String userAgent;
}
