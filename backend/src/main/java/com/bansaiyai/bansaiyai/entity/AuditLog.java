package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for audit logging of security-relevant actions.
 * Stores comprehensive information about user actions for compliance and
 * investigation.
 */
@Entity
@Table(name = "system_audit_log", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "log_id")
  private Long logId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false, length = 100)
  private String action;

  @Column(name = "entity_type", length = 50)
  private String entityType;

  @Column(name = "entity_id")
  private Long entityId;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "old_values", columnDefinition = "JSON")
  private String oldValues;

  @Column(name = "new_values", columnDefinition = "JSON")
  private String newValues;

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime timestamp;

  // Explicit getters to fix Lombok compilation issues
  public Long getLogId() {
    return logId;
  }

  public User getUser() {
    return user;
  }

  public String getAction() {
    return action;
  }

  public String getEntityType() {
    return entityType;
  }

  public Long getEntityId() {
    return entityId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getOldValues() {
    return oldValues;
  }

  public String getNewValues() {
    return newValues;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}
