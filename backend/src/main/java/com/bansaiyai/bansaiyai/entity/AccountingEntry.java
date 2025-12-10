package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing an accounting entry in the double-entry bookkeeping
 * system.
 * Each entry records either a debit or credit to an account for a specific
 * fiscal period.
 */
@Entity
@Table(name = "accounting", indexes = {
        @Index(name = "idx_accounting_fiscal_period", columnList = "fiscal_period"),
        @Index(name = "idx_accounting_debit", columnList = "debit"),
        @Index(name = "idx_accounting_credit", columnList = "credit"),
        @Index(name = "idx_accounting_account_code", columnList = "account_code")
})
public class AccountingEntry extends BaseEntity {

    @Column(name = "fiscal_period", nullable = false, length = 20)
    private String fiscalPeriod; // e.g., "2023-08" for August 2023

    @Column(name = "account_code", nullable = false, length = 10)
    private String accountCode; // e.g., "1001" for Cash, "4001" for Interest Income

    @Column(name = "account_name", length = 100)
    private String accountName;

    @Column(name = "debit", precision = 15, scale = 2)
    private BigDecimal debit;

    @Column(name = "credit", precision = 15, scale = 2)
    private BigDecimal credit;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // e.g., "LOAN", "SAVINGS", "PAYMENT"

    @Column(name = "reference_id")
    private Long referenceId; // ID of the related transaction

    @Column(name = "reference_number", length = 50)
    private String referenceNumber; // e.g., "JRN-123456789"

    // Constructors
    public AccountingEntry() {
    }

    public AccountingEntry(String fiscalPeriod, String accountCode, String accountName,
            BigDecimal debit, BigDecimal credit, LocalDate transactionDate,
            String description) {
        this.fiscalPeriod = fiscalPeriod;
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.debit = debit;
        this.credit = credit;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    // Getters and Setters
    public String getFiscalPeriod() {
        return fiscalPeriod;
    }

    public void setFiscalPeriod(String fiscalPeriod) {
        this.fiscalPeriod = fiscalPeriod;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
}
