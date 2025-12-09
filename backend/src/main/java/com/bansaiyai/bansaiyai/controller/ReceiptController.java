package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.service.ReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for receipt generation and retrieval
 */
@RestController
@RequestMapping("/api/receipts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /**
     * Generate and download PDF receipt for a transaction
     * @param transactionId The transaction ID
     * @return PDF file as byte array
     */
    @GetMapping("/{transactionId}/pdf")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<byte[]> getReceiptPdf(@PathVariable Long transactionId) {
        try {
            byte[] pdfBytes = receiptService.generateReceiptPdf(transactionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "receipt-" + transactionId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
