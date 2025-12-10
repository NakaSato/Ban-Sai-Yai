package com.bansaiyai.bansaiyai.dto;

import com.bansaiyai.bansaiyai.entity.enums.NotificationStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for submitting a payment notification (Member -> System)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotificationRequest {

    @NotNull(message = "Loan ID is required")
    @Positive(message = "Loan ID must be positive")
    private Long loanId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @DecimalMax(value = "10000000.00", message = "Payment amount exceeds maximum limit")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDateTime paymentDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Slip image will be handled separately as multipart file upload
}
