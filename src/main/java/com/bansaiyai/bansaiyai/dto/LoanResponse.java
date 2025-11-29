package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanResponse {
  private Long id;
  private String loanNumber;
  private Long memberId;
  private String memberName;
  private LoanType loanType;
  private BigDecimal principalAmount;
  private BigDecimal approvedAmount;
  private BigDecimal interestRate;
  private Integer termMonths;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalDate maturityDate;
  private LocalDate disbursementDate;
  private BigDecimal outstandingBalance;
  private BigDecimal paidPrincipal;
  private BigDecimal paidInterest;
  private BigDecimal penaltyAmount;
  private LoanStatus status;
  private String purpose;
  private String approvalNotes;
  private String rejectionReason;
  private BigDecimal collateralValue;
  private String disbursementReference;
  private LocalDate createdAt;
  private String createdBy;
  private LocalDate updatedAt;
  private String updatedBy;
  private BigDecimal monthlyPayment;
  private BigDecimal totalInterest;

  // Constructors
  public LoanResponse() {
  }

  // Builder pattern
  public static LoanResponseBuilder builder() {
    return new LoanResponseBuilder();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLoanNumber() {
    return loanNumber;
  }

  public void setLoanNumber(String loanNumber) {
    this.loanNumber = loanNumber;
  }

  public Long getMemberId() {
    return memberId;
  }

  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }

  public String getMemberName() {
    return memberName;
  }

  public void setMemberName(String memberName) {
    this.memberName = memberName;
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

  public BigDecimal getApprovedAmount() {
    return approvedAmount;
  }

  public void setApprovedAmount(BigDecimal approvedAmount) {
    this.approvedAmount = approvedAmount;
  }

  public BigDecimal getInterestRate() {
    return interestRate;
  }

  public void setInterestRate(BigDecimal interestRate) {
    this.interestRate = interestRate;
  }

  public Integer getTermMonths() {
    return termMonths;
  }

  public void setTermMonths(Integer termMonths) {
    this.termMonths = termMonths;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public LocalDate getMaturityDate() {
    return maturityDate;
  }

  public void setMaturityDate(LocalDate maturityDate) {
    this.maturityDate = maturityDate;
  }

  public LocalDate getDisbursementDate() {
    return disbursementDate;
  }

  public void setDisbursementDate(LocalDate disbursementDate) {
    this.disbursementDate = disbursementDate;
  }

  public BigDecimal getOutstandingBalance() {
    return outstandingBalance;
  }

  public void setOutstandingBalance(BigDecimal outstandingBalance) {
    this.outstandingBalance = outstandingBalance;
  }

  public BigDecimal getPaidPrincipal() {
    return paidPrincipal;
  }

  public void setPaidPrincipal(BigDecimal paidPrincipal) {
    this.paidPrincipal = paidPrincipal;
  }

  public BigDecimal getPaidInterest() {
    return paidInterest;
  }

  public void setPaidInterest(BigDecimal paidInterest) {
    this.paidInterest = paidInterest;
  }

  public BigDecimal getPenaltyAmount() {
    return penaltyAmount;
  }

  public void setPenaltyAmount(BigDecimal penaltyAmount) {
    this.penaltyAmount = penaltyAmount;
  }

  public LoanStatus getStatus() {
    return status;
  }

  public void setStatus(LoanStatus status) {
    this.status = status;
  }

  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public String getApprovalNotes() {
    return approvalNotes;
  }

  public void setApprovalNotes(String approvalNotes) {
    this.approvalNotes = approvalNotes;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public BigDecimal getCollateralValue() {
    return collateralValue;
  }

  public void setCollateralValue(BigDecimal collateralValue) {
    this.collateralValue = collateralValue;
  }

  public String getDisbursementReference() {
    return disbursementReference;
  }

  public void setDisbursementReference(String disbursementReference) {
    this.disbursementReference = disbursementReference;
  }

  public LocalDate getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDate createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDate getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDate updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public BigDecimal getMonthlyPayment() {
    return monthlyPayment;
  }

  public void setMonthlyPayment(BigDecimal monthlyPayment) {
    this.monthlyPayment = monthlyPayment;
  }

  public BigDecimal getTotalInterest() {
    return totalInterest;
  }

  public void setTotalInterest(BigDecimal totalInterest) {
    this.totalInterest = totalInterest;
  }

  // Builder class
  public static class LoanResponseBuilder {
    private LoanResponse response = new LoanResponse();

    public LoanResponseBuilder id(Long id) {
      response.setId(id);
      return this;
    }

    public LoanResponseBuilder loanNumber(String loanNumber) {
      response.setLoanNumber(loanNumber);
      return this;
    }

    public LoanResponseBuilder memberId(Long memberId) {
      response.setMemberId(memberId);
      return this;
    }

    public LoanResponseBuilder memberName(String memberName) {
      response.setMemberName(memberName);
      return this;
    }

    public LoanResponseBuilder loanType(LoanType loanType) {
      response.setLoanType(loanType);
      return this;
    }

    public LoanResponseBuilder principalAmount(BigDecimal principalAmount) {
      response.setPrincipalAmount(principalAmount);
      return this;
    }

    public LoanResponseBuilder approvedAmount(BigDecimal approvedAmount) {
      response.setApprovedAmount(approvedAmount);
      return this;
    }

    public LoanResponseBuilder interestRate(BigDecimal interestRate) {
      response.setInterestRate(interestRate);
      return this;
    }

    public LoanResponseBuilder termMonths(Integer termMonths) {
      response.setTermMonths(termMonths);
      return this;
    }

    public LoanResponseBuilder startDate(LocalDate startDate) {
      response.setStartDate(startDate);
      return this;
    }

    public LoanResponseBuilder endDate(LocalDate endDate) {
      response.setEndDate(endDate);
      return this;
    }

    public LoanResponseBuilder maturityDate(LocalDate maturityDate) {
      response.setMaturityDate(maturityDate);
      return this;
    }

    public LoanResponseBuilder disbursementDate(LocalDate disbursementDate) {
      response.setDisbursementDate(disbursementDate);
      return this;
    }

    public LoanResponseBuilder outstandingBalance(BigDecimal outstandingBalance) {
      response.setOutstandingBalance(outstandingBalance);
      return this;
    }

    public LoanResponseBuilder paidPrincipal(BigDecimal paidPrincipal) {
      response.setPaidPrincipal(paidPrincipal);
      return this;
    }

    public LoanResponseBuilder paidInterest(BigDecimal paidInterest) {
      response.setPaidInterest(paidInterest);
      return this;
    }

    public LoanResponseBuilder penaltyAmount(BigDecimal penaltyAmount) {
      response.setPenaltyAmount(penaltyAmount);
      return this;
    }

    public LoanResponseBuilder status(LoanStatus status) {
      response.setStatus(status);
      return this;
    }

    public LoanResponseBuilder purpose(String purpose) {
      response.setPurpose(purpose);
      return this;
    }

    public LoanResponseBuilder approvalNotes(String approvalNotes) {
      response.setApprovalNotes(approvalNotes);
      return this;
    }

    public LoanResponseBuilder rejectionReason(String rejectionReason) {
      response.setRejectionReason(rejectionReason);
      return this;
    }

    public LoanResponseBuilder collateralValue(BigDecimal collateralValue) {
      response.setCollateralValue(collateralValue);
      return this;
    }

    public LoanResponseBuilder disbursementReference(String disbursementReference) {
      response.setDisbursementReference(disbursementReference);
      return this;
    }

    public LoanResponseBuilder createdAt(LocalDate createdAt) {
      response.setCreatedAt(createdAt);
      return this;
    }

    public LoanResponseBuilder createdBy(String createdBy) {
      response.setCreatedBy(createdBy);
      return this;
    }

    public LoanResponseBuilder updatedAt(LocalDate updatedAt) {
      response.setUpdatedAt(updatedAt);
      return this;
    }

    public LoanResponseBuilder updatedBy(String updatedBy) {
      response.setUpdatedBy(updatedBy);
      return this;
    }

    public LoanResponseBuilder monthlyPayment(BigDecimal monthlyPayment) {
      response.setMonthlyPayment(monthlyPayment);
      return this;
    }

    public LoanResponseBuilder totalInterest(BigDecimal totalInterest) {
      response.setTotalInterest(totalInterest);
      return this;
    }

    public LoanResponse build() {
      return response;
    }
  }
}
