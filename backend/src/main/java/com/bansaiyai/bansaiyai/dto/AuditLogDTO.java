package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for audit log entries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
  private Long id;
  private String username;
  private String action;
  private String entityType;
  private String entityId;
  private String oldValue;
  private String newValue;
  private String reason;
  private LocalDateTime timestamp;
  private String ipAddress;
  private String userAgent;
  private String status; // SUCCESS, FAILED
}
