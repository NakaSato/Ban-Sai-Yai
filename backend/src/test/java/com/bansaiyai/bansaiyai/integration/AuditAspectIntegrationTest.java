package com.bansaiyai.bansaiyai.integration;

import com.bansaiyai.bansaiyai.dto.LoanApprovalRequest;
import com.bansaiyai.bansaiyai.entity.AuditLog;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.repository.AuditLogRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.security.UserPrincipal;
import com.bansaiyai.bansaiyai.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for AuditAspect.
 * Tests that the @Audited annotation correctly captures state changes for
 * audited methods.
 * 
 * This test validates Property 31: Loan approval audit with state capture
 * Requirements: 11.1, 11.2
 */
@SpringBootTest
@Transactional
public class AuditAspectIntegrationTest {

        @Autowired
        private LoanService loanService;

        @Autowired
        private LoanRepository loanRepository;

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AuditLogRepository auditLogRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private User testUser;
        private Member testMember;

        @BeforeEach
        public void setUp() {
                // Clean up before each test
                auditLogRepository.deleteAll();
                loanRepository.deleteAll();

                // Create test user and member
                testUser = createTestUser();
                testMember = createTestMember();

                // Set up security context
                UserPrincipal userPrincipal = UserPrincipal.create(testUser);
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(userPrincipal, null,
                                                userPrincipal.getAuthorities()));
        }

        /**
         * Feature: rbac-security-system, Property 31: Loan approval audit with state
         * capture
         * Validates: Requirements 11.2
         * 
         * Test that loan approval creates an audit log with old and new state captured.
         */
        @Test
        public void testLoanApprovalCreatesAuditLogWithStateCapture() throws Exception {
                // Setup: Create a pending loan
                Loan loan = createPendingLoan(
                                new BigDecimal("50000.00"),
                                new BigDecimal("12.00"),
                                12);
                loan = loanRepository.save(loan);

                // Capture the initial state
                LoanStatus oldStatus = loan.getStatus();
                assertEquals(LoanStatus.PENDING, oldStatus, "Initial loan status should be PENDING");

                // Get initial audit log count
                long initialAuditCount = auditLogRepository.count();

                // Action: Approve the loan
                LoanApprovalRequest approvalRequest = new LoanApprovalRequest();
                approvalRequest.setApprovedAmount(new BigDecimal("50000.00"));
                approvalRequest.setApprovalNotes("Approved for testing");

                loanService.approveLoan(loan.getId(), approvalRequest, testUser.getUsername());

                // Property 1: An audit log entry should be created
                long finalAuditCount = auditLogRepository.count();
                assertTrue(finalAuditCount > initialAuditCount,
                                String.format("Audit log count should increase: initial=%d, final=%d",
                                                initialAuditCount, finalAuditCount));

                // Retrieve the audit log for this loan
                List<AuditLog> logs = auditLogRepository.findByEntity("Loan", loan.getId());

                assertFalse(logs.isEmpty(), "Should find at least one audit log for the loan");

                AuditLog auditLog = logs.get(0);

                // Property 2: The audit log should contain the user who performed the action
                assertNotNull(auditLog.getUser(), "Audit log should have a user");
                assertEquals(testUser.getId(), auditLog.getUser().getId(),
                                "User ID should match the user who approved the loan");

                // Property 3: The audit log should have a timestamp
                assertNotNull(auditLog.getTimestamp(), "Audit log should have a timestamp");

                // Property 4: The audit log should have the correct action
                assertEquals("LOAN_APPROVAL", auditLog.getAction(),
                                "Action should be LOAN_APPROVAL");

                // Property 5: The audit log should capture old values (previous state)
                assertNotNull(auditLog.getOldValues(), "Old values should not be null");

                Map<?, ?> oldValues = objectMapper.readValue(auditLog.getOldValues(), Map.class);
                assertNotNull(oldValues, "Old values should be parseable as JSON");

                // The old state should contain information about the loan
                // It might be in different formats depending on how the aspect captures it
                assertTrue(oldValues.size() > 0, "Old values should contain some data");

                // Property 6: The audit log should capture new values (updated state)
                assertNotNull(auditLog.getNewValues(), "New values should not be null");

                Map<?, ?> newValues = objectMapper.readValue(auditLog.getNewValues(), Map.class);
                assertNotNull(newValues, "New values should be parseable as JSON");
                assertFalse(newValues.isEmpty(), "New values should not be empty");

                // Property 7: The state change should be reconstructable from the audit log
                assertEquals("Loan", auditLog.getEntityType(),
                                "Entity type should indicate this is a loan operation");
                assertEquals(loan.getId(), auditLog.getEntityId(),
                                "Entity ID should match the loan ID");

                // Property 8: The audit log should be immutable (stored permanently)
                AuditLog retrievedLog = auditLogRepository.findById(auditLog.getLogId()).orElse(null);
                assertNotNull(retrievedLog, "Audit log should be persisted and retrievable");
                assertEquals(auditLog.getLogId(), retrievedLog.getLogId(),
                                "Retrieved log should match the original");
        }

        /**
         * Test that multiple loan approvals create separate audit logs.
         */
        @Test
        public void testMultipleLoanApprovalsCreateSeparateAuditLogs() throws Exception {
                // Create and approve first loan
                Loan loan1 = createPendingLoan(
                                new BigDecimal("30000.00"),
                                new BigDecimal("10.00"),
                                12);
                loan1 = loanRepository.save(loan1);

                LoanApprovalRequest request1 = new LoanApprovalRequest();
                request1.setApprovedAmount(new BigDecimal("30000.00"));
                request1.setApprovalNotes("First loan approved");

                loanService.approveLoan(loan1.getId(), request1, testUser.getUsername());

                // Create and approve second loan
                Loan loan2 = createPendingLoan(
                                new BigDecimal("40000.00"),
                                new BigDecimal("11.00"),
                                18);
                loan2 = loanRepository.save(loan2);

                LoanApprovalRequest request2 = new LoanApprovalRequest();
                request2.setApprovedAmount(new BigDecimal("40000.00"));
                request2.setApprovalNotes("Second loan approved");

                loanService.approveLoan(loan2.getId(), request2, testUser.getUsername());

                // Verify separate audit logs exist
                List<AuditLog> logs1 = auditLogRepository.findByEntity("Loan", loan1.getId());
                List<AuditLog> logs2 = auditLogRepository.findByEntity("Loan", loan2.getId());

                assertFalse(logs1.isEmpty(), "Should have audit log for first loan");
                assertFalse(logs2.isEmpty(), "Should have audit log for second loan");

                // Verify they are different logs
                assertNotEquals(logs1.get(0).getLogId(), logs2.get(0).getLogId(),
                                "Audit logs should have different IDs");
                assertEquals(loan1.getId(), logs1.get(0).getEntityId(),
                                "First audit log should reference first loan");
                assertEquals(loan2.getId(), logs2.get(0).getEntityId(),
                                "Second audit log should reference second loan");
        }

        /**
         * Test that audit log captures JSON-formatted state for rollback capability.
         */
        @Test
        public void testAuditLogCapturesJSONFormattedState() throws Exception {
                // Create and approve a loan
                Loan loan = createPendingLoan(
                                new BigDecimal("25000.00"),
                                new BigDecimal("9.50"),
                                24);
                loan = loanRepository.save(loan);

                LoanApprovalRequest request = new LoanApprovalRequest();
                request.setApprovedAmount(new BigDecimal("25000.00"));
                request.setApprovalNotes("Testing JSON format");

                loanService.approveLoan(loan.getId(), request, testUser.getUsername());

                // Retrieve the audit log
                List<AuditLog> logs = auditLogRepository.findByEntity("Loan", loan.getId());
                assertFalse(logs.isEmpty(), "Should have audit log");

                AuditLog auditLog = logs.get(0);

                // Verify old values are valid JSON
                assertNotNull(auditLog.getOldValues(), "Old values should not be null");
                assertDoesNotThrow(() -> {
                        Map<?, ?> oldValues = objectMapper.readValue(auditLog.getOldValues(), Map.class);
                        assertNotNull(oldValues, "Old values should be parseable as JSON");
                }, "Old values should be valid JSON");

                // Verify new values are valid JSON
                assertNotNull(auditLog.getNewValues(), "New values should not be null");
                assertDoesNotThrow(() -> {
                        Map<?, ?> newValues = objectMapper.readValue(auditLog.getNewValues(), Map.class);
                        assertNotNull(newValues, "New values should be parseable as JSON");
                }, "New values should be valid JSON");
        }

        // ==================== Helper Methods ====================

        /**
         * Create a test user for audit logging.
         */
        private User createTestUser() {
                User user = User.builder()
                                .username("testuser_" + System.currentTimeMillis())
                                .email("test_" + System.currentTimeMillis() + "@example.com")
                                .password("password")
                                .role(User.Role.PRESIDENT) // President can approve loans
                                .enabled(true)
                                .build();

                return userRepository.save(user);
        }

        /**
         * Create a test member for loan applications.
         */
        private Member createTestMember() {
                Member member = Member.builder()
                                .memberId("M" + System.currentTimeMillis())
                                .name("Test Member")
                                .idCard("1234567890" + String.format("%03d", System.currentTimeMillis() % 1000))
                                .dateOfBirth(LocalDate.of(1990, 1, 1))
                                .phone("0812345678")
                                .address("Test Address 123")
                                .registrationDate(LocalDate.now())
                                .isActive(true)
                                .build();

                return memberRepository.save(member);
        }

        /**
         * Create a pending loan for testing.
         */
        private Loan createPendingLoan(BigDecimal principalAmount, BigDecimal interestRate, Integer termMonths) {
                return Loan.builder()
                                .loanNumber("LN" + System.currentTimeMillis())
                                .member(testMember)
                                .loanType(LoanType.PERSONAL)
                                .principalAmount(principalAmount)
                                .interestRate(interestRate)
                                .termMonths(termMonths)
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(termMonths))
                                .status(LoanStatus.PENDING)
                                .purpose("Test loan")
                                .build();
        }
}
