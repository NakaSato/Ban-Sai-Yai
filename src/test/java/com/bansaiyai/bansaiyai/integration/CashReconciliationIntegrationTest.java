package com.bansaiyai.bansaiyai.integration;

import com.bansaiyai.bansaiyai.dto.CashReconciliationRequest;
import com.bansaiyai.bansaiyai.dto.CashReconciliationResponse;
import com.bansaiyai.bansaiyai.dto.DiscrepancyApprovalRequest;
import com.bansaiyai.bansaiyai.entity.CashReconciliation;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.entity.enums.CashReconciliationStatus;
import com.bansaiyai.bansaiyai.repository.CashReconciliationRepository;
import com.bansaiyai.bansaiyai.service.CashReconciliationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for cash reconciliation flow
 * Validates: Requirements 8.2, 8.3, 8.4, 9.3, 9.4
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class CashReconciliationIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private CashReconciliationService cashReconciliationService;

  @Autowired
  private CashReconciliationRepository cashReconciliationRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(springSecurity(new WithSecurityContextTestExecutionListener()))
        .build();
  }

  /**
   * Test Officer creates reconciliation with variance
   */
  @Test
  void testOfficerCreatesReconciliationWithVariance() throws Exception {
    // Arrange: Officer creates reconciliation with variance
    CashReconciliationRequest request = new CashReconciliationRequest();
    request.setPhysicalCashCount(BigDecimal.valueOf(5000.00));
    request.setNotes("Test reconciliation with variance");
    request.setReconciliationDate(LocalDate.now());

    // Act: Officer creates reconciliation
    mockMvc.perform(post("/api/cash-reconciliation")
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasVariance").value(true))
        .andExpect(jsonPath("$.status").value("PENDING"));

    // Assert: Verify reconciliation was created with variance
    List<CashReconciliation> pendingReconciliations = cashReconciliationService.getPendingReconciliations();
    assertEquals(1, pendingReconciliations.size());

    CashReconciliation created = pendingReconciliations.get(0);
    assertTrue(created.getHasVariance());
    assertEquals(CashReconciliationStatus.PENDING, created.getStatus());
  }

  /**
   * Test Officer cannot close day with variance
   */
  @Test
  void testOfficerCannotCloseDayWithVariance() throws Exception {
    // Arrange: Create reconciliation with variance
    CashReconciliationRequest request = new CashReconciliationRequest();
    request.setPhysicalCashCount(BigDecimal.valueOf(4500.00));
    request.setNotes("Test reconciliation - should not allow day close");
    request.setReconciliationDate(LocalDate.now());

    // Act: Officer creates reconciliation
    mockMvc.perform(post("/api/cash-reconciliation")
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Act & Assert: Officer tries to close day - should fail
    mockMvc.perform(get("/api/cash-reconciliation/can-close-day")
        .with(user(createOfficerUser())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Day cannot be closed with pending reconciliations"));
  }

  /**
   * Test Secretary approves discrepancy
   */
  @Test
  void testSecretaryApprovesDiscrepancy() throws Exception {
    // Arrange: Officer creates reconciliation with variance
    CashReconciliationRequest request = new CashReconciliationRequest();
    request.setPhysicalCashCount(BigDecimal.valueOf(5000.00));
    request.setNotes("Test reconciliation for approval");
    request.setReconciliationDate(LocalDate.now());

    // Officer creates reconciliation
    mockMvc.perform(post("/api/cash-reconciliation")
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Get the created reconciliation ID
    List<CashReconciliation> pendingReconciliations = cashReconciliationService.getPendingReconciliations();
    Long reconciliationId = pendingReconciliations.get(0).getId();

    // Act: Secretary approves the discrepancy
    DiscrepancyApprovalRequest approvalRequest = new DiscrepancyApprovalRequest();
    approvalRequest.setApprovalNotes("Approved after review - variance acceptable");
    approvalRequest.setAccountingEntry("Cash variance adjustment");

    mockMvc.perform(post("/api/cash-reconciliation/{id}/approve", reconciliationId)
        .with(user(createSecretaryUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(approvalRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.secretaryNotes").value("Approved after review - variance acceptable"));

    // Assert: Verify reconciliation was approved
    CashReconciliation approved = cashReconciliationRepository.findById(reconciliationId).orElse(null);
    assertNotNull(approved);
    assertEquals(CashReconciliationStatus.APPROVED, approved.getStatus());
    assertEquals("Approved after review - variance acceptable", approved.getSecretaryNotes());
  }

  /**
   * Test day can close after approval
   */
  @Test
  void testDayCanCloseAfterApproval() throws Exception {
    // Arrange: Create and approve a reconciliation
    CashReconciliationRequest request = new CashReconciliationRequest();
    request.setPhysicalCashCount(BigDecimal.valueOf(5000.00));
    request.setNotes("Test reconciliation - will be approved");
    request.setReconciliationDate(LocalDate.now());

    // Officer creates reconciliation
    mockMvc.perform(post("/api/cash-reconciliation")
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    List<CashReconciliation> pendingReconciliations = cashReconciliationService.getPendingReconciliations();
    Long reconciliationId = pendingReconciliations.get(0).getId();

    // Secretary approves
    DiscrepancyApprovalRequest approvalRequest = new DiscrepancyApprovalRequest();
    approvalRequest.setApprovalNotes("Approved - variance resolved");
    approvalRequest.setAccountingEntry("Variance adjustment entry");

    mockMvc.perform(post("/api/cash-reconciliation/{id}/approve", reconciliationId)
        .with(user(createSecretaryUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(approvalRequest)))
        .andExpect(status().isOk());

    // Act: Officer can now close day
    mockMvc.perform(get("/api/cash-reconciliation/can-close-day")
        .with(user(createOfficerUser())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canCloseDay").value(true));
  }

  /**
   * Test Secretary can view pending reconciliations
   */
  @Test
  void testSecretaryCanViewPendingReconciliations() throws Exception {
    // Arrange: Create multiple reconciliations with variance
    for (int i = 0; i < 3; i++) {
      CashReconciliationRequest request = new CashReconciliationRequest();
      request.setPhysicalCashCount(BigDecimal.valueOf(5000.00 + (i * 100)));
      request.setNotes("Test reconciliation " + i);
      request.setReconciliationDate(LocalDate.now());

      mockMvc.perform(post("/api/cash-reconciliation")
          .with(user(createOfficerUser()))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    // Act: Secretary views pending reconciliations
    mockMvc.perform(get("/api/cash-reconciliation/pending")
        .with(user(createSecretaryUser())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[1].status").value("PENDING"))
        .andExpect(jsonPath("$[2].status").value("PENDING"));
  }

  /**
   * Test Officer cannot access approval endpoints
   */
  @Test
  void testOfficerCannotAccessApprovalEndpoints() throws Exception {
    // Arrange: Create a reconciliation
    CashReconciliationRequest request = new CashReconciliationRequest();
    request.setPhysicalCashCount(BigDecimal.valueOf(5000.00));
    request.setNotes("Test reconciliation");
    request.setReconciliationDate(LocalDate.now());

    mockMvc.perform(post("/api/cash-reconciliation")
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    List<CashReconciliation> pendingReconciliations = cashReconciliationService.getPendingReconciliations();
    Long reconciliationId = pendingReconciliations.get(0).getId();

    // Act & Assert: Officer cannot access approval endpoints
    mockMvc.perform(get("/api/cash-reconciliation/pending")
        .with(user(createOfficerUser())))
        .andExpect(status().isForbidden());

    DiscrepancyApprovalRequest approvalRequest = new DiscrepancyApprovalRequest();
    approvalRequest.setApprovalNotes("Officer attempting approval");

    mockMvc.perform(post("/api/cash-reconciliation/{id}/approve", reconciliationId)
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(approvalRequest)))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/cash-reconciliation/{id}/reject", reconciliationId)
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(approvalRequest)))
        .andExpect(status().isForbidden());
  }

  /**
   * Test Secretary can reject discrepancy
   */
  @Test
  void testSecretaryRejectsDiscrepancy() throws Exception {
    // Arrange: Officer creates reconciliation with significant variance
    CashReconciliationRequest request = new CashReconciliationRequest();
    request.setPhysicalCashCount(BigDecimal.valueOf(3000.00));
    request.setNotes("Test reconciliation - large variance");
    request.setReconciliationDate(LocalDate.now());

    // Officer creates reconciliation
    mockMvc.perform(post("/api/cash-reconciliation")
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    List<CashReconciliation> pendingReconciliations = cashReconciliationService.getPendingReconciliations();
    Long reconciliationId = pendingReconciliations.get(0).getId();

    // Act: Secretary rejects the discrepancy
    DiscrepancyApprovalRequest rejectionRequest = new DiscrepancyApprovalRequest();
    rejectionRequest.setRejectionReason("Significant variance requires investigation");

    mockMvc.perform(post("/api/cash-reconciliation/{id}/reject", reconciliationId)
        .with(user(createSecretaryUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(rejectionRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.secretaryNotes").value("Significant variance requires investigation"));

    // Assert: Verify reconciliation was rejected
    CashReconciliation rejected = cashReconciliationRepository.findById(reconciliationId).orElse(null);
    assertNotNull(rejected);
    assertEquals(CashReconciliationStatus.REJECTED, rejected.getStatus());
    assertEquals("Significant variance requires investigation", rejected.getSecretaryNotes());
  }

  /**
   * Test zero variance reconciliation doesn't require approval
   */
  @Test
  void testZeroVarianceReconciliationDoesNotRequireApproval() throws Exception {
    // Arrange: Create reconciliation with exact match (no variance)
    CashReconciliationRequest request = new CashReconciliationRequest();
    request.setPhysicalCashCount(BigDecimal.valueOf(5000.00));
    request.setNotes("Test reconciliation - no variance");
    request.setReconciliationDate(LocalDate.now());

    // Act: Officer creates reconciliation
    mockMvc.perform(post("/api/cash-reconciliation")
        .with(user(createOfficerUser()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpected(status().isOk())
        .andExpect(jsonPath("$.hasVariance").value(false))
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    // Assert: Should not appear in pending reconciliations
    mockMvc.perform(get("/api/cash-reconciliation/pending")
        .with(user(createSecretaryUser())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  private org.springframework.security.core.userdetails.User createOfficerUser() {
    return org.springframework.security.core.userdetails.User.builder()
        .username("officer_user")
        .password("password")
        .authorities("ROLE_OFFICER")
        .build();
  }

  private org.springframework.security.core.userdetails.User createSecretaryUser() {
    return org.springframework.security.core.userdetails.User.builder()
        .username("secretary_user")
        .password("password")
        .authorities("ROLE_SECRETARY")
        .build();
  }
}
