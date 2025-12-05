package com.bansaiyai.bansaiyai.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {
    private Long transactionId;
    private LocalDateTime timestamp;
    private String memberName;
    private String type;
    private BigDecimal amount;

    public TransactionDTO() {
    }

    public TransactionDTO(Long transactionId, LocalDateTime timestamp, String memberName, String type, BigDecimal amount) {
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.memberName = memberName;
        this.type = type;
        this.amount = amount;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
