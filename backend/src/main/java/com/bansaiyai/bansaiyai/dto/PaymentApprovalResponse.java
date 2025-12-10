package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment approval response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApprovalResponse {

    private String message;
    private Long notificationId;
    private Long paymentId;
    private String receiptNumber;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal totalPaid;
    private BigDecimal newOutstandingBalance;
}
