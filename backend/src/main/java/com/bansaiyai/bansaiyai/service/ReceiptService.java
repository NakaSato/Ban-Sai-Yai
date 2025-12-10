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
        receipt.append("Date: ").append(
                payment.getPaymentDate() != null ? payment.getPaymentDate().format(DateTimeFormatter.ISO_DATE) : "N/A")
                .append("\n");
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
    private byte[] generateErrorReceipt(String message) {
        String errorContent = "ERROR GENERATING RECEIPT\n\n" + message;
        return errorContent.getBytes();
    }

    /**
     * Generate HTML receipt (for Thermal/A5 printing with Bootstrap)
     */
    public String generateReceiptHtml(Long transactionId) {
        StringBuilder html = new StringBuilder();

        try {
            // Common Header
            html.append("<html><head>");
            html.append(
                    "<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
            html.append(
                    "<style> body { font-family: 'Sarabun', sans-serif; } .receipt-container { max-width: 300px; margin: 0 auto; padding: 10px; } </style>");
            html.append("</head><body>");
            html.append("<div class='receipt-container'>");
            html.append(
                    "<div class='text-center mb-3'><h5>BAN SAI YAI GROUP</h5><p class='small'>Official Receipt</p></div>");

            // Try to find as savings transaction
            SavingTransaction savingTxn = savingTransactionRepository.findById(transactionId).orElse(null);
            if (savingTxn != null) {
                html.append("<div class='mb-2'>");
                html.append("<strong>Receipt No:</strong> ").append(savingTxn.getTransactionNumber()).append("<br>");
                html.append("<strong>Date:</strong> ").append(savingTxn.getTransactionDate()).append("<br>");
                html.append("<strong>Member:</strong> ").append(savingTxn.getSavingAccount().getMember().getName())
                        .append("<br>");
                html.append("</div>");

                html.append("<table class='table table-sm table-bordered'>");
                html.append("<tr><td>Type</td><td class='text-end'>").append(savingTxn.getTransactionType())
                        .append("</td></tr>");
                html.append("<tr><td>Amount</td><td class='text-end fw-bold'>")
                        .append(String.format("%,.2f", savingTxn.getAmount())).append("</td></tr>");
                html.append("</table>");

                html.append("<div class='mt-3 text-center small'>Balance: ")
                        .append(String.format("%,.2f", savingTxn.getBalanceAfter())).append("</div>");

            } else {
                // Try to find as payment
                Payment payment = paymentRepository.findById(transactionId).orElse(null);
                if (payment != null) {
                    html.append("<div class='mb-2'>");
                    html.append("<strong>Receipt No:</strong> ").append(payment.getPaymentNumber()).append("<br>");
                    html.append("<strong>Date:</strong> ").append(payment.getPaymentDate()).append("<br>");
                    html.append("<strong>Member:</strong> ").append(payment.getMember().getName()).append("<br>");
                    html.append("</div>");

                    html.append("<table class='table table-sm table-bordered'>");
                    html.append("<tr><td>Principal</td><td class='text-end'>")
                            .append(String.format("%,.2f", payment.getPrincipalAmount())).append("</td></tr>");
                    html.append("<tr><td>Interest</td><td class='text-end'>")
                            .append(String.format("%,.2f", payment.getInterestAmount())).append("</td></tr>");
                    if (payment.getPenaltyAmount() != null && payment.getPenaltyAmount().signum() > 0) {
                        html.append("<tr><td>Penalty</td><td class='text-end'>")
                                .append(String.format("%,.2f", payment.getPenaltyAmount())).append("</td></tr>");
                    }
                    html.append("<tr><td><strong>Total</strong></td><td class='text-end fw-bold'>")
                            .append(String.format("%,.2f", payment.getAmount())).append("</td></tr>");
                    html.append("</table>");
                } else {
                    return "<html><body><div class='alert alert-danger'>Transaction Not Found</div></body></html>";
                }
            }

            html.append("<div class='mt-4 text-center small text-muted'>System Generated Receipt</div>");
            html.append("</div></body></html>");

            return html.toString();

        } catch (Exception e) {
            log.error("Error generating HTML receipt", e);
            return "<html><body>Error generating receipt</body></html>";
        }

    }

    /**
     * Get Receipt Data DTO for frontend printing
     */
    public com.bansaiyai.bansaiyai.dto.ReceiptDTO getReceiptData(Long transactionId) {
        try {
            // Try as Savings Transaction
            SavingTransaction savingTxn = savingTransactionRepository.findById(transactionId).orElse(null);
            if (savingTxn != null) {
                return com.bansaiyai.bansaiyai.dto.ReceiptDTO.builder()
                        .receiptNumber(savingTxn.getTransactionNumber())
                        .transactionDate(savingTxn.getTransactionDate().atStartOfDay()) // or createdAt if available
                        .memberName(savingTxn.getSavingAccount().getMember().getName())
                        .memberId(savingTxn.getSavingAccount().getMember().getMemberId())
                        .transactionType(savingTxn.getTransactionType().name())
                        .totalAmount(savingTxn.getAmount())
                        .paymentMethod("CASH") // Default or derive
                        .cashierName(savingTxn.getCreatorUser() != null ? savingTxn.getCreatorUser().getUsername()
                                : "System")
                        .lineItems(java.util.List.of(
                                com.bansaiyai.bansaiyai.dto.ReceiptDTO.ReceiptLineItem.builder()
                                        .description(savingTxn.getDescription())
                                        .amount(savingTxn.getAmount())
                                        .build()))
                        .build();
            }

            // Try as Payment
            Payment payment = paymentRepository.findById(transactionId).orElse(null);
            if (payment != null) {
                java.util.List<com.bansaiyai.bansaiyai.dto.ReceiptDTO.ReceiptLineItem> lines = new java.util.ArrayList<>();
                if (payment.getPrincipalAmount() != null && payment.getPrincipalAmount().signum() > 0) {
                    lines.add(com.bansaiyai.bansaiyai.dto.ReceiptDTO.ReceiptLineItem.builder()
                            .description("Loan Principal")
                            .amount(payment.getPrincipalAmount())
                            .build());
                }
                if (payment.getInterestAmount() != null && payment.getInterestAmount().signum() > 0) {
                    lines.add(com.bansaiyai.bansaiyai.dto.ReceiptDTO.ReceiptLineItem.builder()
                            .description("Loan Interest")
                            .amount(payment.getInterestAmount())
                            .build());
                }
                if (payment.getPenaltyAmount() != null && payment.getPenaltyAmount().signum() > 0) {
                    lines.add(com.bansaiyai.bansaiyai.dto.ReceiptDTO.ReceiptLineItem.builder()
                            .description("Late Penalty")
                            .amount(payment.getPenaltyAmount())
                            .build());
                }

                return com.bansaiyai.bansaiyai.dto.ReceiptDTO.builder()
                        .receiptNumber(payment.getPaymentNumber())
                        .transactionDate(payment.getPaymentDate().atStartOfDay())
                        .memberName(payment.getMember().getName())
                        .memberId(payment.getMember().getMemberId())
                        .transactionType(payment.getPaymentType().name())
                        .totalAmount(payment.getAmount())
                        .paymentMethod("CASH")
                        .cashierName(
                                payment.getCreatorUser() != null ? payment.getCreatorUser().getUsername() : "System")
                        .lineItems(lines)
                        .build();
            }

            throw new RuntimeException("Transaction not found");

        } catch (Exception e) {
            log.error("Error fetching receipt data", e);
            throw new RuntimeException("Error fetching receipt data");
        }
    }
}
