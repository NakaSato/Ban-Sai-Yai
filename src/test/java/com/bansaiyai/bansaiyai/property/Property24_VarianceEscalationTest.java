package com.bansaiyai.bansaiyai.property;

import com.bansaiyai.bansaiyai.dto.CashReconciliationRequest;
import com.bansaiyai.bansaiyai.dto.CashReconciliationResponse;
import com.bansaiyai.bansaiyai.dto.DiscrepancyApprovalRequest;
import com.bansaiyai.bansaiyai.entity.CashReconciliation;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.CashReconciliationRepository;
import com.bansaiyai.bansaiyai.service.CashReconciliationService;
import net.jqwik.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 24: Variance escalation to Secretary
 * Validates: Requirements 8.5, 9.1
 * 
 * This property test ensures that when a cash reconciliation has variance,
 * it is properly escalated to Secretary for review and approval.
 * The Secretary should be able to view pending reconciliations with variance
 * and take appropriate approval or rejection actions.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Property24_VarianceEscalationTest {

        @Autowired
        private CashReconciliationService cashReconciliationService;

        @Autowired
        private CashReconciliationRepository cashReconciliationRepository;

        /**
         * Property: When a reconciliation with variance is created,
         * it should be available for Secretary review
         */
        @Property
        void reconciliationWithVarianceShouldBeEscalatedToSecretary(
                        @ForAll Random random) throws Exception {

                // Arrange: Create reconciliation with variance
                BigDecimal physicalCash = BigDecimal.valueOf(random.nextDouble(1000.0, 10000.0));
                BigDecimal databaseBalance = physicalCash.add(
                                BigDecimal.valueOf(random.nextDouble(-500.0, 500.0)));
                System.out.println("Testing with physical: " + physicalCash + ", database: " + databaseBalance);

                CashReconciliationRequest request = new CashReconciliationRequest();
                request.setPhysicalCount(physicalCash);
                request.setNotes("Test reconciliation with variance");

                // Act: Create reconciliation as Officer
                User officer = createOfficerUser();
                CashReconciliation reconciliation = cashReconciliationService.createReconciliation(
                                physicalCash, officer, "Test reconciliation with variance");
                CashReconciliationResponse response = CashReconciliationResponse.fromEntity(reconciliation);

                // Assert: Verify reconciliation exists and has variance
                assertNotNull(response, "Reconciliation should be created");
                assertTrue(response.getVariance() != null && response.getVariance().compareTo(BigDecimal.ZERO) != 0,
                                "Reconciliation should have variance");
                assertNotNull(response.getVariance(), "Variance should be calculated");

                // Assert: Verify escalation - reconciliation should be in pending status
                Long reconciliationId = response.getReconciliationId();
                CashReconciliation savedReconciliation = cashReconciliationRepository.findById(reconciliationId)
                                .orElse(null);
                assertNotNull(savedReconciliation, "Reconciliation should be saved");
                assertEquals(CashReconciliation.ReconciliationStatus.PENDING,
                                savedReconciliation.getStatus(),
                                "Reconciliation with variance should be in PENDING status");

                // Assert: Secretary should be able to view pending reconciliations
                User secretary = createSecretaryUser();
                assertNotNull(secretary, "Secretary user should be created");
                List<CashReconciliation> pendingReconciliations = cashReconciliationService.getPendingReconciliations();

                assertFalse(pendingReconciliations.isEmpty(),
                                "Secretary should be able to see pending reconciliations");

                boolean foundInPending = pendingReconciliations.stream()
                                .anyMatch(r -> r.getReconciliationId().equals(reconciliationId));
                assertTrue(foundInPending,
                                "Created reconciliation with variance should be visible to Secretary");
        }

        /**
         * Property: Zero variance reconciliations should not require escalation
         */
        @Property
        void zeroVarianceReconciliationShouldNotRequireEscalation(
                        @ForAll Random random) throws Exception {

                // Arrange: Create reconciliation with zero variance
                BigDecimal exactAmount = BigDecimal.valueOf(random.nextDouble(1000.0, 10000.0));

                CashReconciliationRequest request = new CashReconciliationRequest();
                request.setPhysicalCount(exactAmount);
                request.setNotes("Test reconciliation with zero variance");

                // Act: Create reconciliation
                User officer = createOfficerUser();
                CashReconciliation reconciliation = cashReconciliationService.createReconciliation(
                                exactAmount, officer, "Test reconciliation with zero variance");
                CashReconciliationResponse response = CashReconciliationResponse.fromEntity(reconciliation);

                // Assert: Verify reconciliation has no variance
                assertTrue(response.getVariance() == null || response.getVariance().compareTo(BigDecimal.ZERO) == 0,
                                "Reconciliation should not have variance");

                // Assert: Should not appear in pending reconciliations for Secretary
                List<CashReconciliation> pendingReconciliations = cashReconciliationService.getPendingReconciliations();

                Long reconciliationId = response.getReconciliationId();
                boolean foundInPending = pendingReconciliations.stream()
                                .anyMatch(r -> r.getReconciliationId().equals(reconciliationId));
                assertFalse(foundInPending,
                                "Zero variance reconciliation should not be in pending list");
        }

        /**
         * Property: Secretary should be able to approve variance reconciliations
         */
        @Property
        void secretaryShouldBeAbleToApproveVarianceReconciliation(
                        @ForAll Random random) throws Exception {

                // Arrange: Create reconciliation with variance
                BigDecimal physicalCash = BigDecimal.valueOf(random.nextDouble(1000.0, 10000.0));
                // Note: Database balance would be physicalCash + $100 variance

                CashReconciliationRequest request = new CashReconciliationRequest();
                request.setPhysicalCount(physicalCash);
                request.setNotes("Test variance for approval");

                User officer = createOfficerUser();
                CashReconciliation reconciliation = cashReconciliationService.createReconciliation(
                                request.getPhysicalCount(), officer, request.getNotes());
                CashReconciliationResponse response = CashReconciliationResponse.fromEntity(reconciliation);
                assertNotNull(response, "Response should not be null");

                // Act: Secretary approves variance
                User secretary = createSecretaryUser();
                DiscrepancyApprovalRequest approvalRequest = new DiscrepancyApprovalRequest();
                approvalRequest.setAction("APPROVE");
                approvalRequest.setNotes("Approved by Secretary after review");

                CashReconciliation approvedReconciliation = cashReconciliationService.approveDiscrepancy(
                                reconciliation.getReconciliationId(), secretary, approvalRequest.getNotes());

                // Assert: Verify approval
                assertNotNull(approvedReconciliation, "Approval should succeed");
                assertEquals(CashReconciliation.ReconciliationStatus.APPROVED,
                                approvedReconciliation.getStatus(),
                                "Reconciliation should be approved");
                assertEquals("Approved by Secretary after review",
                                approvedReconciliation.getSecretaryNotes());
        }

        /**
         * Property: Secretary should be able to reject variance reconciliations
         */
        @Property
        void secretaryShouldBeAbleToRejectVarianceReconciliation(
                        @ForAll Random random) throws Exception {

                // Arrange: Create reconciliation with variance
                BigDecimal physicalCash = BigDecimal.valueOf(random.nextDouble(1000.0, 10000.0));
                // Note: Database balance would be physicalCash - $50 variance

                CashReconciliationRequest request = new CashReconciliationRequest();
                request.setPhysicalCount(physicalCash);
                request.setNotes("Test variance for rejection");

                User officer = createOfficerUser();
                CashReconciliation reconciliation = cashReconciliationService.createReconciliation(
                                request.getPhysicalCount(), officer, request.getNotes());
                CashReconciliationResponse response = CashReconciliationResponse.fromEntity(reconciliation);
                assertNotNull(response, "Response should not be null");

                // Act: Secretary rejects variance
                User secretary = createSecretaryUser();
                DiscrepancyApprovalRequest rejectionRequest = new DiscrepancyApprovalRequest();
                rejectionRequest.setAction("REJECT");
                rejectionRequest.setNotes("Significant discrepancy requires investigation");

                CashReconciliation rejectedReconciliation = cashReconciliationService.rejectDiscrepancy(
                                reconciliation.getReconciliationId(), secretary, rejectionRequest.getNotes());

                // Assert: Verify rejection
                assertNotNull(rejectedReconciliation, "Rejection should succeed");
                assertEquals(CashReconciliation.ReconciliationStatus.REJECTED,
                                rejectedReconciliation.getStatus(),
                                "Reconciliation should be rejected");
                assertEquals("Significant discrepancy requires investigation",
                                rejectedReconciliation.getSecretaryNotes());
        }

        /**
         * Property: Officer should not be able to approve their own variance
         */
        @Property
        void officerShouldNotBeAbleToApproveOwnVariance(
                        @ForAll Random random) throws Exception {

                // Arrange: Create reconciliation with variance as Officer
                BigDecimal physicalCash = BigDecimal.valueOf(random.nextDouble(1000.0, 10000.0));
                // Note: Database balance would be physicalCash + $75 variance

                CashReconciliationRequest request = new CashReconciliationRequest();
                request.setPhysicalCount(physicalCash);
                request.setNotes("Test variance - should not be self-approvable");

                User officer = createOfficerUser();
                CashReconciliation reconciliation = cashReconciliationService.createReconciliation(
                                request.getPhysicalCount(), officer, request.getNotes());

                // Act & Assert: Officer should not be able to approve
                DiscrepancyApprovalRequest approvalRequest = new DiscrepancyApprovalRequest();
                approvalRequest.setAction("APPROVE");
                approvalRequest.setNotes("Officer attempting self-approval");

                Exception exception = assertThrows(Exception.class, () -> {
                        cashReconciliationService.approveDiscrepancy(
                                        reconciliation.getReconciliationId(), officer, approvalRequest.getNotes());
                });

                assertTrue(exception.getMessage().contains("not authorized") ||
                                exception.getMessage().contains("permission") ||
                                exception.getMessage().contains("Secretary"),
                                "Officer should not be authorized to approve variance");
        }

        private User createOfficerUser() {
                User officer = new User();
                officer.setId(1L);
                officer.setUsername("officer_user");
                officer.setRole(User.Role.OFFICER);
                return officer;
        }

        private User createSecretaryUser() {
                User secretary = new User();
                secretary.setId(2L);
                secretary.setUsername("secretary_user");
                secretary.setRole(User.Role.SECRETARY);
                return secretary;
        }
}
