package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.*;
import com.bansaiyai.bansaiyai.entity.enums.*;
import com.bansaiyai.bansaiyai.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DividendModuleTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SavingRepository savingRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private DividendDistributionRepository dividendDistributionRepository;
    @Mock
    private DividendRecipientRepository dividendRecipientRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private AuditService auditService; // Needed for logging

    @InjectMocks
    private DividendService dividendService;

    private User presidentUser;
    private Member member1;
    private SavingAccount saving1;

    @BeforeEach
    void setup() {
        presidentUser = new User();
        presidentUser.setUsername("prez");
        presidentUser.setRole(User.Role.PRESIDENT);

        member1 = new Member();
        member1.setId(1L);
        member1.setMemberId("1001");
        member1.setIsActive(true);

        saving1 = new SavingAccount();
        saving1.setId(10L);
        saving1.setMember(member1);
        saving1.setShareCapital(new BigDecimal("10000.00")); // 10k Shares
        saving1.setIsActive(true);
    }

    @Test
    void testCalculateDividends() {
        // Mock Data
        when(memberRepository.findByIsActiveTrue()).thenReturn(List.of(member1));
        when(savingRepository.findFirstByMemberIdAndIsActiveTrue(member1.getId())).thenReturn(Optional.of(saving1));
        when(paymentRepository.sumInterestByMemberAndYear(member1.getId(), 2024)).thenReturn(new BigDecimal("1000.00"));

        // existsByYear returns false by default for mock, which is what we want.
        when(dividendDistributionRepository.save(any(DividendDistribution.class))).thenAnswer(i -> i.getArguments()[0]);
        when(dividendRecipientRepository.save(any(DividendRecipient.class))).thenAnswer(i -> i.getArguments()[0]);

        // Logic
        DividendDistribution dist = dividendService.calculateDividends(
                2024,
                new BigDecimal("5.00"), // 5% of 10k = 500
                new BigDecimal("10.00"), // 10% of 1k = 100
                presidentUser);

        // Verify
        assertEquals(2024, dist.getYear());
        // Dist rate: div + avg return. Total = 500 + 100 = 600.
        // Wait, did I set total fields? Logic does.
        assertEquals(0, new BigDecimal("600.00")
                .compareTo(dist.getTotalDividendAmount().add(dist.getTotalAverageReturnAmount())));

        verify(dividendRecipientRepository, times(1)).save(any(DividendRecipient.class));
    }

    @Test
    void testDistributeDividends() {
        // Mock Calculated Distribution
        DividendDistribution dist = new DividendDistribution();
        dist.setId(100L);
        dist.setYear(2024);
        dist.setStatus(ApprovalStatus.PENDING);
        dist.setTotalDividendAmount(new BigDecimal("600.00"));

        DividendRecipient recipient = new DividendRecipient();
        recipient.setMember(member1);
        recipient.setTotalAmount(new BigDecimal("600.00"));

        when(dividendDistributionRepository.findByYear(2024)).thenReturn(Optional.of(dist));
        when(dividendRecipientRepository.findByDividendDistributionId(dist.getId())).thenReturn(List.of(recipient));
        when(dividendDistributionRepository.save(any(DividendDistribution.class))).thenAnswer(i -> i.getArguments()[0]);

        // Execute
        DividendDistribution result = dividendService.distributeDividends(2024, presidentUser);

        // Verify
        assertEquals(ApprovalStatus.APPROVED, result.getStatus());
        assertNotNull(dist.getDistributedAt());

        verify(transactionService, times(1)).processDividendPayout(
                eq(member1.getId()),
                eq(new BigDecimal("600.00")),
                eq(2024),
                eq(presidentUser));
    }
}
