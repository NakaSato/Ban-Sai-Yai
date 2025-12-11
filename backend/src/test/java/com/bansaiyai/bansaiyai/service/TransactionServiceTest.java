package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.CompositePaymentRequest;
import com.bansaiyai.bansaiyai.dto.CompositeTransactionResponse;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.SavingAccount;
import com.bansaiyai.bansaiyai.entity.SavingTransaction;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import com.bansaiyai.bansaiyai.repository.SavingRepository;
import com.bansaiyai.bansaiyai.repository.SavingTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SavingRepository savingRepository;
    @Mock
    private SavingTransactionRepository savingTransactionRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private DashboardService dashboardService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    private User officer;
    private Member member;
    private Loan loan;

    @BeforeEach
    void setUp() {
        officer = new User();
        officer.setId(1L);
        officer.setUsername("officer");

        member = new Member();
        member.setId(100L);
        member.setName("Test Member");

        loan = new Loan();
        loan.setId(500L);
        loan.setMember(member);
        loan.setPrincipalAmount(new BigDecimal("10000"));
        loan.setOutstandingBalance(new BigDecimal("5000"));
        loan.setInterestRate(new BigDecimal("10.0"));
        loan.setMaturityDate(LocalDate.now().plusMonths(6));
    }

    @Test
    void processCompositePayment_ShouldHandleShareAndLoan() {
        // Setup Inputs
        CompositePaymentRequest request = new CompositePaymentRequest();
        request.setMemberId(member.getId());
        request.setShareAmount(new BigDecimal("500"));
        request.setLoanPaymentAmount(new BigDecimal("1000"));
        request.setLoanId(loan.getId());

        // Mocks for Share Deposit
        when(dashboardService.getCurrentFiscalPeriod()).thenReturn(createOpenPeriod());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setBalance(BigDecimal.ZERO);
        when(savingRepository.findByMemberIdAndIsActive(member.getId(), true))
                .thenReturn(List.of(savingAccount));

        when(savingTransactionRepository.save(any(SavingTransaction.class))).thenAnswer(i -> {
            SavingTransaction st = i.getArgument(0);
            st.setId(111L); // Mock ID
            return st;
        });

        // Mocks for Loan Payment
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(222L);
            return p;
        });

        // Execute
        CompositeTransactionResponse response = transactionService.processCompositePayment(request, officer);

        // Assert
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(111L, response.getShareTransactionId());
        assertEquals(222L, response.getLoanTransactionId());

        // Verify Audit
        verify(auditService).logAction(eq(officer), eq("COMPOSITE_PAYMENT"), eq("Composite"), eq(member.getId()), any(),
                anyString());

        // Verify individual creates were audited too (by the internal method calls)
        verify(auditService).logAction(eq(officer), eq("TRANSACTION_CREATE"), anyString(), eq(111L), any(), any());
        verify(auditService).logAction(eq(officer), eq("PAYMENT_CREATE"), anyString(), eq(222L), any(), any());
    }

    @Test
    void processCompositePayment_ShouldPrioritizeInterest() {
        // Test Split Logic: 1000 Payment. Interest Due = 100. Penalty = 0. Principal =
        // 900.
        CompositePaymentRequest request = new CompositePaymentRequest();
        request.setMemberId(member.getId());
        request.setLoanPaymentAmount(new BigDecimal("1000"));
        request.setLoanId(loan.getId());
        request.setShareAmount(BigDecimal.ZERO); // No share

        when(dashboardService.getCurrentFiscalPeriod()).thenReturn(createOpenPeriod());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        // Mock Loan Payment
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            // Verify Split inside the mock
            // Interest: (5000 * 10% / 12) ~ 41.67.
            // Logic in Service: calculateMinimumInterest(loanId).
            // But we didn't mock calculateMinimumInterest (it's internal).
            // However, calculateMinimumInterest relies on loanRepository.findById again.
            // Since we mocked loanRepository, it works.
            // Expected Interest ~ 41.67
            // Expected Principal ~ 1000 - 41.67 = 958.33

            p.setId(222L);
            return p;
        });

        transactionService.processCompositePayment(request, officer);

        verify(paymentRepository).save(argThat(payment -> {
            boolean interestCheck = payment.getInterestAmount().compareTo(BigDecimal.ZERO) > 0;
            boolean principalCheck = payment.getPrincipalAmount().compareTo(new BigDecimal("900")) > 0;
            return interestCheck && principalCheck;
        }));
    }

    private com.bansaiyai.bansaiyai.dto.dashboard.FiscalPeriodDTO createOpenPeriod() {
        return new com.bansaiyai.bansaiyai.dto.dashboard.FiscalPeriodDTO("October 2025", "OPEN");
    }
}
