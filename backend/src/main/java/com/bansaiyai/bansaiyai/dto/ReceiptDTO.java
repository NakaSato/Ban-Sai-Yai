package com.bansaiyai.bansaiyai.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReceiptDTO {
    private String receiptNumber;
    private LocalDateTime transactionDate;
    private String memberName;
    private String memberId;
    private String transactionType;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String cashierName;
    private List<ReceiptLineItem> lineItems;

    @Data
    @Builder
    public static class ReceiptLineItem {
        private String description;
        private BigDecimal amount;
    }
}
