package com.bansaiyai.bansaiyai.component;

import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.seeder.StressTestSeeder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StressTestSeederIntegrationTest {

    @Autowired
    private StressTestSeeder stressTestSeeder;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private com.bansaiyai.bansaiyai.service.DashboardService dashboardService;

    @Test
    public void testStressSeedingAndPerformance() {
        long initialMemberCount = memberRepository.count();
        long initialLoanCount = loanRepository.count();

        // 1. Run the seeder
        System.out.println("Starting Seeder...");
        long startSeed = System.currentTimeMillis();
        stressTestSeeder.seedStressData();
        long endSeed = System.currentTimeMillis();
        System.out.println("Seeding took: " + (endSeed - startSeed) + " ms");

        long finalMemberCount = memberRepository.count();
        long finalLoanCount = loanRepository.count();

        // Verify counts
        // Expecting 1000 new members
        Assertions.assertEquals(initialMemberCount + 1000, finalMemberCount, "Should have added 1000 members");

        // Expecting 500 new loans
        Assertions.assertEquals(initialLoanCount + 500, finalLoanCount, "Should have added 500 loans");

        System.out.println("Stress Seeding Verification Passed: " + (finalMemberCount - initialMemberCount)
                + " members added, " + (finalLoanCount - initialLoanCount) + " loans added.");

        // 2. Benchmark PAR Analysis
        long startPar = System.currentTimeMillis();
        var parData = dashboardService.calculatePARAnalysis();
        long endPar = System.currentTimeMillis();
        long parTime = endPar - startPar;
        System.out.println("PAR Analysis took: " + parTime + " ms");
        Assertions.assertNotNull(parData);
        Assertions.assertTrue(parData.getTotalPortfolio().compareTo(java.math.BigDecimal.ZERO) > 0,
                "Portfolio should not be zero");

        // 3. Benchmark Membership Trends
        long startTrends = System.currentTimeMillis();
        var trendsData = dashboardService.getMembershipTrends(12);
        long endTrends = System.currentTimeMillis();
        long trendsTime = endTrends - startTrends;
        System.out.println("Membership Trends took: " + trendsTime + " ms");
        Assertions.assertNotNull(trendsData);
        Assertions.assertEquals(12, trendsData.getLabels().size());

        // 4. Benchmark Liquidity Ratio
        long startLiq = System.currentTimeMillis();
        var liqData = dashboardService.calculateLiquidityRatio();
        long endLiq = System.currentTimeMillis();
        long liqTime = endLiq - startLiq;
        System.out.println("Liquidity Ratio took: " + liqTime + " ms");
        Assertions.assertNotNull(liqData);

        // Performance Assertions (Soft limits for test environment)
        // Adjust these based on expected performance in test env
        if (parTime > 1000)
            System.err.println("WARNING: PAR Analysis is slow (>1000ms)");
        if (trendsTime > 1000)
            System.err.println("WARNING: Membership Trends is slow (>1000ms)");
    }
}
