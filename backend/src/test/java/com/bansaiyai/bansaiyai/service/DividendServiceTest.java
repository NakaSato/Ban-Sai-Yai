package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.DividendDistribution;
import com.bansaiyai.bansaiyai.entity.DividendRecipient;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.entity.enums.ApprovalStatus;
import com.bansaiyai.bansaiyai.exception.BusinessException;
import com.bansaiyai.bansaiyai.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DividendServiceTest {

    @Mock
    private DividendDistributionRepository dividendDistributionRepository;
    @Mock
    private DividendRecipientRepository dividendRecipientRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private AuditService auditService;
    @Mock
    private SavingRepository savingRepository;

    @InjectMocks
    private DividendService dividendService;

    @Test
    void calculateDividends_Success() {
        // Arrange
        Integer year = 2024;
        BigDecimal divRate = new BigDecimal("5.0");
        BigDecimal avgRetRate = new BigDecimal("10.0");
        User user = new User();

        Member member = Member.builder()
                .shareCapital(new BigDecimal("1000"))
                .build();
        member.setId(1L);

        when(dividendDistributionRepository.existsByYear(year)).thenReturn(false);
        when(memberRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(member));
        when(paymentRepository.sumInterestByMemberAndYear(eq(1L), eq(year))).thenReturn(new BigDecimal("100"));

        when(dividendDistributionRepository.save(any(DividendDistribution.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        DividendDistribution result = dividendService.calculateDividends(year, divRate, avgRetRate, user);

        // Assert
        assertNotNull(result);
        assertEquals(year, result.getYear());
        // Share Dividend: 1000 * 5% = 50.00
        // Avg Return: 100 * 10% = 10.00
        // Total Div Amount stored in Dist should be 50.00
        // Total Avg Return stored in Dist should be 10.00
        assertEquals(new BigDecimal("50.00"), result.getTotalDividendAmount());
        assertEquals(new BigDecimal("10.00"), result.getTotalAverageReturnAmount());

        verify(dividendRecipientRepository, times(1)).save(any(DividendRecipient.class));
    }

    @Test
    void calculateDividends_AlreadyExists_ThrowsException() {
        when(dividendDistributionRepository.existsByYear(2024)).thenReturn(true);
        User user = new User();

        assertThrows(BusinessException.class,
                () -> dividendService.calculateDividends(2024, BigDecimal.TEN, BigDecimal.TEN, user));
    }

    @Test
    void distributeDividends_Success() {
        // Arrange
        Integer year = 2024;
        User user = new User();
        DividendDistribution dist = DividendDistribution.builder()
                .id(1L)
                .year(year)
                .status(ApprovalStatus.PENDING)
                .build();

        Member member = new Member();
        member.setId(10L);

        DividendRecipient recipient = DividendRecipient.builder()
                .member(member)
                .totalAmount(new BigDecimal("100"))
                .build();

        when(dividendDistributionRepository.findByYear(year)).thenReturn(Optional.of(dist));
        when(dividendRecipientRepository.findByDividendDistributionId(dist.getId()))
                .thenReturn(Collections.singletonList(recipient));
        when(dividendDistributionRepository.save(any(DividendDistribution.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        DividendDistribution result = dividendService.distributeDividends(year, user);

        // Assert
        assertEquals(ApprovalStatus.APPROVED, result.getStatus());
        assertNotNull(result.getDistributedAt());
        verify(transactionService).processDividendPayout(eq(10L), eq(new BigDecimal("100")), eq(year), eq(user));
    }
}
