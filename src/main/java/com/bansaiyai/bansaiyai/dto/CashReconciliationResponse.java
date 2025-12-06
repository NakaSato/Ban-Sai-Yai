package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.CashReconciliation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for cash reconciliation response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashReconciliationResponse {

  private Long reconciliationId;
  private LocalDate date;
  private String officerName;
  private String officerId;
  private BigDecimal physicalCount;
  private BigDecimal databaseBalance;
  private BigDecimal variance;
  private String status;
  private String secretaryName;
  private String secretaryId;
  private String secretaryNotes;
  private LocalDateTime approvedAt;
  private LocalDateTime createdAt;

  public static CashReconciliationResponse fromEntity(CashReconciliation reconciliation) {
    return CashReconciliationResponse.builder()
        .reconciliationId(reconciliation.getReconciliationId())
        .date(reconciliation.getDate())
        .officerName(reconciliation.getOfficer() != null
            ? reconciliation.getOfficer().getFirstName() + " " + reconciliation.getOfficer().getLastName()
            : null)
        .officerId(reconciliation.getOfficer() != null ? reconciliation.getOfficer().getId().toString() : null)
        .physicalCount(reconciliation.getPhysicalCount())
        .databaseBalance(reconciliation.getDatabaseBalance())
        .variance(reconciliation.getVariance())
        .status(reconciliation.getStatus().toString())
        .secretaryName(reconciliation.getSecretary() != null
            ? reconciliation.getSecretary().getFirstName() + " " + reconciliation.getSecretary().getLastName()
            : null)
        .secretaryId(reconciliation.getSecretary() != null ? reconciliation.getSecretary().getId().toString() : null)
        .secretaryNotes(reconciliation.getSecretaryNotes())
        .approvedAt(reconciliation.getApprovedAt())
        .createdAt(reconciliation.getCreatedAt())
        .build();
  }
}
