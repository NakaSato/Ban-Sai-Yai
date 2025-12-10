package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.report.BalanceSheetDTO;
import com.bansaiyai.bansaiyai.dto.report.IncomeExpenseReportDTO;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import com.bansaiyai.bansaiyai.entity.enums.TransactionType;
import com.bansaiyai.bansaiyai.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AccountingRepository accountingRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SavingTransactionRepository savingTransactionRepository;
    @Mock
    private LoanBalanceRepository loanBalanceRepository;
    @Mock
    private SavingBalanceRepository savingBalanceRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void generateIncomeExpenseReport_Success() {
        // Arrange
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        // Mock Payment Income
        List<Object[]> revenue = new ArrayList<>();
        revenue.add(new Object[] { PaymentType.LOAN_INTEREST, new BigDecimal("500") });
        when(paymentRepository.findRevenueByDateRangeGroupByType(start, end)).thenReturn(revenue);

        // Mock Manual Income
        List<Object[]> manualIncome = new ArrayList<>();
        manualIncome.add(new Object[] { "Other Income", new BigDecimal("100") });
        when(accountingRepository.sumCreditByCodePatternAndDateRange(start, end, "4%")).thenReturn(manualIncome);

        // Mock Savings Interest Expense
        when(savingTransactionRepository.sumAmountByTypeAndDateRange(TransactionType.INTEREST_CREDIT, start, end))
                .thenReturn(new BigDecimal("50"));

        // Mock Manual Expense
        List<Object[]> manualExpense = new ArrayList<>();
        manualExpense.add(new Object[] { "Utility", new BigDecimal("20") });
        when(accountingRepository.sumDebitByCodePatternAndDateRange(start, end, "5%")).thenReturn(manualExpense);

        // Act
        IncomeExpenseReportDTO report = reportService.generateIncomeExpenseReport(start, end);

        // Assert
        // Income = 500 + 100 = 600
        assertEquals(new BigDecimal("600"), report.getTotalIncome());
        // Expense = 50 + 20 = 70
        assertEquals(new BigDecimal("70"), report.getTotalExpense());
        // Net = 530
        assertEquals(new BigDecimal("530"), report.getNetProfit());
    }

    @Test
    void generateBalanceSheet_Success() {
        // Arrange
        LocalDate asOf = LocalDate.of(2024, 12, 31);

        // Assets
        when(loanBalanceRepository.sumOutstandingBalanceByDate(asOf)).thenReturn(new BigDecimal("50000"));

        // Liabilities
        when(savingBalanceRepository.sumClosingBalanceByDate(asOf)).thenReturn(new BigDecimal("20000"));

        // Equity (Shares)
        when(memberRepository.sumTotalShareCapital()).thenReturn(new BigDecimal("10000"));

        // Retained Earnings = Assets - Liabilities - Existing Equity
        // 50000 - 20000 - 10000 = 20000

        // Act
        BalanceSheetDTO sheet = reportService.generateBalanceSheet(asOf);

        // Assert
        assertEquals(new BigDecimal("50000"), sheet.getTotalAssets());
        assertEquals(new BigDecimal("20000"), sheet.getTotalLiabilities());

        // Equity should include Retained Earnings
        // 10000 (Shares) + 20000 (Retained) = 30000
        assertEquals(new BigDecimal("30000"), sheet.getTotalEquity());

        // Accounting Equation Check
        assertEquals(sheet.getTotalAssets(), sheet.getTotalLiabilities().add(sheet.getTotalEquity()));
    }
}
