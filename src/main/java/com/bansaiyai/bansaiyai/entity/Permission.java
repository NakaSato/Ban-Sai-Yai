package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a permission in the RBAC system.
 * Permissions are granular capabilities that can be assigned to roles.
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_module", columnList = "module")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "perm_id")
  private Integer permId;

  @Column(name = "perm_slug", unique = true, nullable = false, length = 50)
  private String permSlug;

  @Column(nullable = false, length = 50)
  private String module;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;
}
