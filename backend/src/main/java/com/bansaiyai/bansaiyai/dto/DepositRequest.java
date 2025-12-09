package com.bansaiyai.bansaiyai.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for deposit requests
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositRequest {

    @NotNull(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;

    private String transactionType;

    private Long memberId;

    private String notes;

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
