package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.LoanBalance;
import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import com.bansaiyai.bansaiyai.repository.LoanBalanceRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountingServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanBalanceRepository loanBalanceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private com.bansaiyai.bansaiyai.repository.UserRepository userRepository;
    @Mock
    private com.bansaiyai.bansaiyai.service.AuditService auditService;

    @InjectMocks
    private AccountingService accountingService;

    private Loan loan;
    private final int MONTH = 10;
    private final int YEAR = 2025;
    private final YearMonth YEAR_MONTH = YearMonth.of(YEAR, MONTH);
    private final LocalDate START_DATE = YEAR_MONTH.atDay(1);
    private final LocalDate END_DATE = YEAR_MONTH.atEndOfMonth();

    @BeforeEach
    void setUp() {
        loan = Loan.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .loanNumber("LN-20251001-001")
                .principalAmount(new BigDecimal("10000.00"))
                .outstandingBalance(new BigDecimal("9000.00")) // Current Balance
                .interestRate(new BigDecimal("12.00")) // 12% Annual
                .startDate(LocalDate.of(2025, 1, 1))
                .status(LoanStatus.ACTIVE)
                .build();
    }

    @Test
    void closeMonth_ShouldProcessActiveLoan_WhenNotClosedAttempted() {
        // Arrange
        when(loanRepository.findByStatusIn(any())).thenReturn(List.of(loan));
        when(loanBalanceRepository.findByLoanIdAndBalanceDate(loan.getId(), END_DATE)).thenReturn(Optional.empty());

        // Mock Payments
        Payment payment = Payment.builder()
                .paymentType(PaymentType.LOAN_REPAYMENT)
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentDate(LocalDate.of(2025, 10, 15))
                .principalAmount(new BigDecimal("500.00"))
                .interestAmount(new BigDecimal("100.00"))
                .amount(new BigDecimal("600.00"))
                .build();

        when(paymentRepository.findLoanPaymentsByLoan(eq(loan.getId()), anyList()))
                .thenReturn(List.of(payment));

        // Act
        accountingService.closeMonth(MONTH, YEAR);

        // Assert
        verify(loanRepository).findByStatusIn(any());
        verify(loanBalanceRepository).save(any(LoanBalance.class));

        // You could capture the argument and assert values if needed
    }

    @Test
    void closeMonth_ShouldSkip_WhenAlreadyClosed() {
        // Arrange
        when(loanRepository.findByStatusIn(any())).thenReturn(List.of(loan));
        when(loanBalanceRepository.findByLoanIdAndBalanceDate(loan.getId(), END_DATE))
                .thenReturn(Optional.of(new LoanBalance()));

        // Act
        accountingService.closeMonth(MONTH, YEAR);

        // Assert
        verify(loanBalanceRepository, never()).save(any(LoanBalance.class));
    }

    @Test
    void checkAndFlagOverdueLoans_ShouldMarkDefaulted() {
        Loan overdueLoan = new Loan();
        overdueLoan.setId(99L);
        overdueLoan.setLoanNumber("L-OVERDUE");
        overdueLoan.setOutstandingBalance(new BigDecimal("5000"));
        overdueLoan.setMaturityDate(LocalDate.now().minusDays(1)); // Yesterday
        overdueLoan.setStatus(LoanStatus.ACTIVE);

        when(loanRepository.findOverdueLoansByStatus(LoanStatus.ACTIVE))
                .thenReturn(List.of(overdueLoan));

        accountingService.checkAndFlagOverdueLoans();

        assertEquals(LoanStatus.DEFAULTED, overdueLoan.getStatus());
        verify(loanRepository).save(overdueLoan);
    }
}
