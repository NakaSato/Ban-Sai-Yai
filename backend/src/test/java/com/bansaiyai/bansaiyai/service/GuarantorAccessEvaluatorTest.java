package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.*;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.repository.GuarantorRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GuarantorAccessEvaluator.
 * Tests specific examples and edge cases for guarantor-based access control.
 * 
 * Requirements: 7.1, 7.5
 */
@ExtendWith(MockitoExtension.class)
class GuarantorAccessEvaluatorTest {

    @Mock
    private GuarantorRepository guarantorRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private GuarantorAccessEvaluator guarantorAccessEvaluator;

    private User borrowerUser;
    private User guarantorUser;
    private User nonRelatedUser;
    private Member borrowerMember;
    private Member guarantorMember;
    private Member nonRelatedMember;
    private Loan testLoan;
    private Guarantor activeGuarantor;

    @BeforeEach
    void setUp() {
        // Setup borrower
        borrowerMember = new Member();
        borrowerMember.setId(1L);
        borrowerMember.setMemberId("MEM-001");
        borrowerMember.setName("Borrower Member");

        borrowerUser = new User();
        borrowerUser.setId(1L);
        borrowerUser.setUsername("borrower");
        borrowerUser.setMember(borrowerMember);

        // Setup guarantor
        guarantorMember = new Member();
        guarantorMember.setId(2L);
        guarantorMember.setMemberId("MEM-002");
        guarantorMember.setName("Guarantor Member");

        guarantorUser = new User();
        guarantorUser.setId(2L);
        guarantorUser.setUsername("guarantor");
        guarantorUser.setMember(guarantorMember);

        // Setup non-related member
        nonRelatedMember = new Member();
        nonRelatedMember.setId(3L);
        nonRelatedMember.setMemberId("MEM-003");
        nonRelatedMember.setName("Non-Related Member");

        nonRelatedUser = new User();
        nonRelatedUser.setId(3L);
        nonRelatedUser.setUsername("nonrelated");
        nonRelatedUser.setMember(nonRelatedMember);

        // Setup loan
        testLoan = new Loan();
        testLoan.setId(100L);
        testLoan.setLoanNumber("LN-001");
        testLoan.setMember(borrowerMember);
        testLoan.setLoanType(LoanType.EMERGENCY);
        testLoan.setPrincipalAmount(new BigDecimal("50000.00"));
        testLoan.setStatus(LoanStatus.ACTIVE);

        // Setup active guarantor
        activeGuarantor = new Guarantor();
        activeGuarantor.setId(1L);
        activeGuarantor.setLoan(testLoan);
        activeGuarantor.setMember(guarantorMember);
        activeGuarantor.setIsActive(true);
        activeGuarantor.setGuaranteedAmount(new BigDecimal("10000.00"));
    }

    /**
     * Test canViewLoan for borrower
     * Requirements: 7.1
     */
    @Test
    void testCanViewLoan_Borrower_ShouldAllowAccess() {
        // Arrange
        when(loanRepository.findById(100L)).thenReturn(Optional.of(testLoan));

        // Act
        boolean canView = guarantorAccessEvaluator.canViewLoan(borrowerUser, 100L);

        // Assert
        assertTrue(canView, "Borrower should have access to their own loan");
        verify(loanRepository).findById(100L);
        // Should not check guarantor status for borrower
        verify(guarantorRepository, never()).existsActiveByLoanIdAndMemberId(anyLong(), anyLong());
    }

    /**
     * Test canViewLoan for guarantor
     * Requirements: 7.1
     */
    @Test
    void testCanViewLoan_Guarantor_ShouldAllowAccess() {
        // Arrange
        when(loanRepository.findById(100L)).thenReturn(Optional.of(testLoan));
        when(guarantorRepository.existsActiveByLoanIdAndMemberId(100L, 2L)).thenReturn(true);

        // Act
        boolean canView = guarantorAccessEvaluator.canViewLoan(guarantorUser, 100L);

        // Assert
        assertTrue(canView, "Guarantor should have access to guaranteed loan");
        verify(loanRepository).findById(100L);
        verify(guarantorRepository).existsActiveByLoanIdAndMemberId(100L, 2L);
    }

    /**
     * Test canViewLoan denial for non-related member
     * Requirements: 7.5
     */
    @Test
    void testCanViewLoan_NonRelatedMember_ShouldDenyAccess() {
        // Arrange
        when(loanRepository.findById(100L)).thenReturn(Optional.of(testLoan));
        when(guarantorRepository.existsActiveByLoanIdAndMemberId(100L, 3L)).thenReturn(false);

        // Act
        boolean canView = guarantorAccessEvaluator.canViewLoan(nonRelatedUser, 100L);

        // Assert
        assertFalse(canView, "Non-related member should not have access to loan");
        verify(loanRepository).findById(100L);
        verify(guarantorRepository).existsActiveByLoanIdAndMemberId(100L, 3L);
    }

