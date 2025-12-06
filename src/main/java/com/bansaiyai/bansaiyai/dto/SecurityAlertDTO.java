package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for security alerts (off-hours activity, suspicious patterns).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAlertDTO {
  private Long id;
  private String alertType; // OFF_HOURS, MULTIPLE_FAILED_LOGINS, UNUSUAL_IP
  private String username;
  private String action;
  private LocalDateTime timestamp;
  private String ipAddress;
  private String severity; // LOW, MEDIUM, HIGH, CRITICAL
  private String description;
  private boolean resolved;
  private LocalDateTime resolvedAt;
  private String resolvedBy;
}
