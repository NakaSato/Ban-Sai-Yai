package com.bansaiyai.bansaiyai.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bansaiyai.bansaiyai.BaseIntegrationTest;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.entity.enums.LoanType;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

public class MemberFinancialControllerTest extends BaseIntegrationTest {

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PaymentRepository paymentRepository;

        @Autowired
        private LoanRepository loanRepository;

        private Member testMember;

        @BeforeEach
        void setUp() {
                paymentRepository.deleteAll();
                loanRepository.deleteAll();
                memberRepository.deleteAll();
                userRepository.deleteAll();

                // Create User
                User user = User.builder()
                                .username("testmember")
                                .password("password")
                                .firstName("Test")
                                .lastName("Member")
                                .email("test@example.com")
                                .role(User.Role.MEMBER)
                                .isActive(true)
                                .build();
                user = userRepository.save(user);

                // Create Member
                testMember = Member.builder()
                                .user(user)
                                .memberId("MEM-001")
                                .uuid(UUID.randomUUID())
                                .name("Test Member")
                                .idCard("1234567890123")
                                .dateOfBirth(LocalDate.of(1990, 1, 1))
                                .address("Test Address")
                                .phone("0812345678")
                                .registrationDate(LocalDate.now().minusMonths(12))
                                .isActive(true)
                                .shareCapital(new BigDecimal("1200.00")) // Initial shares
                                .build();
                testMember = memberRepository.save(testMember);

                // Create Share Payment
                Payment sharePayment = Payment.builder()
                                .paymentNumber("PAY-001")
                                .member(testMember)
                                .amount(new BigDecimal("100.00"))
                                .paymentType(PaymentType.SHARE_CAPITAL)
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .paymentDate(LocalDate.now())
                                .build();
                paymentRepository.save(sharePayment);

                // Create Loan
                Loan loan = Loan.builder()
                                .member(testMember)
                                .loanNumber("LOAN-001")
                                .uuid(UUID.randomUUID())
                                .loanType(LoanType.EMERGENCY)
                                .principalAmount(new BigDecimal("10000.00"))
                                .interestRate(new BigDecimal("10.00"))
                                .termMonths(12)
                                .startDate(LocalDate.now().minusMonths(1))
                                .endDate(LocalDate.now().plusMonths(11))
                                .status(LoanStatus.ACTIVE)
                                .outstandingBalance(new BigDecimal("9000.00"))
                                .build();
                loan = loanRepository.save(loan);

                // Add loan to member list manually if needed, but JPA should handle query
                // Wait, the Service uses member.getLoans() which is FetchType.LAZY.
                // In test context with @Transactional session, it should work.
                // However, BaseIntegrationTest doesn't usually have @Transactional on class
                // level by default
                // unless specified. Let's rely on repository queries implicitly or ensure
                // session is open.
                // Actually, MemberFinancialService relies on member.getLoans().
                // If the test transaction commits after save, the session closes.
                // MockMvc tests run in a transaction if @Transactional is present on test
                // class.
                // BaseIntegrationTest often has it or extends simpler setup.
                // Let's check BaseIntegrationTest again later if lazy init fails.

                // Create Loan Payment
                Payment loanPayment = Payment.builder()
                                .paymentNumber("PAY-002")
                                .member(testMember)
                                .loan(loan)
                                .amount(new BigDecimal("1100.00"))
                                .paymentType(PaymentType.LOAN_PRINCIPAL) // Simplified type
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .paymentDate(LocalDate.now())
                                .build();
                paymentRepository.save(loanPayment);
        }

        @Test
        @WithMockUser(username = "testmember", roles = "MEMBER")
        void shouldGetMyFinancials() throws Exception {
                mockMvc.perform(get("/api/financials/me"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.shares.totalAccumulatedShares").value(1200.00))
                                .andExpect(jsonPath("$.shares.monthlyPaymentStatus").value(true))
                                .andExpect(jsonPath("$.shares.transactions").isArray())
                                .andExpect(jsonPath("$.shares.transactions", hasSize(1)))
                                .andExpect(jsonPath("$.loans").isArray())
                                .andExpect(jsonPath("$.loans", hasSize(1)))
                                .andExpect(jsonPath("$.loans[0].loanNumber").value("LOAN-001"))
                                .andExpect(jsonPath("$.loans[0].transactions", hasSize(1)));
        }
}
