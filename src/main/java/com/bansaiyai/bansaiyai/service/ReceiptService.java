package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.SavingTransaction;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import com.bansaiyai.bansaiyai.repository.SavingTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for generating transaction receipts
 */
@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    
    private final SavingTransactionRepository savingTransactionRepository;
    private final PaymentRepository paymentRepository;

    public ReceiptService(SavingTransactionRepository savingTransactionRepository,
                         PaymentRepository paymentRepository) {
        this.savingTransactionRepository = savingTransactionRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Generate PDF receipt for a transaction
     * This is a simplified implementation that generates a basic text-based PDF
     * In a production system, you would use a library like iText or Apache PDFBox
     */
    public byte[] generateReceiptPdf(Long transactionId) {
        try {
            // Try to find as savings transaction first
            SavingTransaction savingTxn = savingTransactionRepository.findById(transactionId).orElse(null);
            if (savingTxn != null) {
                return generateSavingTransactionReceipt(savingTxn);
            }
            
            // Try to find as payment
            Payment payment = paymentRepository.findById(transactionId).orElse(null);
            if (payment != null) {
                return generatePaymentReceipt(payment);
            }
            
            // Transaction not found
            return generateErrorReceipt("Transaction not found");
            
        } catch (Exception e) {
            log.error("Error generating receipt PDF for transaction {}: {}", transactionId, e.getMessage());
            return generateErrorReceipt("Error generating receipt: " + e.getMessage());
        }
    }

    /**
     * Generate receipt for savings transaction
     */
    private byte[] generateSavingTransactionReceipt(SavingTransaction txn) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("TRANSACTION RECEIPT\n");
        receipt.append("===================\n\n");
        receipt.append("Transaction Number: ").append(txn.getTransactionNumber()).append("\n");
        receipt.append("Date: ").append(txn.getTransactionDate().format(DateTimeFormatter.ISO_DATE)).append("\n");
        receipt.append("Type: ").append(txn.getTransactionType().name()).append("\n");
        receipt.append("Amount: ").append(txn.getAmount()).append("\n");
        
        if (txn.getSavingAccount() != null && txn.getSavingAccount().getMember() != null) {
            receipt.append("Member: ").append(txn.getSavingAccount().getMember().getName()).append("\n");
        }
        
        receipt.append("Balance Before: ").append(txn.getBalanceBefore()).append("\n");
        receipt.append("Balance After: ").append(txn.getBalanceAfter()).append("\n");
        
        if (txn.getDescription() != null) {
            receipt.append("Description: ").append(txn.getDescription()).append("\n");
        }
        
        receipt.append("\n");
        receipt.append("Thank you for your transaction.\n");
        
        return receipt.toString().getBytes();
    }

    /**
     * Generate receipt for payment
     */
    private byte[] generatePaymentReceipt(Payment payment) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("PAYMENT RECEIPT\n");
        receipt.append("===============\n\n");
        receipt.append("Payment Number: ").append(payment.getPaymentNumber()).append("\n");
        receipt.append("Date: ").append(payment.getPaymentDate() != null ? 
            payment.getPaymentDate().format(DateTimeFormatter.ISO_DATE) : "N/A").append("\n");
        receipt.append("Type: ").append(payment.getPaymentType().name()).append("\n");
        receipt.append("Amount: ").append(payment.getAmount()).append("\n");
        
        if (payment.getMember() != null) {
            receipt.append("Member: ").append(payment.getMember().getName()).append("\n");
        }
        
        if (payment.getPrincipalAmount() != null) {
            receipt.append("Principal: ").append(payment.getPrincipalAmount()).append("\n");
        }
        if (payment.getInterestAmount() != null) {
            receipt.append("Interest: ").append(payment.getInterestAmount()).append("\n");
        }
        if (payment.getFeeAmount() != null) {
            receipt.append("Fee: ").append(payment.getFeeAmount()).append("\n");
        }
        if (payment.getPenaltyAmount() != null) {
            receipt.append("Penalty: ").append(payment.getPenaltyAmount()).append("\n");
        }
        
        receipt.append("Status: ").append(payment.getPaymentStatus().name()).append("\n");
        
        if (payment.getDescription() != null) {
            receipt.append("Description: ").append(payment.getDescription()).append("\n");
        }
        
        receipt.append("\n");
        receipt.append("Thank you for your payment.\n");
        
        return receipt.toString().getBytes();
    }

    /**
     * Generate error receipt
     */
    private byte[] generateErrorReceipt(String errorMessage) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("RECEIPT ERROR\n");
        receipt.append("=============\n\n");
        receipt.append(errorMessage).append("\n");
        return receipt.toString().getBytes();
    }
}
