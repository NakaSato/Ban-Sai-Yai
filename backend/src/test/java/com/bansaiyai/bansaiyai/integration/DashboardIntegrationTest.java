package com.bansaiyai.bansaiyai.integration;

import com.bansaiyai.bansaiyai.dto.dashboard.FiscalPeriodDTO;
import com.bansaiyai.bansaiyai.dto.dashboard.MemberSearchResultDTO;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.service.DashboardService;
import com.bansaiyai.bansaiyai.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class DashboardIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    public void setUp() {
        // Create a test member
        testMember = Member.builder()
                .memberId("TEST-001")
                .name("John Doe")
                .idCard("1234567890123")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Test Street")
                .phone("0812345678")
                .email("john.doe@test.com")
                .registrationDate(LocalDate.now())
                .isActive(true)
                .shareCapital(BigDecimal.valueOf(1000))
                .build();

        memberRepository.save(testMember);
    }

    @Test
    public void testGetCurrentFiscalPeriod() {
        FiscalPeriodDTO fiscalPeriod = dashboardService.getCurrentFiscalPeriod();

        assertNotNull(fiscalPeriod);
        assertNotNull(fiscalPeriod.period());
        assertNotNull(fiscalPeriod.status());
        assertTrue(fiscalPeriod.status().equals("OPEN") || fiscalPeriod.status().equals("CLOSED"));
    }

    @Test
    public void testSearchMembers_ByName() {
        List<MemberSearchResultDTO> results = dashboardService.searchMembers("John", 5);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.getFirstName().contains("John")));
    }

    @Test
    public void testSearchMembers_ByMemberId() {
        List<MemberSearchResultDTO> results = dashboardService.searchMembers("TEST-001", 5);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    public void testSearchMembers_ByIdCard() {
        List<MemberSearchResultDTO> results = dashboardService.searchMembers("1234567890123", 5);

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    public void testSearchMembers_EmptyQuery() {
        List<MemberSearchResultDTO> results = dashboardService.searchMembers("", 5);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSearchMembers_NoResults() {
        List<MemberSearchResultDTO> results = dashboardService.searchMembers("NonExistentMember12345", 5);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSearchMembers_LimitResults() {
        // Create multiple test members
        for (int i = 0; i < 10; i++) {
            Member member = Member.builder()
                    .memberId("TEST-" + String.format("%03d", i + 2))
                    .name("Test Member " + i)
                    .idCard(String.format("9876543210%03d", i))
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .address("123 Test Street")
                    .phone("081234567" + i)
                    .registrationDate(LocalDate.now())
                    .isActive(true)
                    .shareCapital(BigDecimal.valueOf(1000))
                    .build();
            memberRepository.save(member);
        }

        List<MemberSearchResultDTO> results = dashboardService.searchMembers("Test", 5);

        assertNotNull(results);
        assertTrue(results.size() <= 5);
    }
}
