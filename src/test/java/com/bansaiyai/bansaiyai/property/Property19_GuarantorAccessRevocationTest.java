package com.bansaiyai.bansaiyai.property;

import com.bansaiyai.bansaiyai.entity.Guarantor;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.repository.GuarantorRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.service.GuarantorAccessEvaluator;
import net.jqwik.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 19: Guarantor access revocation on loan completion
 * Validates: Requirements 7.4
 * 
 * This property test ensures that when a loan is completed,
 * guarantors lose access to view that loan.
 * Guarantors should only be able to view active loans they have guaranteed.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Property19_GuarantorAccessRevocationTest {

        @Autowired
        private GuarantorRepository guarantorRepository;

        @Autowired
        private LoanRepository loanRepository;

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private GuarantorAccessEvaluator guarantorAccessEvaluator;

        /**
         * Property: Guarantor access is revoked when loan is completed
         */
        @Property
        void guarantorAccessRevokedOnLoanCompletion(@ForAll Random random) throws Exception {
                // Arrange: Create borrower and guarantor
                Member borrower = createTestMember("Borrower");
                Member guarantor = createTestMember("Guarantor");

                borrower = memberRepository.save(borrower);
                guarantor = memberRepository.save(guarantor);

                // Create loan with guarantor
                Loan loan = new Loan();
                loan.setMember(borrower);
                loan.setPrincipalAmount(BigDecimal.valueOf(25000.0));
                loan.setStartDate(LocalDate.now().minusMonths(6));
                loan.setEndDate(LocalDate.now().plusMonths(6));
                loan.setStatus(LoanStatus.ACTIVE);
                loan = loanRepository.save(loan);

                // Create guarantor relationship
                Guarantor guarantorRelation = new Guarantor();
                guarantorRelation.setLoan(loan);
                guarantorRelation.setMember(guarantor);
                guarantorRelation.setGuaranteedAmount(BigDecimal.valueOf(25000.0));
                guarantorRelation.setGuaranteeStartDate(LocalDate.now());
                guarantorRelation.setIsActive(true);
                guarantorRepository.save(guarantorRelation);

                // Create User objects for testing
                User guarantorUser = createTestUser(guarantor);
                User borrowerUser = createTestUser(borrower);
                assertNotNull(borrowerUser, "Borrower user should be created for testing");

                // Verify initial access
                assertTrue(guarantorAccessEvaluator.canViewLoan(guarantorUser, loan.getId()),
                                "Guarantor should initially have access to active loan");

                List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());
                assertEquals(1, guaranteedLoans.size(),
                                "Guarantor should initially see one guaranteed loan");

                // Act: Complete the loan
                loan.setStatus(LoanStatus.COMPLETED);
                loanRepository.save(loan);

                // Assert: Verify access revocation
                assertFalse(guarantorAccessEvaluator.canViewLoan(guarantorUser, loan.getId()),
                                "Guarantor should lose access when loan is completed");

                List<Loan> guaranteedLoansAfterCompletion = guarantorAccessEvaluator
                                .getGuaranteedLoans(guarantor.getId());
                assertTrue(guaranteedLoansAfterCompletion.isEmpty(),
                                "Guarantor should see no guaranteed loans after completion");

                // Assert: Verify guarantor relationship is marked as inactive
                Guarantor savedGuarantor = guarantorRepository.findById(guarantorRelation.getId()).orElse(null);
                assertNotNull(savedGuarantor, "Guarantor relation should still exist");
                assertFalse(savedGuarantor.getIsActive(),
                                "Guarantor relation should be marked as inactive");
                assertNotNull(savedGuarantor.getGuaranteeEndDate(),
                                "Guarantee end date should be set");
        }

        /**
         * Property: Multiple guarantors lose access on loan completion
         */
        @Property
        void multipleGuarantorsLoseAccessOnLoanCompletion(@ForAll Random random) throws Exception {
                // Arrange: Create borrower and multiple guarantors
                Member borrower = createTestMember("Borrower");
                Member guarantor1 = createTestMember("Guarantor1");
                Member guarantor2 = createTestMember("Guarantor2");
                Member guarantor3 = createTestMember("Guarantor3");

                borrower = memberRepository.save(borrower);
                guarantor1 = memberRepository.save(guarantor1);
                guarantor2 = memberRepository.save(guarantor2);
                guarantor3 = memberRepository.save(guarantor3);

                // Create loan
                Loan loan = new Loan();
                loan.setMember(borrower);
                loan.setPrincipalAmount(BigDecimal.valueOf(50000.0));
                loan.setStartDate(LocalDate.now().minusMonths(12));
                loan.setEndDate(LocalDate.now().plusMonths(12));
                loan.setStatus(LoanStatus.ACTIVE);
                loan = loanRepository.save(loan);

                // Create multiple guarantor relationships
                Guarantor[] guarantorRelations = new Guarantor[3];
                Member[] guarantors = { guarantor1, guarantor2, guarantor3 };

                for (int i = 0; i < 3; i++) {
                        guarantorRelations[i] = new Guarantor();
                        guarantorRelations[i].setLoan(loan);
                        guarantorRelations[i].setMember(guarantors[i]);
                        guarantorRelations[i].setGuaranteedAmount(BigDecimal.valueOf(16666.67));
                        guarantorRelations[i].setGuaranteeStartDate(LocalDate.now());
                        guarantorRelations[i].setIsActive(true);
                        guarantorRepository.save(guarantorRelations[i]);
                }

                // Verify initial access for all guarantors
                for (Member guarantor : guarantors) {
                        User guarantorUser = createTestUser(guarantor);
                        assertTrue(guarantorAccessEvaluator.canViewLoan(guarantorUser, loan.getId()),
                                        "All guarantors should initially have access");

                        List<Loan> loans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());
                        assertEquals(1, loans.size(),
                                        "Each guarantor should initially see one loan");
                }

                // Act: Complete the loan
                loan.setStatus(LoanStatus.COMPLETED);
                loanRepository.save(loan);

                // Assert: Verify all guarantors lose access
                for (Member guarantor : guarantors) {
                        User guarantorUser = createTestUser(guarantor);
                        assertFalse(guarantorAccessEvaluator.canViewLoan(guarantorUser, loan.getId()),
                                        "All guarantors should lose access when loan is completed");

                        List<Loan> loans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());
                        assertTrue(loans.isEmpty(),
                                        "All guarantors should see no guaranteed loans after completion");
                }

                // Verify all guarantor relations are marked as inactive
                for (Guarantor guarantorRelation : guarantorRelations) {
                        Guarantor savedGuarantor = guarantorRepository.findById(guarantorRelation.getId()).orElse(null);
                        assertNotNull(savedGuarantor, "Guarantor relation should still exist");
                        assertFalse(savedGuarantor.getIsActive(),
                                        "All guarantor relations should be marked as inactive");
                }
        }

        /**
         * Property: Partial completion - only completed loans are revoked
         */
        @Property
        void partialCompletionOnlyRevokesCompletedLoans(@ForAll Random random) throws Exception {
                // Arrange: Create borrower and guarantor
                Member borrower = createTestMember("Borrower");
                Member guarantor = createTestMember("Guarantor");

                borrower = memberRepository.save(borrower);
                guarantor = memberRepository.save(guarantor);

                // Create multiple loans for same guarantor
                Loan activeLoan = new Loan();
                activeLoan.setMember(borrower);
                activeLoan.setPrincipalAmount(BigDecimal.valueOf(20000.0));
                activeLoan.setStartDate(LocalDate.now().minusMonths(6));
                activeLoan.setEndDate(LocalDate.now().plusMonths(6));
                activeLoan.setStatus(LoanStatus.ACTIVE);
                activeLoan = loanRepository.save(activeLoan);

                Loan completedLoan = new Loan();
                completedLoan.setMember(borrower);
                completedLoan.setPrincipalAmount(BigDecimal.valueOf(15000.0));
                completedLoan.setStartDate(LocalDate.now().minusMonths(12));
                completedLoan.setEndDate(LocalDate.now().minusMonths(1));
                completedLoan.setStatus(LoanStatus.ACTIVE);
                completedLoan = loanRepository.save(completedLoan);

                // Create guarantor relationships
                Guarantor activeGuarantor = new Guarantor();
                activeGuarantor.setLoan(activeLoan);
                activeGuarantor.setMember(guarantor);
                activeGuarantor.setGuaranteedAmount(BigDecimal.valueOf(20000.0));
                activeGuarantor.setGuaranteeStartDate(LocalDate.now());
                activeGuarantor.setIsActive(true);
                guarantorRepository.save(activeGuarantor);

                Guarantor completedGuarantor = new Guarantor();
                completedGuarantor.setLoan(completedLoan);
                completedGuarantor.setMember(guarantor);
                completedGuarantor.setGuaranteedAmount(BigDecimal.valueOf(15000.0));
                completedGuarantor.setGuaranteeStartDate(LocalDate.now());
                completedGuarantor.setIsActive(true);
                guarantorRepository.save(completedGuarantor);

                // Verify initial access to both loans
                User guarantorUser = createTestUser(guarantor);
                assertTrue(guarantorAccessEvaluator.canViewLoan(guarantorUser, activeLoan.getId()),
                                "Guarantor should have access to active loan");
                assertTrue(guarantorAccessEvaluator.canViewLoan(guarantorUser, completedLoan.getId()),
                                "Guarantor should have access to soon-to-be completed loan");

                List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());
                assertEquals(2, guaranteedLoans.size(),
                                "Guarantor should initially see two guaranteed loans");

                // Act: Complete only one loan
                completedLoan.setStatus(LoanStatus.COMPLETED);
                loanRepository.save(completedLoan);

                // Assert: Verify selective access revocation
                assertTrue(guarantorAccessEvaluator.canViewLoan(guarantorUser, activeLoan.getId()),
                                "Guarantor should still have access to active loan");
                assertFalse(guarantorAccessEvaluator.canViewLoan(guarantorUser, completedLoan.getId()),
                                "Guarantor should lose access to completed loan");

                List<Loan> guaranteedLoansAfterPartial = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());
                assertEquals(1, guaranteedLoansAfterPartial.size(),
                                "Guarantor should see only one guaranteed loan after partial completion");
                assertEquals(activeLoan.getId(), guaranteedLoansAfterPartial.get(0).getId(),
                                "Remaining loan should be the active one");
        }

        /**
         * Property: Non-guarantors never have access
         */
        @Property
        void nonGuarantorsNeverHaveAccess(@ForAll Random random) throws Exception {
                // Arrange: Create borrower and non-guarantor
                Member borrower = createTestMember("Borrower");
                Member nonGuarantor = createTestMember("NonGuarantor");

                borrower = memberRepository.save(borrower);
                nonGuarantor = memberRepository.save(nonGuarantor);

                // Create loan
                Loan loan = new Loan();
                loan.setMember(borrower);
                loan.setPrincipalAmount(BigDecimal.valueOf(30000.0));
                loan.setStartDate(LocalDate.now().minusMonths(6));
                loan.setEndDate(LocalDate.now().plusMonths(6));
                loan.setStatus(LoanStatus.ACTIVE);
                loan = loanRepository.save(loan);

                // Verify non-guarantor has no access initially
                User nonGuarantorUser = createTestUser(nonGuarantor);
                assertFalse(guarantorAccessEvaluator.canViewLoan(nonGuarantorUser, loan.getId()),
                                "Non-guarantor should never have access to loan");

                List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(nonGuarantor.getId());
                assertTrue(guaranteedLoans.isEmpty(),
                                "Non-guarantor should see no guaranteed loans");

                // Act: Complete the loan
                loan.setStatus(LoanStatus.COMPLETED);
                loanRepository.save(loan);

                // Assert: Verify non-guarantor still has no access
                assertFalse(guarantorAccessEvaluator.canViewLoan(nonGuarantorUser, loan.getId()),
                                "Non-guarantor should still have no access after loan completion");

                List<Loan> guaranteedLoansAfterCompletion = guarantorAccessEvaluator
                                .getGuaranteedLoans(nonGuarantor.getId());
                assertTrue(guaranteedLoansAfterCompletion.isEmpty(),
                                "Non-guarantor should still see no guaranteed loans after completion");
        }

        /**
         * Property: Access revocation timing
         */
        @Test
        void accessRevocationTiming() {
                // Arrange: Create borrower and guarantor
                Member borrower = createTestMember("Borrower");
                Member guarantor = createTestMember("Guarantor");

                borrower = memberRepository.save(borrower);
                guarantor = memberRepository.save(guarantor);

                // Create loan
                Loan loan = new Loan();
                loan.setMember(borrower);
                loan.setPrincipalAmount(BigDecimal.valueOf(40000.0));
                loan.setStartDate(LocalDate.now().minusMonths(9));
                loan.setEndDate(LocalDate.now().plusMonths(3));
                loan.setStatus(LoanStatus.ACTIVE);
                loan = loanRepository.save(loan);

                // Create guarantor relationship
                Guarantor guarantorRelation = new Guarantor();
                guarantorRelation.setLoan(loan);
                guarantorRelation.setMember(guarantor);
                guarantorRelation.setGuaranteedAmount(BigDecimal.valueOf(40000.0));
                guarantorRelation.setGuaranteeStartDate(LocalDate.now());
                guarantorRelation.setIsActive(true);
                guarantorRepository.save(guarantorRelation);

                // Create User object for testing
                User guarantorUser = createTestUser(guarantor);

                // Verify initial access
                assertTrue(guarantorAccessEvaluator.canViewLoan(guarantorUser, loan.getId()),
                                "Guarantor should have access to active loan");

                // Record timing
                long startTime = System.currentTimeMillis();

                // Act: Complete the loan
                loan.setStatus(LoanStatus.COMPLETED);
                loanRepository.save(loan);

                // Check access immediately after completion
                long endTime = System.currentTimeMillis();

                // Assert: Verify immediate revocation
                assertFalse(guarantorAccessEvaluator.canViewLoan(guarantorUser, loan.getId()),
                                "Access should be revoked immediately after loan completion");

                // Verify timing is reasonable (should be very fast)
                assertTrue((endTime - startTime) < 100, // Less than 100ms
                                "Access revocation should be immediate");

                // Verify guarantor relation is updated
                Guarantor savedGuarantor = guarantorRepository.findById(guarantorRelation.getId()).orElse(null);
                assertNotNull(savedGuarantor, "Guarantor relation should still exist");
                assertFalse(savedGuarantor.getIsActive(),
                                "Guarantor relation should be marked as inactive");
                assertEquals(LocalDate.now(), savedGuarantor.getGuaranteeEndDate(),
                                "Guarantee end date should be set to today");
        }

        private Member createTestMember(String name) {
                Member member = new Member();
                member.setName(name);
                member.setEmail(name.toLowerCase() + "@test.com");
                member.setPhone("1234567890");
                member.setAddress("123 Test Street");
                member.setIdCard("1234567890123");
                member.setDateOfBirth(LocalDate.of(1980, 1, 1));
                member.setRegistrationDate(LocalDate.now());
                return member;
        }

        private User createTestUser(Member member) {
                User user = new User();
                user.setId(member.getId());
                user.setMember(member);
                user.setUsername(member.getName().toLowerCase().replace(" ", "_"));
                return user;
        }
}
