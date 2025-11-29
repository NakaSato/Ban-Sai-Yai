package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.LoanType;

import java.math.BigDecimal;

public class LoanApplicationRequest {
  private Long memberId;
  private LoanType loanType;
  private BigDecimal principalAmount;
  private Integer termMonths;
  private String purpose;

  // Constructors
  public LoanApplicationRequest() {
  }

  public LoanApplicationRequest(Long memberId, LoanType loanType, BigDecimal principalAmount,
      Integer termMonths, String purpose) {
    this.memberId = memberId;
    this.loanType = loanType;
    this.principalAmount = principalAmount;
    this.termMonths = termMonths;
    this.purpose = purpose;
  }

  // Getters and Setters
  public Long getMemberId() {
    return memberId;
  }

  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }

  public LoanType getLoanType() {
    return loanType;
  }

  public void setLoanType(LoanType loanType) {
    this.loanType = loanType;
  }

  public BigDecimal getPrincipalAmount() {
    return principalAmount;
  }

  public void setPrincipalAmount(BigDecimal principalAmount) {
    this.principalAmount = principalAmount;
  }

  public Integer getTermMonths() {
    return termMonths;
  }

  public void setTermMonths(Integer termMonths) {
    this.termMonths = termMonths;
  }

  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }
}