    /**
     * Test canViewLoan with null user
     */
    @Test
    void testCanViewLoan_NullUser_ShouldDenyAccess() {
        // Act
        boolean canView = guarantorAccessEvaluator.canViewLoan(null, 100L);

        // Assert
        assertFalse(canView, "Null user should not have access");
        verify(loanRepository, never()).findById(anyLong());
    }

    /**
     * Test canViewLoan with user without member
     */
    @Test
    void testCanViewLoan_UserWithoutMember_ShouldDenyAccess() {
        // Arrange
        User userWithoutMember = new User();
        userWithoutMember.setId(4L);
        userWithoutMember.setUsername("nomember");
        userWithoutMember.setMember(null);

        // Act
        boolean canView = guarantorAccessEvaluator.canViewLoan(userWithoutMember, 100L);

        // Assert
        assertFalse(canView, "User without member should not have access");
        verify(loanRepository, never()).findById(anyLong());
    }

    /**
     * Test canViewLoan with null loan ID
     */
    @Test
    void testCanViewLoan_NullLoanId_ShouldDenyAccess() {
        // Act
        boolean canView = guarantorAccessEvaluator.canViewLoan(borrowerUser, null);

        // Assert
        assertFalse(canView, "Null loan ID should deny access");
        verify(loanRepository, never()).findById(anyLong());
    }

    /**
     * Test canViewLoan with non-existent loan
     */
    @Test
    void testCanViewLoan_NonExistentLoan_ShouldDenyAccess() {
        // Arrange
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean canView = guarantorAccessEvaluator.canViewLoan(borrowerUser, 999L);

        // Assert
        assertFalse(canView, "Non-existent loan should deny access");
        verify(loanRepository).findById(999L);
    }

    /**
     * Test canViewLoan for inactive guarantor
     */
    @Test
    void testCanViewLoan_InactiveGuarantor_ShouldDenyAccess() {
        // Arrange
        when(loanRepository.findById(100L)).thenReturn(Optional.of(testLoan));
        when(guarantorRepository.existsActiveByLoanIdAndMemberId(100L, 2L)).thenReturn(false);

        // Act
        boolean canView = guarantorAccessEvaluator.canViewLoan(guarantorUser, 100L);

        // Assert
        assertFalse(canView, "Inactive guarantor should not have access");
        verify(loanRepository).findById(100L);
        verify(guarantorRepository).existsActiveByLoanIdAndMemberId(100L, 2L);
    }

    /**
     * Test getGuaranteedLoans
     * Requirements: 7.2
     */
    @Test
    void testGetGuaranteedLoans_ShouldReturnActiveGuaranteedLoans() {
        // Arrange
        Loan loan1 = new Loan();
        loan1.setId(101L);
        loan1.setStatus(LoanStatus.ACTIVE);

        Loan loan2 = new Loan();
        loan2.setId(102L);
        loan2.setStatus(LoanStatus.PENDING);

        Guarantor guarantor1 = new Guarantor();
        guarantor1.setLoan(loan1);
        guarantor1.setIsActive(true);

        Guarantor guarantor2 = new Guarantor();
        guarantor2.setLoan(loan2);
        guarantor2.setIsActive(true);

        when(guarantorRepository.findActiveByMemberId(2L))
                .thenReturn(Arrays.asList(guarantor1, guarantor2));

        // Act
        List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(2L);

        // Assert
        assertEquals(2, guaranteedLoans.size(), "Should return 2 guaranteed loans");
        assertTrue(guaranteedLoans.contains(loan1), "Should contain loan1");
        assertTrue(guaranteedLoans.contains(loan2), "Should contain loan2");
        verify(guarantorRepository).findActiveByMemberId(2L);
    }

    /**
     * Test getGuaranteedLoans excludes completed loans
     */
    @Test
    void testGetGuaranteedLoans_ShouldExcludeCompletedLoans() {
        // Arrange
        Loan activeLoan = new Loan();
        activeLoan.setId(101L);
        activeLoan.setStatus(LoanStatus.ACTIVE);

        Loan completedLoan = new Loan();
        completedLoan.setId(102L);
        completedLoan.setStatus(LoanStatus.COMPLETED);

        Guarantor guarantor1 = new Guarantor();
        guarantor1.setLoan(activeLoan);
        guarantor1.setIsActive(true);

        Guarantor guarantor2 = new Guarantor();
        guarantor2.setLoan(completedLoan);
        guarantor2.setIsActive(true);

        when(guarantorRepository.findActiveByMemberId(2L))
                .thenReturn(Arrays.asList(guarantor1, guarantor2));

        // Act
        List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(2L);

        // Assert
        assertEquals(1, guaranteedLoans.size(), "Should return only 1 active loan");
        assertTrue(guaranteedLoans.contains(activeLoan), "Should contain active loan");
        assertFalse(guaranteedLoans.contains(completedLoan), "Should not contain completed loan");
    }

