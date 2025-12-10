package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

/**
 * Base entity class providing common fields for all entities.
 * Includes audit fields like creation/update timestamps and user tracking.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "uuid", unique = true, nullable = false, updatable = false)
  private java.util.UUID uuid;

  @PrePersist
  protected void generateUuid() {
    if (uuid == null) {
      uuid = java.util.UUID.randomUUID();
    }
  }

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Column(name = "created_by", length = 100)
  @CreatedBy
  private String createdBy;

  @Column(name = "updated_by", length = 100)
  @LastModifiedBy
  private String updatedBy;

  // Manual getters and setters for Lombok compatibility
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public java.util.UUID getUuid() {
    return uuid;
  }

  public void setUuid(java.util.UUID uuid) {
    this.uuid = uuid;
  }
}
