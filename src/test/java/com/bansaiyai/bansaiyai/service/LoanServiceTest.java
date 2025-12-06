package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.LoanApplicationRequest;
import com.bansaiyai.bansaiyai.dto.LoanResponse;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.exception.BusinessException;
import com.bansaiyai.bansaiyai.exception.ResourceNotFoundException;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoanService.
 * Tests loan application creation, approval, rejection, and disbursement
 * workflows.
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

  @Mock
  private LoanRepository loanRepository;

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private LoanService loanService;

  private Member activeMember;
  private Member inactiveMember;
  private LoanApplicationRequest validRequest;
  private Loan pendingLoan;
  private Loan approvedLoan;

  @BeforeEach
  void setUp() {
    // Setup active member
    activeMember = Member.builder()
        .memberId("MBR001")
        .name("Test Member")
        .email("test@example.com")
        .phone("0812345678")
        .idCard("1234567890123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .address("123 Test Street")
        .registrationDate(LocalDate.now().minusMonths(12))
        .isActive(true)
        .build();
    activeMember.setId(1L);

    // Setup inactive member
    inactiveMember = Member.builder()
        .memberId("MBR002")
        .name("Inactive Member")
        .isActive(false)
        .build();
    inactiveMember.setId(2L);

    // Setup valid loan request
    validRequest = LoanApplicationRequest.builder()
        .memberId(1L)
        .loanType(LoanType.PERSONAL)
        .principalAmount(new BigDecimal("50000.00"))
        .termMonths(12)
        .purpose("Personal expenses for home improvement")
        .build();

    // Setup pending loan
    pendingLoan = Loan.builder()
        .loanNumber("LN202401001")
        .member(activeMember)
        .loanType(LoanType.PERSONAL)
        .principalAmount(new BigDecimal("50000.00"))
        .interestRate(new BigDecimal("11.0"))
        .termMonths(12)
        .status(LoanStatus.PENDING)
        .build();
    pendingLoan.setId(1L);

    // Setup approved loan
    approvedLoan = Loan.builder()
        .loanNumber("LN202401002")
        .member(activeMember)
        .loanType(LoanType.BUSINESS)
        .principalAmount(new BigDecimal("100000.00"))
        .approvedAmount(new BigDecimal("100000.00"))
        .interestRate(new BigDecimal("12.0"))
        .termMonths(24)
        .status(LoanStatus.APPROVED)
        .build();
    approvedLoan.setId(2L);
  }

  @Nested
  @DisplayName("Create Loan Application Tests")
  class CreateLoanApplicationTests {

    @Test
    @DisplayName("Should create loan application for active member")
    void shouldCreateLoanApplicationForActiveMember() {
      // Arrange
      when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
      when(loanRepository.findByMemberIdAndStatus(1L, LoanStatus.ACTIVE))
          .thenReturn(Collections.emptyList());
      when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
        Loan saved = invocation.getArgument(0);
        saved.setId(1L);
        return saved;
      });

      // Act
      LoanResponse response = loanService.createLoanApplication(validRequest, "admin");

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getLoanNumber()).startsWith("LN");
      assertThat(response.getStatus()).isEqualTo(LoanStatus.PENDING);
      assertThat(response.getPrincipalAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));

      verify(loanRepository).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
      // Arrange
      when(memberRepository.findById(999L)).thenReturn(Optional.empty());

      LoanApplicationRequest request = LoanApplicationRequest.builder()
          .memberId(999L)
          .loanType(LoanType.PERSONAL)
          .principalAmount(new BigDecimal("50000.00"))
          .termMonths(12)
          .purpose("Test purpose")
          .build();

      // Act & Assert
      assertThatThrownBy(() -> loanService.createLoanApplication(request, "admin"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Member");

      verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for inactive member")
    void shouldThrowExceptionForInactiveMember() {
      // Arrange
      when(memberRepository.findById(2L)).thenReturn(Optional.of(inactiveMember));

      LoanApplicationRequest request = LoanApplicationRequest.builder()
          .memberId(2L)
          .loanType(LoanType.PERSONAL)
          .principalAmount(new BigDecimal("50000.00"))
          .termMonths(12)
          .purpose("Test purpose")
          .build();

      // Act & Assert
      assertThatThrownBy(() -> loanService.createLoanApplication(request, "admin"))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("not active");

      verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when member has active loan")
    void shouldThrowExceptionWhenMemberHasActiveLoan() {
      // Arrange
      Loan activeLoan = Loan.builder()
          .status(LoanStatus.ACTIVE)
          .build();

      when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
      when(loanRepository.findByMemberIdAndStatus(1L, LoanStatus.ACTIVE))
          .thenReturn(List.of(activeLoan));

      // Act & Assert
      assertThatThrownBy(() -> loanService.createLoanApplication(validRequest, "admin"))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("active loan");

      verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for invalid term months")
    void shouldThrowExceptionForInvalidTermMonths() {
      // Arrange
      when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
      when(loanRepository.findByMemberIdAndStatus(1L, LoanStatus.ACTIVE))
          .thenReturn(Collections.emptyList());

      LoanApplicationRequest request = LoanApplicationRequest.builder()
          .memberId(1L)
          .loanType(LoanType.PERSONAL)
          .principalAmount(new BigDecimal("50000.00"))
          .termMonths(150) // Exceeds MAX_TERM_MONTHS
          .purpose("Test purpose")
          .build();

      // Act & Assert
      assertThatThrownBy(() -> loanService.createLoanApplication(request, "admin"))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("term");

      verify(loanRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Loan Approval Tests")
  class LoanApprovalTests {

    @Test
    @DisplayName("Should throw exception when approving non-pending loan")
    void shouldThrowExceptionWhenApprovingNonPendingLoan() {
      // Arrange
      when(loanRepository.findById(2L)).thenReturn(Optional.of(approvedLoan));

      // Act & Assert
      assertThatThrownBy(() -> loanService.approveLoan(2L, null, "approver"))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("pending");
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void shouldThrowExceptionWhenLoanNotFound() {
      // Arrange
      when(loanRepository.findById(999L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> loanService.approveLoan(999L, null, "approver"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Loan");
    }
  }

  @Nested
  @DisplayName("Loan Disbursement Tests")
  class LoanDisbursementTests {

    @Test
    @DisplayName("Should throw exception when disbursing non-approved loan")
    void shouldThrowExceptionWhenDisbursingNonApprovedLoan() {
      // Arrange
      when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));

      // Act & Assert
      assertThatThrownBy(() -> loanService.disburseLoan(1L, "disburser"))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("approved");
    }
  }

  @Nested
  @DisplayName("Loan Rejection Tests")
  class LoanRejectionTests {

    @Test
    @DisplayName("Should throw exception when rejecting non-pending loan")
    void shouldThrowExceptionWhenRejectingNonPendingLoan() {
      // Arrange
      when(loanRepository.findById(2L)).thenReturn(Optional.of(approvedLoan));

      // Act & Assert
      assertThatThrownBy(() -> loanService.rejectLoan(2L, "Test reason", "rejector"))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("pending");
    }
  }

  @Nested
  @DisplayName("Loan Retrieval Tests")
  class LoanRetrievalTests {

    @Test
    @DisplayName("Should get loan by ID")
    void shouldGetLoanById() {
      // Arrange
      when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));

      // Act
      LoanResponse response = loanService.getLoanById(1L);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getLoanNumber()).isEqualTo("LN202401001");
    }

    @Test
    @DisplayName("Should throw exception when loan not found by ID")
    void shouldThrowExceptionWhenLoanNotFoundById() {
      // Arrange
      when(loanRepository.findById(999L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> loanService.getLoanById(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Loan");
    }

    @Test
    @DisplayName("Should get loan by loan number")
    void shouldGetLoanByLoanNumber() {
      // Arrange
      when(loanRepository.findByLoanNumber("LN202401001")).thenReturn(Optional.of(pendingLoan));

      // Act
      LoanResponse response = loanService.getLoanByNumber("LN202401001");

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(1L);
    }
  }

  @Nested
  @DisplayName("Loan Update and Delete Tests")
  class LoanUpdateDeleteTests {

    @Test
    @DisplayName("Should throw exception when updating non-pending loan")
    void shouldThrowExceptionWhenUpdatingNonPendingLoan() {
      // Arrange
      when(loanRepository.findById(2L)).thenReturn(Optional.of(approvedLoan));

      // Act & Assert
      assertThatThrownBy(() -> loanService.updateLoan(2L, validRequest, "updater"))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("pending");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-pending loan")
    void shouldThrowExceptionWhenDeletingNonPendingLoan() {
      // Arrange
      when(loanRepository.findById(2L)).thenReturn(Optional.of(approvedLoan));

      // Act & Assert
      assertThatThrownBy(() -> loanService.deleteLoan(2L))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("pending");

      verify(loanRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete pending loan successfully")
    void shouldDeletePendingLoanSuccessfully() {
      // Arrange
      when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));

      // Act
      loanService.deleteLoan(1L);

      // Assert
      verify(loanRepository).delete(pendingLoan);
    }
  }

  @Nested
  @DisplayName("Business Logic Tests")
  class BusinessLogicTests {

    @Test
    @DisplayName("Should calculate correct interest rate for different loan types")
    void shouldCalculateCorrectInterestRateForDifferentLoanTypes() {
      // Test that the service calculates correct interest rates
      // This is tested implicitly through loan creation

      when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
      when(loanRepository.findByMemberIdAndStatus(1L, LoanStatus.ACTIVE))
          .thenReturn(Collections.emptyList());
      when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
        Loan saved = invocation.getArgument(0);
        saved.setId(1L);
        return saved;
      });

      // Test BUSINESS loan type
      LoanApplicationRequest businessLoan = LoanApplicationRequest.builder()
          .memberId(1L)
          .loanType(LoanType.BUSINESS)
          .principalAmount(new BigDecimal("100000.00"))
          .termMonths(24)
          .purpose("Business expansion")
          .build();

      LoanResponse response = loanService.createLoanApplication(businessLoan, "admin");

      assertThat(response.getInterestRate()).isEqualByComparingTo(new BigDecimal("12.0"));
    }
  }
}
