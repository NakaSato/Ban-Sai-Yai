package com.bansaiyai.bansaiyai.dto;

import java.math.BigDecimal;

public class LoanApprovalRequest {
  private BigDecimal approvedAmount;
  private String approvalNotes;
  private BigDecimal collateralValue;

  // Constructors
  public LoanApprovalRequest() {
  }

  public LoanApprovalRequest(BigDecimal approvedAmount, String approvalNotes, BigDecimal collateralValue) {
    this.approvedAmount = approvedAmount;
    this.approvalNotes = approvalNotes;
    this.collateralValue = collateralValue;
  }

  // Getters and Setters
  public BigDecimal getApprovedAmount() {
    return approvedAmount;
  }

  public void setApprovedAmount(BigDecimal approvedAmount) {
    this.approvedAmount = approvedAmount;
  }

  public String getApprovalNotes() {
    return approvalNotes;
  }

  public void setApprovalNotes(String approvalNotes) {
    this.approvalNotes = approvalNotes;
  }

  public BigDecimal getCollateralValue() {
    return collateralValue;
  }

  public void setCollateralValue(BigDecimal collateralValue) {
    this.collateralValue = collateralValue;
  }
}
