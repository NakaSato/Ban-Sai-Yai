package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for loan document response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDocumentResponse {

    private String documentUuid; // UUID for security
    private String documentName;
    private String documentType;
    private String documentUrl;
    private Long fileSize;
    private String uploadedBy;
    private String uploadedAt;
    private String description;
}
