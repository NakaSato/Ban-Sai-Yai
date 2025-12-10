package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.*;
import com.bansaiyai.bansaiyai.entity.*;
import com.bansaiyai.bansaiyai.entity.enums.NotificationStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import com.bansaiyai.bansaiyai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing payment notifications submitted by members via mobile
 * devices.
 * Handles the workflow: Member submits -> Officer verifies -> System processes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final PaymentNotificationRepository paymentNotificationRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.slip-images-dir:/opt/bansaiyai/uploads/slips}")
    private String slipImagesDir;

    @Value("${app.upload.max-file-size:5242880}") // 5MB default
    private long maxFileSize;

    @Value("${app.payment-notification.rate-limit-hours:24}")
    private int rateLimitHours;

    @Value("${app.payment-notification.rate-limit-count:5}")
    private int rateLimitCount;

    @Value("${app.payment-notification.duplicate-window-hours:1}")
    private int duplicateWindowHours;

    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "application/pdf");

    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".pdf");

    /**
     * Submit a new payment notification from member
     */
    @Transactional
    public PaymentNotificationResponse submitNotification(
            PaymentNotificationRequest request,
            MultipartFile slipFile,
            Long memberId,
            String createdBy) {

        log.info("Submitting payment notification for member: {}, loan: {}, amount: {}",
                memberId, request.getLoanId(), request.getAmount());

        // 1. Validate member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        if (!member.getIsActive()) {
            throw new IllegalStateException("Member account is not active");
        }

        // 2. Validate loan
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + request.getLoanId()));

        if (!loan.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("Loan does not belong to this member");
        }

        if (!loan.isActive()) {
            throw new IllegalStateException("Loan is not active");
        }

        // 3. Validate payment amount
        validatePaymentAmount(request.getAmount(), loan);

        // 4. Anti-fraud checks
        performAntiFraudChecks(memberId, request.getLoanId());

        // 5. Save slip image
        String slipImagePath = null;
        if (slipFile != null && !slipFile.isEmpty()) {
            slipImagePath = saveSlipImage(slipFile);
        }

        // 6. Create notification
        PaymentNotification notification = PaymentNotification.builder()
                .member(member)
                .loan(loan)
                .payAmount(request.getAmount())
                .payDate(request.getPaymentDate())
                .slipImage(slipImagePath)
                .status(NotificationStatus.PENDING)
                .build();

        notification = paymentNotificationRepository.save(notification);

        log.info("Payment notification created successfully: ID={}, Member={}, Amount={}",
                notification.getId(), member.getMemberId(), request.getAmount());

        return mapToResponse(notification);
    }

    /**
     * Get all pending notifications for officer verification
     */
    @Transactional(readOnly = true)
    public List<PaymentNotificationResponse> getPendingNotifications() {
        List<PaymentNotification> pending = paymentNotificationRepository.findPendingNotifications();
        return pending.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get notification history for a member
     */
    @Transactional(readOnly = true)
    public List<PaymentNotificationResponse> getMemberNotificationHistory(
            Long memberId,
            NotificationStatus status) {

        List<PaymentNotification> notifications;
        if (status != null) {
            notifications = paymentNotificationRepository.findByMemberIdAndStatus(memberId, status);
        } else {
            notifications = paymentNotificationRepository.findByMemberId(memberId);
        }

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approve a payment notification and create payment record
     */
    @Transactional
    public PaymentApprovalResponse approveNotification(Long notificationId, Long approverId) {

        log.info("Approving payment notification: ID={}, Approver={}", notificationId, approverId);

        // 1. Load notification with pessimistic lock to prevent concurrent approval
        PaymentNotification notification = paymentNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (!notification.canBeProcessed()) {
            throw new IllegalStateException(
                    "Notification cannot be approved in current status: " + notification.getStatus());
        }

        // 2. Load approver
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException("Approver not found: " + approverId));

        // 3. Calculate interest and principal split
        Loan loan = notification.getLoan();
        BigDecimal paymentAmount = notification.getPayAmount();

        PaymentBreakdown breakdown = calculatePaymentBreakdown(loan, paymentAmount, notification.getPayDate());

        // 4. Create Payment record
        Payment payment = Payment.builder()
                .paymentNumber(generatePaymentNumber())
                .member(notification.getMember())
                .loan(loan)
                .paymentType(PaymentType.LOAN_REPAYMENT)
                .paymentStatus(PaymentStatus.COMPLETED)
                .amount(paymentAmount)
                .principalAmount(breakdown.getPrincipalAmount())
                .interestAmount(breakdown.getInterestAmount())
                .penaltyAmount(breakdown.getPenaltyAmount())
                .paymentDate(notification.getPayDate().toLocalDate())
                .processedDate(LocalDateTime.now())
                .completedDate(LocalDateTime.now())
                .paymentMethod("BANK_TRANSFER")
                .referenceNumber(notification.getSlipImage())
                .description("Payment via mobile notification #" + notificationId)
                .isVerified(true)
                .verifiedBy(approver.getUsername())
                .verifiedDate(LocalDateTime.now())
                .createdBy(approver.getUsername())
                .build();

        payment = paymentRepository.save(payment);

        // 5. Update loan balance
        updateLoanBalance(loan, breakdown);

        // 6. Create accounting entries
        createAccountingEntries(payment, breakdown);

        // 7. Generate receipt (simplified - would integrate with ReceiptService)
        String receiptNumber = generateReceiptNumber();

        // 8. Approve notification
        notification.approve(approver, payment, null); // Receipt entity to be created
        paymentNotificationRepository.save(notification);

        log.info("Payment notification approved successfully: ID={}, Payment={}, Receipt={}",
                notificationId, payment.getPaymentNumber(), receiptNumber);

        return PaymentApprovalResponse.builder()
                .message("Payment approved successfully")
                .notificationId(notificationId)
                .paymentId(payment.getId())
                .receiptNumber(receiptNumber)
                .principalPaid(breakdown.getPrincipalAmount())
                .interestPaid(breakdown.getInterestAmount())
                .totalPaid(paymentAmount)
                .newOutstandingBalance(loan.getOutstandingBalance())
                .build();
    }

    /**
     * Reject a payment notification
     */
    @Transactional
    public void rejectNotification(Long notificationId, PaymentNotificationRejectRequest request, Long rejectorId) {

        log.info("Rejecting payment notification: ID={}, Rejector={}", notificationId, rejectorId);

        PaymentNotification notification = paymentNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (!notification.canBeProcessed()) {
            throw new IllegalStateException(
                    "Notification cannot be rejected in current status: " + notification.getStatus());
        }

        User rejector = userRepository.findById(rejectorId)
                .orElseThrow(() -> new IllegalArgumentException("Rejector not found: " + rejectorId));

        notification.reject(rejector, request.getReason());
        paymentNotificationRepository.save(notification);

        log.info("Payment notification rejected: ID={}, Reason={}", notificationId, request.getReason());
    }

    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public PaymentNotificationResponse getNotification(Long notificationId) {
        PaymentNotification notification = paymentNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        return mapToResponse(notification);
    }

    /**
     * Get notification by UUID (for API endpoints - secure)
     */
    @Transactional(readOnly = true)
    public PaymentNotificationResponse getNotificationByUuid(UUID uuid) {
        PaymentNotification notification = paymentNotificationRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        return mapToResponse(notification);
    }

    // ============================================================================
    // Private Helper Methods
    // ============================================================================

    /**
     * Validate payment amount
     */
    private void validatePaymentAmount(BigDecimal amount, Loan loan) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        BigDecimal outstandingBalance = loan.getOutstandingBalance();
        if (outstandingBalance != null && amount.compareTo(outstandingBalance) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment amount (%.2f) exceeds outstanding balance (%.2f)",
                            amount, outstandingBalance));
        }

        // Check if amount is reasonable (not too small)
        BigDecimal minimumPayment = BigDecimal.valueOf(100); // Configurable
        if (amount.compareTo(minimumPayment) < 0) {
            throw new IllegalArgumentException(
                    String.format("Payment amount must be at least %.2f", minimumPayment));
        }
    }

    /**
     * Perform anti-fraud checks
     */
    private void performAntiFraudChecks(Long memberId, Long loanId) {
        // 1. Check for duplicate notifications within time window
        LocalDateTime duplicateWindow = LocalDateTime.now().minusHours(duplicateWindowHours);
        boolean hasDuplicate = paymentNotificationRepository.hasPendingNotificationForLoan(
                memberId, loanId, duplicateWindow);

        if (hasDuplicate) {
            throw new IllegalStateException(
                    "You already have a pending notification for this loan. Please wait for approval.");
        }

        // 2. Check rate limiting
        LocalDateTime rateLimitWindow = LocalDateTime.now().minusHours(rateLimitHours);
        long recentNotifications = paymentNotificationRepository.countPendingByMemberSince(
                memberId, rateLimitWindow);

        if (recentNotifications >= rateLimitCount) {
            throw new IllegalStateException(
                    String.format("You have exceeded the maximum of %d notifications per %d hours",
                            rateLimitCount, rateLimitHours));
        }
    }

    /**
     * Save slip image to file system
     */
    private String saveSlipImage(MultipartFile file) {
        try {
            // Validate file
            validateSlipFile(file);

            // Create directory if not exists
            Path uploadPath = Paths.get(slipImagesDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String filename = String.format("%s_%s%s",
                    UUID.randomUUID().toString(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                    extension);

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Slip image saved: {}", filename);
            return filename;

        } catch (IOException e) {
            log.error("Failed to save slip image", e);
            throw new RuntimeException("Failed to save slip image: " + e.getMessage());
        }
    }

    /**
     * Validate slip file
     */
    private void validateSlipFile(MultipartFile file) {
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum limit of %d MB",
                            maxFileSize / 1024 / 1024));
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: JPG, PNG, PDF");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Invalid file extension. Allowed extensions: .jpg, .jpeg, .png, .pdf");
        }
    }

    /**
     * Calculate payment breakdown (principal, interest, penalty)
     * Uses payment date for accurate calculation
     */
    private PaymentBreakdown calculatePaymentBreakdown(Loan loan, BigDecimal paymentAmount, LocalDateTime paymentDate) {

        // Calculate accrued interest up to payment date
        BigDecimal accruedInterest = loan.calculateAccruedInterest(paymentDate.toLocalDate());

        // Calculate penalty if overdue
        BigDecimal penalty = loan.calculatePenalty();

        BigDecimal remainingAmount = paymentAmount;

        // 1. First pay penalty
        BigDecimal penaltyPaid = penalty.min(remainingAmount);
        remainingAmount = remainingAmount.subtract(penaltyPaid);

        // 2. Then pay interest
        BigDecimal interestPaid = accruedInterest.min(remainingAmount);
        remainingAmount = remainingAmount.subtract(interestPaid);

        // 3. Finally pay principal
        BigDecimal principalPaid = remainingAmount;

        return new PaymentBreakdown(principalPaid, interestPaid, penaltyPaid);
    }

    /**
     * Update loan balance after payment
     */
    private void updateLoanBalance(Loan loan, PaymentBreakdown breakdown) {
        // Update outstanding balance
        BigDecimal currentBalance = loan.getOutstandingBalance();
        BigDecimal newBalance = currentBalance.subtract(breakdown.getPrincipalAmount());
        loan.setOutstandingBalance(newBalance);

        // Update paid amounts
        BigDecimal currentPaidPrincipal = loan.getPaidPrincipal() != null ? loan.getPaidPrincipal() : BigDecimal.ZERO;
        loan.setPaidPrincipal(currentPaidPrincipal.add(breakdown.getPrincipalAmount()));

        BigDecimal currentPaidInterest = loan.getPaidInterest() != null ? loan.getPaidInterest() : BigDecimal.ZERO;
        loan.setPaidInterest(currentPaidInterest.add(breakdown.getInterestAmount()));

        // Check if loan is fully paid
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(com.bansaiyai.bansaiyai.entity.enums.LoanStatus.COMPLETED);
        }

        loanRepository.save(loan);
    }

    /**
     * Create accounting entries for the payment
     */
    private void createAccountingEntries(Payment payment, PaymentBreakdown breakdown) {
        // This would integrate with AccountingService
        // For now, just log
        log.info("Creating accounting entries for payment: {}", payment.getPaymentNumber());
        log.info("Principal: {}, Interest: {}, Penalty: {}",
                breakdown.getPrincipalAmount(),
                breakdown.getInterestAmount(),
                breakdown.getPenaltyAmount());
    }

    /**
     * Generate payment number
     */
    private String generatePaymentNumber() {
        return "PAY-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * Generate receipt number
     */
    private String generateReceiptNumber() {
        return "RCP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * Map entity to response DTO
     */
    private PaymentNotificationResponse mapToResponse(PaymentNotification notification) {
        Member member = notification.getMember();
        Loan loan = notification.getLoan();

        return PaymentNotificationResponse.builder()
                // Use UUIDs for security (prevent ID enumeration)
                .notificationUuid(notification.getUuid().toString())
                .memberUuid(member.getUuid().toString())
                .loanUuid(loan.getUuid().toString())
                .notificationId(notification.getId())

                // Business IDs (safe to expose)
                .memberNumber(member.getMemberId())
                .loanNumber(loan.getLoanNumber())

                // Member details
                .memberName(member.getName())

                // Loan details
                .outstandingBalance(loan.getOutstandingBalance())

                // Payment details
                .payAmount(notification.getPayAmount())
                .payDate(notification.getPayDate())
                .slipImageUrl(notification.getSlipImageUrl())
                .notes(notification.getNotes())

                // Status
                .status(notification.getStatus())
                .statusDisplay(notification.getStatusDisplayThai())

                // Approval details
                .approvedAt(notification.getApprovedAt())
                .approvedBy(notification.getApprovedByUser() != null
                        ? notification.getApprovedByUser().getUsername()
                        : null)
                .rejectedAt(notification.getRejectedAt())
                .rejectedBy(notification.getRejectedByUser() != null
                        ? notification.getRejectedByUser().getUsername()
                        : null)
                .officerComment(notification.getOfficerComment())

                // Audit
                .createdAt(notification.getCreatedAt())
                .daysPending((int) notification.getDaysPending())
                .isOverdue(notification.isOverdue())
                .build();
    }

    /**
     * Inner class for payment breakdown
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class PaymentBreakdown {
        private BigDecimal principalAmount;
        private BigDecimal interestAmount;
        private BigDecimal penaltyAmount;
    }
}
