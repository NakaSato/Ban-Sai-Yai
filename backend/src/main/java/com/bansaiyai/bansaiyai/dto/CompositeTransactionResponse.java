package com.bansaiyai.bansaiyai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompositeTransactionResponse {
    private Long shareTransactionId;
    private Long loanTransactionId;
    private String status;
    private String message;
}
