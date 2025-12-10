package com.bansaiyai.bansaiyai.seeder;

import com.bansaiyai.bansaiyai.entity.*;
import com.bansaiyai.bansaiyai.entity.enums.*;
import com.bansaiyai.bansaiyai.repository.*;
import com.bansaiyai.bansaiyai.service.TransactionService;
import com.bansaiyai.bansaiyai.dto.DepositRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final TransactionService transactionService; // Reuse logic for deposits
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seed() {
        log.info("Starting Data Seeding...");

        // 1. Seed Roles

        Role managerRole = createRoleIfNotFound(ERole.ROLE_MANAGER);
        Role officerRole = createRoleIfNotFound(ERole.ROLE_OFFICER);
        Role userRole = createRoleIfNotFound(ERole.ROLE_USER); // Basic Member
        Role presidentRole = createRoleIfNotFound(ERole.ROLE_PRESIDENT);

        // 2. Seed Users

        User manager = createUserIfNotFound("manager", "manager123", Set.of(managerRole));
        User secretary = createUserIfNotFound("secretary", "secretary123", Set.of(managerRole)); // Map Secretary to
                                                                                                 // Manager role
        User officer = createUserIfNotFound("officer", "officer123", Set.of(officerRole));
        User president = createUserIfNotFound("president", "president123", Set.of(presidentRole));

        // 3. Seed Members
        for (int i = 1; i <= 10; i++) {
            String idCard = String.format("1234567890%03d", i);
            String name = "Member " + i;
            Member member = memberRepository.findByIdCard(idCard).orElse(null);

            if (member == null) {
                member = new Member();
                member.setIdCard(idCard);
                member.setName(name);
                member.setDateOfBirth(LocalDate.of(1990, 1, 1));
                member.setAddress("123 Street " + i);
                member.setPhone("081234567" + i);
                member.setOccupation("Farmer");
                member.setMonthlyIncome(new BigDecimal("15000"));
                member.setShareCapital(new BigDecimal("1000")); // Initial Share
                member.setRegistrationDate(LocalDate.now());
                member.setIsActive(true);
                memberRepository.save(member);
                log.info("Seeded Member: {}", name);
            }
        }

        // 4. Seed Loans & Transactions
        // Get a member to play with
        Member member1 = memberRepository.findByIdCard("1234567890001").orElseThrow();

        // Create a Loan for Member 1
        if (loanRepository.findByMemberId(member1.getId(), org.springframework.data.domain.Pageable.unpaged())
                .isEmpty()) {
            Loan loan = new Loan();
            loan.setMember(member1);
            loan.setLoanNumber("LN-001");
            loan.setPrincipalAmount(new BigDecimal("50000"));
            loan.setInterestRate(new BigDecimal("12.00")); // 12%
            loan.setTermMonths(12);
            loan.setStartDate(LocalDate.now());
            loan.setMaturityDate(LocalDate.now().plusMonths(12));
            loan.setOutstandingBalance(new BigDecimal("50000"));
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setLoanType(LoanType.PERSONAL);
            loanRepository.save(loan);
            log.info("Seeded Loan for Member 1");
        }

        // Create Pending Deposit for Manager to Approve
        // Using TransactionService to logic but manually creating might be needed if
        // service auto-approves?
        // Let's assume processDepositWithCreator sets to PENDING because of the Officer
        // role (simulated by 'officer' user).
        // Actually the service logic I viewed earlier:
        // `processDepositWithCreator` sets `ApprovalStatus.PENDING` explicitly.

        try {
            DepositRequest deposit = new DepositRequest();
            deposit.setMemberId(member1.getId());
            deposit.setAmount(new BigDecimal("5000"));
            deposit.setNotes("Pending Deposit for Seed");

            transactionService.processDepositWithCreator(deposit, officer); // Created by Officer, pending approval
            log.info("Seeded Pending Deposit for Member 1");
        } catch (Exception e) {
            log.error("Failed to seed deposit: {}", e.getMessage());
        }

        log.info("Data Seeding Completed.");
    }

    private Role createRoleIfNotFound(ERole name) {
        return roleRepository.findByRoleName(name.name()).orElseGet(() -> roleRepository.save(
                Role.builder().roleName(name.name()).build()));
    }

    private User createUserIfNotFound(String username, String password, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            return userRepository.findByUsername(username).get();
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(username + "@example.com")
                .status(com.bansaiyai.bansaiyai.entity.User.UserStatus.ACTIVE)
                .enabled(true)
                .build();

        if (roles != null && !roles.isEmpty()) {
            user.setRbacRole(roles.iterator().next());
            try {
                String roleName = roles.iterator().next().getRoleName().replace("ROLE_", "");
                user.setRole(com.bansaiyai.bansaiyai.entity.User.Role.valueOf(roleName));
            } catch (Exception e) {
                user.setRole(com.bansaiyai.bansaiyai.entity.User.Role.MEMBER);
            }
        }
        return userRepository.save(user);
    }
}
