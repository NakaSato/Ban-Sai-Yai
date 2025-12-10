package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.*;
import com.bansaiyai.bansaiyai.entity.enums.NotificationStatus;
import com.bansaiyai.bansaiyai.service.PaymentNotificationService;
import com.bansaiyai.bansaiyai.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * REST controller for payment notification operations.
 * Handles member-submitted payment notifications and officer verification
 * workflow.
 */
@RestController
@RequestMapping("/api/payment-notifications")
@Slf4j
@RequiredArgsConstructor
public class PaymentNotificationController {

    private final PaymentNotificationService paymentNotificationService;
    private final UserContext userContext;

    /**
     * Submit a new payment notification (Member)
     * POST /api/payment-notifications/notify
     */
    @PostMapping(value = "/notify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MEMBER') or hasRole('OFFICER')")
    public ResponseEntity<?> submitNotification(
            @RequestParam("loanId") Long loanId,
            @RequestParam("amount") String amount,
            @RequestParam("paymentDate") String paymentDate,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "slipFile", required = false) MultipartFile slipFile) {

        try {
            // Get current member ID from security context
            Long memberId = userContext.getCurrentMemberId();
            if (memberId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Member ID not found in security context"));
            }

            // Build request DTO
            PaymentNotificationRequest request = PaymentNotificationRequest.builder()
                    .loanId(loanId)
                    .amount(new java.math.BigDecimal(amount))
                    .paymentDate(java.time.LocalDateTime.parse(paymentDate))
                    .notes(notes)
                    .build();

            // Submit notification
            PaymentNotificationResponse response = paymentNotificationService.submitNotification(
                    request, slipFile, memberId, userContext.getCurrentUsername());

            log.info("Payment notification submitted: ID={}, Member={}",
                    response.getNotificationId(), memberId);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Payment notification submitted successfully",
                    "notificationId", response.getNotificationId(),
                    "status", response.getStatus(),
                    "data", response));

        } catch (IllegalArgumentException e) {
            log.error("Validation error submitting notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("State error submitting notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error submitting payment notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit notification: " + e.getMessage()));
        }
    }

    /**
     * Get all pending notifications (Officer/Admin)
     * GET /api/payment-notifications/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT')")
    public ResponseEntity<?> getPendingNotifications() {
        try {
            List<PaymentNotificationResponse> notifications = paymentNotificationService.getPendingNotifications();

            log.info("Retrieved {} pending notifications", notifications.size());

            return ResponseEntity.ok(Map.of(
                    "count", notifications.size(),
                    "notifications", notifications));

        } catch (Exception e) {
            log.error("Error getting pending notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve pending notifications"));
        }
    }

    /**
     * Get notification history for current member
     * GET /api/payment-notifications/history
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('MEMBER') or hasRole('OFFICER')")
    public ResponseEntity<?> getMemberNotificationHistory(
            @RequestParam(required = false) String status) {
        try {
            Long memberId = userContext.getCurrentMemberId();
            if (memberId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Member ID not found"));
            }

            NotificationStatus notificationStatus = null;
            if (status != null && !status.isEmpty()) {
                notificationStatus = NotificationStatus.valueOf(status.toUpperCase());
            }

            List<PaymentNotificationResponse> notifications = paymentNotificationService
                    .getMemberNotificationHistory(memberId, notificationStatus);

            return ResponseEntity.ok(Map.of(
                    "count", notifications.size(),
                    "notifications", notifications));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
        } catch (Exception e) {
            log.error("Error getting notification history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve notification history"));
        }
    }

    /**
     * Get notification by UUID (secure - prevents ID enumeration)
     * GET /api/payment-notifications/{uuid}
     */
    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('OFFICER')")
    public ResponseEntity<?> getNotification(@PathVariable java.util.UUID uuid) {
        try {
            PaymentNotificationResponse notification = paymentNotificationService.getNotificationByUuid(uuid);

            // Security: Check if current user is the member or has officer role
            Long currentMemberId = userContext.getCurrentMemberId();
            boolean isOfficer = userContext.hasRole("OFFICER") ||
                    userContext.hasRole("PRESIDENT") ||
                    userContext.hasRole("SECRETARY");

            // Note: We can't check memberUuid directly since it's a string in response
            // This check would need to be done in the service layer
            // For now, service layer handles authorization

            return ResponseEntity.ok(notification);

        } catch (IllegalArgumentException e) {
            // Return 404 for both "not found" and "not authorized" to prevent information
            // leakage
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting notification {}", uuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve notification"));
        }
    }

    /**
     * Approve a payment notification (Officer/Admin)
     * POST /api/payment-notifications/{uuid}/approve
     */
    @PostMapping("/{uuid}/approve")
    @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
    public ResponseEntity<?> approveNotification(@PathVariable java.util.UUID uuid) {
        try {
            Long approverId = userContext.getCurrentUserId();
            if (approverId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "User ID not found"));
            }

            // Find notification by UUID first
            PaymentNotificationResponse notification = paymentNotificationService.getNotificationByUuid(uuid);
            // Then approve using internal ID (service layer needs internal ID)
            // This is a temporary bridge - ideally service layer should accept UUID
            PaymentApprovalResponse response = paymentNotificationService.approveNotification(
                    notification.getNotificationId(), approverId);

            log.info("Payment notification approved: UUID={}, Approver={}", uuid, approverId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error approving notification {}: {}", uuid, e.getMessage());
            return ResponseEntity.notFound().build(); // Consistent error response
        } catch (IllegalStateException e) {
            log.error("State error approving notification {}: {}", uuid, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error approving notification {}", uuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to approve notification: " + e.getMessage()));
        }
    }

    /**
     * Reject a payment notification (Officer/Admin)
     * POST /api/payment-notifications/{uuid}/reject
     */
    @PostMapping("/{uuid}/reject")
    @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
    public ResponseEntity<?> rejectNotification(
            @PathVariable java.util.UUID uuid,
            @Valid @RequestBody PaymentNotificationRejectRequest request) {
        try {
            Long rejectorId = userContext.getCurrentUserId();
            if (rejectorId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "User ID not found"));
            }

            // Find notification by UUID first
            PaymentNotificationResponse notification = paymentNotificationService.getNotificationByUuid(uuid);
            // Then reject using internal ID
            paymentNotificationService.rejectNotification(
                    notification.getNotificationId(), request, rejectorId);

            log.info("Payment notification rejected: UUID={}, Rejector={}", uuid, rejectorId);

            return ResponseEntity.ok(Map.of(
                    "message", "Payment notification rejected successfully",
                    "notificationUuid", uuid.toString()));

        } catch (IllegalArgumentException e) {
            log.error("Validation error rejecting notification {}: {}", uuid, e.getMessage());
            return ResponseEntity.notFound().build(); // Consistent error response
        } catch (IllegalStateException e) {
            log.error("State error rejecting notification {}: {}", uuid, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error rejecting notification {}", uuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reject notification: " + e.getMessage()));
        }
    }

    /**
     * Serve slip image file
     * GET /api/payment-notifications/slips/{filename}
     */
    @GetMapping("/slips/{filename:.+}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('OFFICER')")
    public ResponseEntity<Resource> getSlipImage(@PathVariable String filename) {
        try {
            // Security: Validate filename to prevent path traversal
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                log.warn("Path traversal attempt detected: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            // Security: Validate file extension
            String lowerFilename = filename.toLowerCase();
            if (!lowerFilename.endsWith(".jpg") && !lowerFilename.endsWith(".jpeg") &&
                    !lowerFilename.endsWith(".png") && !lowerFilename.endsWith(".pdf")) {
                log.warn("Invalid file extension requested: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            // Load file as Resource
            Path filePath = Paths.get("/opt/bansaiyai/uploads/slips").resolve(filename).normalize();

            // Security: Ensure resolved path is still within allowed directory
            Path allowedDir = Paths.get("/opt/bansaiyai/uploads/slips").normalize();
            if (!filePath.startsWith(allowedDir)) {
                log.warn("Path traversal attempt blocked: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = "application/octet-stream";
            if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFilename.endsWith(".pdf")) {
                contentType = "application/pdf";
            }

            // Security: Set security headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .header("X-Content-Type-Options", "nosniff")
                    .header("X-Frame-Options", "DENY")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving slip image: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get notification statistics (Officer/Admin)
     * GET /api/payment-notifications/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT')")
    public ResponseEntity<?> getStatistics() {
        try {
            // This would call a service method to get statistics
            // For now, return basic info
            List<PaymentNotificationResponse> pending = paymentNotificationService.getPendingNotifications();

            return ResponseEntity.ok(Map.of(
                    "pendingCount", pending.size(),
                    "overdueCount", pending.stream().filter(PaymentNotificationResponse::getIsOverdue).count(),
                    "message", "Statistics endpoint - full implementation pending"));

        } catch (Exception e) {
            log.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve statistics"));
        }
    }
}