    /**
     * Test getGuaranteedLoans excludes written-off loans
     */
    @Test
    void testGetGuaranteedLoans_ShouldExcludeWrittenOffLoans() {
        // Arrange
        Loan activeLoan = new Loan();
        activeLoan.setId(101L);
        activeLoan.setStatus(LoanStatus.ACTIVE);

        Loan writtenOffLoan = new Loan();
        writtenOffLoan.setId(102L);
        writtenOffLoan.setStatus(LoanStatus.WRITTEN_OFF);

        Guarantor guarantor1 = new Guarantor();
        guarantor1.setLoan(activeLoan);
        guarantor1.setIsActive(true);

        Guarantor guarantor2 = new Guarantor();
        guarantor2.setLoan(writtenOffLoan);
        guarantor2.setIsActive(true);

        when(guarantorRepository.findActiveByMemberId(2L))
                .thenReturn(Arrays.asList(guarantor1, guarantor2));

        // Act
        List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(2L);

        // Assert
        assertEquals(1, guaranteedLoans.size(), "Should return only 1 active loan");
        assertTrue(guaranteedLoans.contains(activeLoan), "Should contain active loan");
        assertFalse(guaranteedLoans.contains(writtenOffLoan), "Should not contain written-off loan");
    }

    /**
     * Test getGuaranteedLoans with null member ID
     */
    @Test
    void testGetGuaranteedLoans_NullMemberId_ShouldReturnEmptyList() {
        // Act
        List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(null);

        // Assert
        assertTrue(guaranteedLoans.isEmpty(), "Should return empty list for null member ID");
        verify(guarantorRepository, never()).findActiveByMemberId(anyLong());
    }

    /**
     * Test getGuaranteedLoans with no guarantees
     */
    @Test
    void testGetGuaranteedLoans_NoGuarantees_ShouldReturnEmptyList() {
        // Arrange
        when(guarantorRepository.findActiveByMemberId(2L)).thenReturn(List.of());

        // Act
        List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(2L);

        // Assert
        assertTrue(guaranteedLoans.isEmpty(), "Should return empty list when no guarantees exist");
        verify(guarantorRepository).findActiveByMemberId(2L);
    }

    /**
     * Test isActiveGuarantor returns true for active guarantor
     */
    @Test
    void testIsActiveGuarantor_ActiveGuarantor_ShouldReturnTrue() {
        // Arrange
        when(guarantorRepository.existsActiveByLoanIdAndMemberId(100L, 2L)).thenReturn(true);

        // Act
        boolean isActive = guarantorAccessEvaluator.isActiveGuarantor(2L, 100L);

        // Assert
        assertTrue(isActive, "Should return true for active guarantor");
        verify(guarantorRepository).existsActiveByLoanIdAndMemberId(100L, 2L);
    }

    /**
     * Test isActiveGuarantor returns false for non-guarantor
     */
    @Test
    void testIsActiveGuarantor_NonGuarantor_ShouldReturnFalse() {
        // Arrange
        when(guarantorRepository.existsActiveByLoanIdAndMemberId(100L, 3L)).thenReturn(false);

        // Act
        boolean isActive = guarantorAccessEvaluator.isActiveGuarantor(3L, 100L);

        // Assert
        assertFalse(isActive, "Should return false for non-guarantor");
        verify(guarantorRepository).existsActiveByLoanIdAndMemberId(100L, 3L);
    }

    /**
     * Test isActiveGuarantor with null member ID
     */
    @Test
    void testIsActiveGuarantor_NullMemberId_ShouldReturnFalse() {
        // Act
        boolean isActive = guarantorAccessEvaluator.isActiveGuarantor(null, 100L);

        // Assert
        assertFalse(isActive, "Should return false for null member ID");
        verify(guarantorRepository, never()).existsActiveByLoanIdAndMemberId(anyLong(), anyLong());
    }

    /**
     * Test isActiveGuarantor with null loan ID
     */
    @Test
    void testIsActiveGuarantor_NullLoanId_ShouldReturnFalse() {
        // Act
        boolean isActive = guarantorAccessEvaluator.isActiveGuarantor(2L, null);

        // Assert
        assertFalse(isActive, "Should return false for null loan ID");
        verify(guarantorRepository, never()).existsActiveByLoanIdAndMemberId(anyLong(), anyLong());
    }
}
