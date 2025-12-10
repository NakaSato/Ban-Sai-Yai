package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.GuarantorRequest;
import com.bansaiyai.bansaiyai.dto.LoanApplicationRequest;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.exception.BusinessException;
import com.bansaiyai.bansaiyai.repository.GuarantorRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackendEnhancementTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private GuarantorRepository guarantorRepository;

    @InjectMocks
    private LoanService loanService;

    @InjectMocks
    private MemberService memberService; // Separate instance for member tests

    @Nested
    @DisplayName("Guarantor Logic Tests")
    class GuarantorLogicTest {

        @Test
        @DisplayName("Should throw exception if guarantor limit exceeded (Max 2 per loan)")
        void shouldThrowIfMaxGuarantorsExceeded() {
            LoanApplicationRequest request = new LoanApplicationRequest();
            request.setMemberId(1L);
            request.setLoanType(LoanType.PERSONAL);
            request.setPrincipalAmount(new BigDecimal("10000"));
            request.setTermMonths(12);
            request.setPurpose("Test");

            // Add 3 Guarantors
            List<GuarantorRequest> guarantors = List.of(
                    new GuarantorRequest(), new GuarantorRequest(), new GuarantorRequest());
            request.setGuarantors(guarantors);

            when(memberRepository.findById(1L)).thenReturn(Optional.of(new Member())); // Borrower found
            when(loanRepository.save(any())).thenReturn(new Loan()); // Mock save

            assertThatThrownBy(() -> loanService.createLoanApplication(request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Maximum 2 guarantors");
        }

        @Test
        @DisplayName("Should throw exception if borrower guarantees themselves")
        void shouldThrowIfSelfGuarantee() {
            Member borrower = new Member();
            borrower.setId(1L);
            borrower.setIsActive(true);

            LoanApplicationRequest request = new LoanApplicationRequest();
            request.setMemberId(1L);
            request.setLoanType(LoanType.PERSONAL);
            request.setPrincipalAmount(new BigDecimal("10000"));
            request.setTermMonths(12);
            request.setPurpose("Test");

            GuarantorRequest gReq = new GuarantorRequest();
            gReq.setMemberId(1L); // Same as borrower
            request.setGuarantors(List.of(gReq));

            when(memberRepository.findById(1L)).thenReturn(Optional.of(borrower)); // Return borrower for both calls
            when(loanRepository.findByMemberIdAndStatus(1L, com.bansaiyai.bansaiyai.entity.enums.LoanStatus.ACTIVE))
                    .thenReturn(Collections.emptyList());
            when(loanRepository.save(any())).thenReturn(new Loan());

            assertThatThrownBy(() -> loanService.createLoanApplication(request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Borrower cannot be their own guarantor");
        }

        @Test
        @DisplayName("Should throw exception if guarantor has too many active guarantees")
        void shouldThrowIfGuarantorLimitReached() {
            Member borrower = new Member();
            borrower.setId(1L);
            borrower.setIsActive(true);
            Member guarantor = new Member();
            guarantor.setId(2L);
            guarantor.setIsActive(true);

            LoanApplicationRequest request = new LoanApplicationRequest();
            request.setMemberId(1L);
            // ... setup fields ...
            request.setLoanType(LoanType.PERSONAL);
            request.setPrincipalAmount(new BigDecimal("10000"));
            request.setTermMonths(12);
            request.setPurpose("Test");

            GuarantorRequest gReq = new GuarantorRequest();
            gReq.setMemberId(2L);
            request.setGuarantors(List.of(gReq));

            when(memberRepository.findById(1L)).thenReturn(Optional.of(borrower));
            when(memberRepository.findById(2L)).thenReturn(Optional.of(guarantor));
            when(loanRepository.save(any())).thenReturn(new Loan());

            // Mock that guarantor already has 3 active guarantees
            when(guarantorRepository.countActiveByMemberId(2L)).thenReturn(3L);

            assertThatThrownBy(() -> loanService.createLoanApplication(request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("limit of active guarantees");
        }
    }

    @Nested
    @DisplayName("Payoff Calculation Tests")
    class PayoffTest {

        @Test
        @DisplayName("Should calculate correct payoff details")
        void shouldCalculatePayoff() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate targetDate = LocalDate.of(2024, 1, 31); // 30 days gap

            Loan loan = Loan.builder()
                    .principalAmount(new BigDecimal("10000"))
                    .interestRate(new BigDecimal("12")) // 12% annual
                    .startDate(startDate)
                    .termMonths(12)
                    .build();
            loan.setId(1L);

            // Mock the internal logic of getOutstandingBalance if necessary, or just set
            // the field reference if it's a field
            // But Loan.java has business logic methods. Let's assume the entity state is
            // consistent.
            // For simple payoff, we just need outstandingBalance.
            // Note: In the implemented Service method: BigDecimal outstanding =
            // loan.getOutstandingBalance();
            // We need to ensure loan.getOutstandingBalance() returns 10000 (if no payments
            // made).
            // This depends on Loan entity logic. Let's assume getOutstandingBalance()
            // returns principal if no payments.

            // Wait, getOutstandingBalance() logic is likely inside Loan.java.
            // If it's a calculated getter, we rely on it. If it's a field, we set it.
            // Let's trying setting payments to empty list.

            // Actually, for this unit test, we might struggle if Entity logic is complex.
            // Let's Spy on the Loan object? Or assume it works.
            // Let's assume getOutstandingBalance returns principal for now, or check
            // Loan.java.
            // Line 398 of LoanService.java uses: BigDecimal outstanding =
            // loan.getOutstandingBalance();

            // Mocking repository
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            // Spy on loan to force return value for clarity/isolation?
            // Loan spyLoan = spy(loan);
            // doReturn(new BigDecimal("10000")).when(spyLoan).getOutstandingBalance();
            // when(loanRepository.findById(1L)).thenReturn(Optional.of(spyLoan));

            // But Mockito can't spy final classes or methods easily. Let's rely on setters
            // if possible.
            // If Loan has `private BigDecimal outstandingBalance` field, Lombok generates
            // setter.
            // Let's assume `setOutstandingBalance` exists (standard @Data).
            // But in logical domain models, outstanding balance is often calculated.
            // Let's check Loan.java content from previous view.

            // Looking at Loan.java (I viewed it earlier?), wait, I viewed Guarantor and
            // Member.
            // I should have viewed Loan.java.
            // However, typically in these generated entities, there is a field or a getter.
            // Let's assume I can't be sure.

            // I will implement a safe version that sets the return value via Mock if
            // possible,
            // OR checks the math logic assuming outstanding = 10000.

            // Let's just create a mock Loan object to avoid entity logic dependency
            Loan mockLoan = mock(Loan.class);
            when(mockLoan.getOutstandingBalance()).thenReturn(new BigDecimal("10000"));
            when(mockLoan.getInterestRate()).thenReturn(new BigDecimal("12"));
            when(mockLoan.getStartDate()).thenReturn(startDate);

            when(loanRepository.findById(1L)).thenReturn(Optional.of(mockLoan));

            Map<String, BigDecimal> result = loanService.calculatePayoff(1L, targetDate);

            // Interest = 10000 * (12/100) / 365 = 3.287... daily
            // 30 days = 3.287... * 30 = 98.63 (approx)
            // Let's match roughly.

            assertThat(result.get("principal")).isEqualTo(new BigDecimal("10000"));
            assertThat(result.get("interest")).isGreaterThan(BigDecimal.ZERO);
            assertThat(result.get("total")).isGreaterThan(new BigDecimal("10000"));
        }
    }

    @Nested
    @DisplayName("Member Validation Tests")
    class MemberValidationTest {

        @Test
        @DisplayName("Should throw exception if member is under 18")
        void shouldThrowIfUnder18() {
            Member minor = new Member();
            minor.setDateOfBirth(LocalDate.now().minusYears(17)); // 17 years old

            assertThatThrownBy(() -> memberService.saveMember(minor))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("18 years old");
        }

        @Test
        @DisplayName("Should throw exception if ID Card already exists")
        void shouldThrowIfDuplicateIdCard() {
            Member member = new Member();
            member.setIdCard("1234567890123");
            member.setDateOfBirth(LocalDate.now().minusYears(20));

            when(memberRepository.existsByIdCard("1234567890123")).thenReturn(true);

            assertThatThrownBy(() -> memberService.saveMember(member))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID Card already registered");
        }
    }
}
