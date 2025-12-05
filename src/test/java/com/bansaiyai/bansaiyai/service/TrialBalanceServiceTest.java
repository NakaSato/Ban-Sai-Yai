package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.dashboard.TrialBalanceDTO;
import com.bansaiyai.bansaiyai.repository.AccountingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for trial balance calculation in DashboardService
 */
@ExtendWith(MockitoExtension.class)
class TrialBalanceServiceTest {

    @Mock
    private AccountingRepository accountingRepository;

    @Mock
    private com.bansaiyai.bansaiyai.repository.MemberRepository memberRepository;

    @Mock
    private com.bansaiyai.bansaiyai.repository.LoanRepository loanRepository;

    @Mock
    private com.bansaiyai.bansaiyai.repository.SavingRepository savingRepository;

    @Mock
    private com.bansaiyai.bansaiyai.repository.PaymentRepository paymentRepository;

    @Mock
    private com.bansaiyai.bansaiyai.repository.SavingTransactionRepository savingTransactionRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
            memberRepository,
            loanRepository,
            savingRepository,
            paymentRepository,
            savingTransactionRepository,
            accountingRepository
        );
    }

    @Test
    void testCalculateTrialBalance_Balanced() {
        // Given: Equal debits and credits
        BigDecimal debits = new BigDecimal("50000.00");
        BigDecimal credits = new BigDecimal("50000.00");
        
        when(accountingRepository.sumDebitsByFiscalPeriod(anyString())).thenReturn(debits);
        when(accountingRepository.sumCreditsByFiscalPeriod(anyString())).thenReturn(credits);

        // When: Calculate trial balance
        TrialBalanceDTO result = dashboardService.calculateTrialBalance();

        // Then: Should be balanced
        assertNotNull(result);
        assertEquals(0, debits.compareTo(result.getTotalDebits()));
        assertEquals(0, credits.compareTo(result.getTotalCredits()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getVariance()));
        assertTrue(result.isBalanced());
    }

    @Test
    void testCalculateTrialBalance_Unbalanced_DebitsExceed() {
        // Given: Debits exceed credits
        BigDecimal debits = new BigDecimal("60000.00");
        BigDecimal credits = new BigDecimal("50000.00");
        
        when(accountingRepository.sumDebitsByFiscalPeriod(anyString())).thenReturn(debits);
        when(accountingRepository.sumCreditsByFiscalPeriod(anyString())).thenReturn(credits);

        // When: Calculate trial balance
        TrialBalanceDTO result = dashboardService.calculateTrialBalance();

        // Then: Should be unbalanced with positive variance
        assertNotNull(result);
        assertEquals(0, debits.compareTo(result.getTotalDebits()));
        assertEquals(0, credits.compareTo(result.getTotalCredits()));
        assertEquals(0, new BigDecimal("10000.00").compareTo(result.getVariance()));
        assertFalse(result.isBalanced());
    }

    @Test
    void testCalculateTrialBalance_Unbalanced_CreditsExceed() {
        // Given: Credits exceed debits
        BigDecimal debits = new BigDecimal("40000.00");
        BigDecimal credits = new BigDecimal("50000.00");
        
        when(accountingRepository.sumDebitsByFiscalPeriod(anyString())).thenReturn(debits);
        when(accountingRepository.sumCreditsByFiscalPeriod(anyString())).thenReturn(credits);

        // When: Calculate trial balance
        TrialBalanceDTO result = dashboardService.calculateTrialBalance();

        // Then: Should be unbalanced with negative variance
        assertNotNull(result);
        assertEquals(0, debits.compareTo(result.getTotalDebits()));
        assertEquals(0, credits.compareTo(result.getTotalCredits()));
        assertEquals(0, new BigDecimal("-10000.00").compareTo(result.getVariance()));
        assertFalse(result.isBalanced());
    }

    @Test
    void testCalculateTrialBalance_NullValues() {
        // Given: Null values from repository
        when(accountingRepository.sumDebitsByFiscalPeriod(anyString())).thenReturn(null);
        when(accountingRepository.sumCreditsByFiscalPeriod(anyString())).thenReturn(null);

        // When: Calculate trial balance
        TrialBalanceDTO result = dashboardService.calculateTrialBalance();

        // Then: Should handle nulls gracefully
        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalDebits()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCredits()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getVariance()));
        assertTrue(result.isBalanced());
    }

    @Test
    void testCalculateTrialBalance_ZeroValues() {
        // Given: Zero values
        BigDecimal zero = BigDecimal.ZERO;
        
        when(accountingRepository.sumDebitsByFiscalPeriod(anyString())).thenReturn(zero);
        when(accountingRepository.sumCreditsByFiscalPeriod(anyString())).thenReturn(zero);

        // When: Calculate trial balance
        TrialBalanceDTO result = dashboardService.calculateTrialBalance();

        // Then: Should be balanced at zero
        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalDebits()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCredits()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getVariance()));
        assertTrue(result.isBalanced());
    }
}
