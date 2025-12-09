package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for tracking end-of-day cash reconciliations.
 * Manages the process of comparing physical cash count with database balance.
 */
@Entity
@Table(name = "cash_reconciliations", indexes = {
    @Index(name = "idx_reconciliation_date", columnList = "date"),
    @Index(name = "idx_reconciliation_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashReconciliation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reconciliation_id")
  private Long reconciliationId;

  @Column(nullable = false)
  private LocalDate date;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "officer_id", nullable = false)
  private User officer;

  @Column(name = "physical_count", nullable = false, precision = 15, scale = 2)
  private BigDecimal physicalCount;

  @Column(name = "database_balance", nullable = false, precision = 15, scale = 2)
  private BigDecimal databaseBalance;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal variance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private ReconciliationStatus status = ReconciliationStatus.PENDING;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "secretary_id")
  private User secretary;

  @Column(name = "secretary_notes", columnDefinition = "TEXT")
  private String secretaryNotes;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  /**
   * Enum representing the status of a cash reconciliation.
   */
  public enum ReconciliationStatus {
    PENDING,
    APPROVED,
    REJECTED
  }
}
