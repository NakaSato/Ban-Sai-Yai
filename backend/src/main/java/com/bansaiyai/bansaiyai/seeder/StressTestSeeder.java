package com.bansaiyai.bansaiyai.seeder;

import com.bansaiyai.bansaiyai.entity.*;
import com.bansaiyai.bansaiyai.entity.enums.*;
import com.bansaiyai.bansaiyai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class StressTestSeeder {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    @Transactional
    public void seedStressData() {
        log.info("Starting Stress Test Data Seeding...");
        long startTime = System.currentTimeMillis();

        // 1. Seed 1,000 Members (12 months of history)
        List<Member> members = seedMembers(1000);

        // 2. Seed Loans for 50% of members
        seedLoans(members);

        long endTime = System.currentTimeMillis();
        log.info("Stress Test Seeding Completed in {} ms", (endTime - startTime));
    }

    private List<Member> seedMembers(int count) {
        log.info("Seeding {} members...", count);
        List<Member> members = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < count; i++) {
            // Distribute registration over last 12 months
            int monthsBack = ThreadLocalRandom.current().nextInt(0, 13);
            LocalDate regDate = today.minusMonths(monthsBack).minusDays(ThreadLocalRandom.current().nextInt(0, 28));

            Member member = new Member();
            member.setUuid(UUID.randomUUID());
            member.setMemberId(String.format("STRESS-%05d", i));
            member.setIdCard(String.format("%013d", i + 1000000)); // Unique dummy ID
            member.setName("Stress Member " + i);
            member.setDateOfBirth(LocalDate.of(1980 + ThreadLocalRandom.current().nextInt(0, 20), 1, 1));
            member.setAddress("Stress Test Lane " + i);
            member.setPhone(String.format("08%08d", i));
            member.setRegistrationDate(regDate);
            member.setIsActive(true);
            member.setShareCapital(BigDecimal.valueOf(1000));
            member.setCreatedAt(regDate.atStartOfDay()); // Important for Trends

            members.add(member);

            // Batch save every 100 to avoid memory issues if JPA cache gets large
            if (members.size() >= 100) {
                memberRepository.saveAll(members);
                members.clear();
            }
        }
        // Save remaining
        if (!members.isEmpty()) {
            memberRepository.saveAll(members);
        }

        // Return fresh list from DB? Or just return empty since we don't strictly need
        // them all in memory for next step if we fetch random ones
        return memberRepository.findAll();
    }

    private void seedLoans(List<Member> members) {
        log.info("Seeding loans...");
        List<Loan> loans = new ArrayList<>();
        LocalDate today = LocalDate.now();

        int memberCount = members.size();
        // Target ~500 loans
        int loansToCreate = 500;

        for (int i = 0; i < loansToCreate; i++) {
            Member member = members.get(ThreadLocalRandom.current().nextInt(memberCount));

            Loan loan = new Loan();
            loan.setUuid(UUID.randomUUID());
            loan.setLoanNumber("LN-STRESS-" + i);
            loan.setMember(member);
            loan.setPrincipalAmount(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(10, 100) * 1000)); // 10k -
                                                                                                              // 100k
            loan.setInterestRate(BigDecimal.valueOf(12.0));
            loan.setTermMonths(12);
            loan.setLoanType(LoanType.PERSONAL);

            // Determine Status and Dates based on desired distribution
            int rand = ThreadLocalRandom.current().nextInt(100);

            if (rand < 60) {
                // 60% Active (Healthy)
                loan.setStatus(LoanStatus.ACTIVE);
                loan.setStartDate(today.minusMonths(1));
                loan.setMaturityDate(today.plusMonths(11));
                loan.setOutstandingBalance(loan.getPrincipalAmount());
            } else if (rand < 70) {
                // 10% Active (1-30 days overdue)
                loan.setStatus(LoanStatus.ACTIVE);
                loan.setStartDate(today.minusMonths(13)); // Started long ago
                loan.setMaturityDate(today.minusDays(15)); // Expired 15 days ago
                loan.setOutstandingBalance(BigDecimal.valueOf(5000));
            } else if (rand < 80) {
                // 10% Active (31-60 days overdue)
                loan.setStatus(LoanStatus.ACTIVE);
                loan.setStartDate(today.minusMonths(14));
                loan.setMaturityDate(today.minusDays(45));
                loan.setOutstandingBalance(BigDecimal.valueOf(5000));
            } else if (rand < 85) {
                // 5% Active (61-90 days overdue)
                loan.setStatus(LoanStatus.ACTIVE);
                loan.setStartDate(today.minusMonths(15));
                loan.setMaturityDate(today.minusDays(75));
                loan.setOutstandingBalance(BigDecimal.valueOf(5000));
            } else if (rand < 90) {
                // 5% Active (>90 days overdue)
                loan.setStatus(LoanStatus.ACTIVE);
                loan.setStartDate(today.minusMonths(16));
                loan.setMaturityDate(today.minusDays(100));
                loan.setOutstandingBalance(BigDecimal.valueOf(5000));
            } else {
                // 10% Closed
                loan.setStatus(LoanStatus.COMPLETED);
                loan.setStartDate(today.minusMonths(6));
                loan.setMaturityDate(today.minusMonths(1));
                loan.setOutstandingBalance(BigDecimal.ZERO);
            }

            // Set approved date for throughput stats
            loan.setApprovedDate(loan.getStartDate());
            loan.setApprovedBy("admin");

            loans.add(loan);

            if (loans.size() >= 100) {
                loanRepository.saveAll(loans);
                loans.clear();
            }
        }
        if (!loans.isEmpty()) {
            loanRepository.saveAll(loans);
        }
    }
}
