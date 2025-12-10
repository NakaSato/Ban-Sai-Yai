package com.bansaiyai.bansaiyai.integration;

import com.bansaiyai.bansaiyai.dto.PaymentNotificationRequest;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.PaymentNotification;
import com.bansaiyai.bansaiyai.entity.enums.NotificationStatus;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.PaymentNotificationRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Payment Notification feature
 * Tests the complete workflow from submission to approval/rejection
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentNotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentNotificationRepository paymentNotificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LoanRepository loanRepository;

    private Member testMember;
    private Loan testLoan;

    @BeforeEach
    void setUp() {
        // Set up test data
        // Note: Adjust based on your actual test data setup
    }

    /**
     * Test 1: Complete payment notification workflow
     * Submit → Approve → Verify
     */
    @Test
    @WithMockUser(username = "member1", roles = { "MEMBER" })
    void testCompletePaymentNotificationWorkflow() throws Exception {
        // 1. Submit payment notification
        MockMultipartFile slipFile = new MockMultipartFile(
                "slipFile",
                "slip.jpg",
                "image/jpeg",
                createValidJpegBytes());

        MvcResult submitResult = mockMvc.perform(multipart("/api/payment-notifications/notify")
                .file(slipFile)
                .param("loanId", "101")
                .param("amount", "5000.00")
                .param("paymentDate", LocalDateTime.now().toString())
                .param("notes", "Payment via SCB mobile banking"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Payment notification submitted successfully"))
                .andExpect(jsonPath("$.data.notificationUuid").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        // Extract UUID from response
        String responseBody = submitResult.getResponse().getContentAsString();
        String notificationUuid = JsonPath.read(responseBody, "$.data.notificationUuid");

        // Verify notification was created in database
        PaymentNotification notification = paymentNotificationRepository.findByUuid(UUID.fromString(notificationUuid))
                .orElseThrow();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getPayAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));

        // 2. Officer approves notification
        mockMvc.perform(post("/api/payment-notifications/" + notificationUuid + "/approve")
                .with(user -> {
                    user.setUsername("officer1");
                    user.setRoles("OFFICER");
                    return user;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.principalPaid").exists())
                .andExpect(jsonPath("$.interestPaid").exists())
                .andExpect(jsonPath("$.totalPaid").value(5000.00))
                .andExpect(jsonPath("$.receiptNumber").exists());

        // 3. Verify notification status updated
        mockMvc.perform(get("/api/payment-notifications/" + notificationUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedAt").exists())
                .andExpect(jsonPath("$.approvedBy").exists());

        // 4. Verify database state
        notification = paymentNotificationRepository.findByUuid(UUID.fromString(notificationUuid))
                .orElseThrow();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.APPROVED);
        assertThat(notification.getApprovedAt()).isNotNull();
        assertThat(notification.getPaymentId()).isNotNull();
    }

    /**
     * Test 2: Payment notification rejection workflow
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testPaymentNotificationRejection() throws Exception {
        // Create a pending notification
        PaymentNotification notification = createTestNotification();
        String notificationUuid = notification.getUuid().toString();

        // Reject notification
        String rejectRequest = """
                {
                    "reason": "Transfer slip is unclear, please resubmit with better quality image"
                }
                """;

        mockMvc.perform(post("/api/payment-notifications/" + notificationUuid + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rejectRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment notification rejected successfully"));

        // Verify rejection
        PaymentNotification rejected = paymentNotificationRepository.findByUuid(notification.getUuid())
                .orElseThrow();
        assertThat(rejected.getStatus()).isEqualTo(NotificationStatus.REJECTED);
        assertThat(rejected.getRejectedAt()).isNotNull();
        assertThat(rejected.getOfficerComment()).contains("unclear");
    }

    /**
     * Test 3: ID enumeration prevention
     * Verify that sequential IDs don't work
     */
    @Test
    @WithMockUser(username = "member1", roles = { "MEMBER" })
    void testIdEnumerationPrevention() throws Exception {
        // Try to access with sequential Long ID (should fail - bad request)
        mockMvc.perform(get("/api/payment-notifications/1"))
                .andExpect(status().isBadRequest()); // UUID parse error

        // Try to access with random UUID (should return 404)
        UUID randomUuid = UUID.randomUUID();
        mockMvc.perform(get("/api/payment-notifications/" + randomUuid))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4: Member can only access own notifications
     */
    @Test
    @WithMockUser(username = "member1", roles = { "MEMBER" })
    void testMemberOwnershipVerification() throws Exception {
        // Create notification for different member
        PaymentNotification otherMemberNotification = createTestNotification();
        // Assume this belongs to member2

        // Member1 tries to access member2's notification
        mockMvc.perform(get("/api/payment-notifications/" + otherMemberNotification.getUuid()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    /**
     * Test 5: Anti-fraud - duplicate detection
     */
    @Test
    @WithMockUser(username = "member1", roles = { "MEMBER" })
    void testDuplicateNotificationPrevention() throws Exception {
        MockMultipartFile slipFile = new MockMultipartFile(
                "slipFile", "slip.jpg", "image/jpeg", createValidJpegBytes());

        // Submit first notification
        mockMvc.perform(multipart("/api/payment-notifications/notify")
                .file(slipFile)
                .param("loanId", "101")
                .param("amount", "5000.00")
                .param("paymentDate", LocalDateTime.now().toString()))
                .andExpect(status().isCreated());

        // Try to submit duplicate (same loan, within 1 hour)
        mockMvc.perform(multipart("/api/payment-notifications/notify")
                .file(slipFile)
                .param("loanId", "101")
                .param("amount", "3000.00")
                .param("paymentDate", LocalDateTime.now().toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("already have a pending notification")));
    }

    /**
     * Test 6: File validation - invalid file type
     */
    @Test
    @WithMockUser(username = "member1", roles = { "MEMBER" })
    void testInvalidFileTypeRejection() throws Exception {
        // Create fake PDF (actually text file)
        MockMultipartFile fakeFile = new MockMultipartFile(
                "slipFile",
                "malicious.pdf",
                "application/pdf",
                "This is not a real PDF".getBytes());

        mockMvc.perform(multipart("/api/payment-notifications/notify")
                .file(fakeFile)
                .param("loanId", "101")
                .param("amount", "5000.00")
                .param("paymentDate", LocalDateTime.now().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("File content does not match extension")));
    }

    /**
     * Test 7: Get pending notifications (Officer)
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testGetPendingNotifications() throws Exception {
        // Create some pending notifications
        createTestNotification();
        createTestNotification();

        mockMvc.perform(get("/api/payment-notifications/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications[0].notificationUuid").exists())
                .andExpect(jsonPath("$.notifications[0].status").value("PENDING"));
    }

    /**
     * Test 8: Get member notification history
     */
    @Test
    @WithMockUser(username = "member1", roles = { "MEMBER" })
    void testGetMemberNotificationHistory() throws Exception {
        mockMvc.perform(get("/api/payment-notifications/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.notifications").isArray());

        // Filter by status
        mockMvc.perform(get("/api/payment-notifications/history?status=APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications[*].status").value(everyItem(equalTo("APPROVED"))));
    }

    // Helper methods

    private PaymentNotification createTestNotification() {
        PaymentNotification notification = PaymentNotification.builder()
                .member(testMember)
                .loan(testLoan)
                .payAmount(new BigDecimal("5000.00"))
                .payDate(LocalDateTime.now())
                .slipImage("test_slip.jpg")
                .status(NotificationStatus.PENDING)
                .build();
        return paymentNotificationRepository.save(notification);
    }

    private byte[] createValidJpegBytes() {
        // JPEG signature: FF D8 FF
        return new byte[] {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
        };
    }
}
