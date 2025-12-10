package com.bansaiyai.bansaiyai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DividendCalculationRequest {

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Invalid year")
    private Integer year;

    @NotNull(message = "Dividend rate is required")
    @Min(value = 0, message = "Rate cannot be negative")
    @Max(value = 100, message = "Rate cannot exceed 100")
    private BigDecimal dividendRate;

    @NotNull(message = "Average return rate is required")
    @Min(value = 0, message = "Rate cannot be negative")
    @Max(value = 100, message = "Rate cannot exceed 100")
    private BigDecimal averageReturnRate;
}
