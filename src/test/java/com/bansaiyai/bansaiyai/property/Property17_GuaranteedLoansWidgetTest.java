package com.bansaiyai.bansaiyai.property;

import com.bansaiyai.bansaiyai.entity.Guarantor;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.LoanStatus;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
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
 * Property 17: Guaranteed loans widget accuracy
 * Validates: Requirements 7.2
 * 
 * This property test ensures that the guaranteed loans widget
 * accurately displays all loans that a member has guaranteed,
 * with correct borrower names and loan amounts.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Property17_GuaranteedLoansWidgetTest {

  @Autowired
  private GuarantorRepository guarantorRepository;

  @Autowired
  private LoanRepository loanRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private GuarantorAccessEvaluator guarantorAccessEvaluator;

  /**
   * Property: Guaranteed loans widget shows all guaranteed loans
   */
  @Property
  void guaranteedLoansWidgetShowsAllGuaranteedLoans(@ForAll("loans", loans()) {
    // Arrange: Create multiple loans with different guarantors
    Member borrower = createTestMember("Borrower");
    Member guarantor1 = createTestMember("Guarantor1");
    Member guarantor2 = createTestMember("Guarantor2");
    Member nonGuarantor = createTestMember("NonGuarantor");

    borrower = memberRepository.save(borrower);
    guarantor1 = memberRepository.save(guarantor1);
    guarantor2 = memberRepository.save(guarantor2);
    nonGuarantor = memberRepository.save(nonGuarantor);

    // Create loans with different guarantors
    for (int i = 0; i < loans.size(); i++) {
      Loan loan = loans.get(i);
      loan.setBorrowerMember(borrower);
      loan.setIssueDate(LocalDate.now().minusMonths(i));
      loan.setLoanStatus(LoanStatus.ACTIVE);
      loan = loanRepository.save(loan);

      // Alternate guarantors for different loans
      Member currentGuarantor = (i % 2 == 0) ? guarantor1 : guarantor2;
      Guarantor guarantor = new Guarantor();
      guarantor.setLoan(loan);
      guarantor.setMember(currentGuarantor);
      guarantor.setGuaranteeDate(LocalDate.now().minusMonths(i));
      guarantorRepository.save(guarantor);
    }

    // Act: Get guaranteed loans for each guarantor
    List<Loan> guarantor1Loans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor1.getId());
    List<Loan> guarantor2Loans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor2.getId());
    List<Loan> nonGuarantorLoans = guarantorAccessEvaluator.getGuaranteedLoans(nonGuarantor.getId());

    // Assert: Verify accuracy
    long expectedGuarantor1Loans = loans.size() / 2 + (loans.size() % 2);
    long expectedGuarantor2Loans = loans.size() / 2;
    
    assertEquals(expectedGuarantor1Loans, guarantor1Loans.size(), 
        "Guarantor1 should see correct number of guaranteed loans");
    assertEquals(expectedGuarantor2Loans, guarantor2Loans.size(), 
        "Guarantor2 should see correct number of guaranteed loans");
    assertTrue(nonGuarantorLoans.isEmpty(), 
        "Non-guarantor should see no guaranteed loans");

    // Assert: Verify loan details accuracy
    for (Loan guaranteedLoan : guarantor1Loans) {
      assertEquals(borrower.getId(), guaranteedLoan.getBorrowerMember().getId(),
          "Should show correct borrower for guaranteed loan");
      assertNotNull(guaranteedLoan.getPrincipalAmount(),
          "Should show loan amount for guaranteed loan");
      assertTrue(loans.contains(guaranteedLoan),
          "Should only show loans that were actually guaranteed");
    }
  }

  /**
   * Property: Widget displays correct borrower information
   */
  @Property
  void widgetDisplaysCorrectBorrowerInformation(@ForAll Random random) throws Exception {
    // Arrange: Create borrower and guarantor
    Member borrower = createTestMember("Borrower_" + random.nextInt(1000));
    Member guarantor = createTestMember("Guarantor_" + random.nextInt(1000));

    borrower = memberRepository.save(borrower);
    guarantor = memberRepository.save(guarantor);

    // Create loan with specific details
    Loan loan = new Loan();
    loan.setBorrowerMember(borrower);
    loan.setPrincipalAmount(BigDecimal.valueOf(random.nextDouble(1000.0, 50000.0)));
    loan.setIssueDate(LocalDate.now().minusDays(random.nextInt(30)));
    loan.setLoanStatus(LoanStatus.ACTIVE);
    loan = loanRepository.save(loan);

    // Create guarantor relationship
    Guarantor guarantorRelation = new Guarantor();
    guarantorRelation.setLoan(loan);
    guarantorRelation.setMember(guarantor);
    guarantorRelation.setGuaranteeDate(LocalDate.now());
    guarantorRepository.save(guarantorRelation);

    // Act: Get guaranteed loans
    List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());

    // Assert: Verify borrower information
    assertEquals(1, guaranteedLoans.size(), "Should show exactly one guaranteed loan");

    Loan guaranteedLoan = guaranteedLoans.get(0);
    assertEquals(borrower.getFullName(), guaranteedLoan.getBorrowerMember().getFullName(),
        "Should display correct borrower full name");
    assertEquals(borrower.getId(), guaranteedLoan.getBorrowerMember().getId(),
        "Should display correct borrower ID");
    assertEquals(loan.getPrincipalAmount(), guaranteedLoan.getPrincipalAmount(),
        "Should display correct loan amount");
    assertEquals(loan.getIssueDate(), guaranteedLoan.getIssueDate(),
        "Should display correct loan date");
  }

  /**
   * Property: Widget handles multiple loans for same borrower
   */
  @Property
  void widgetHandlesMultipleLoansForSameBorrower(@ForAll Random random) throws Exception {
    // Arrange: Create borrower and guarantor
    Member borrower = createTestMember("Borrower");
    Member guarantor = createTestMember("Guarantor");

    borrower = memberRepository.save(borrower);
    guarantor = memberRepository.save(guarantor);

    // Create multiple loans for same borrower
    int loanCount = random.nextInt(1, 5); // 1 to 4 loans
    for (int i = 0; i < loanCount; i++) {
      Loan loan = new Loan();
      loan.setBorrowerMember(borrower);
      loan.setPrincipalAmount(BigDecimal.valueOf(5000.0 + (i * 1000)));
      loan.setIssueDate(LocalDate.now().minusMonths(i));
      loan.setLoanStatus(LoanStatus.ACTIVE);
      loan = loanRepository.save(loan);

      // Create guarantor relationship for each loan
      Guarantor guarantorRelation = new Guarantor();
      guarantorRelation.setLoan(loan);
      guarantorRelation.setMember(guarantor);
      guarantorRelation.setGuaranteeDate(LocalDate.now().minusMonths(i));
      guarantorRepository.save(guarantorRelation);
    }

    // Act: Get guaranteed loans
    List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());

    // Assert: Verify all loans are shown
    assertEquals(loanCount, guaranteedLoans.size(),
        "Should show all guaranteed loans for same borrower");

    // Assert: Verify all loans have correct borrower
    for (Loan guaranteedLoan : guaranteedLoans) {
      assertEquals(borrower.getId(), guaranteedLoan.getBorrowerMember().getId(),
          "All loans should show same borrower");
      assertTrue(guaranteedLoan.getPrincipalAmount().compareTo(BigDecimal.ZERO) > 0,
          "All loans should have valid amounts");
    }
  }

  /**
   * Property: Widget excludes completed loans
   */
  @Property
  void widgetExcludesCompletedLoans(@ForAll Random random) throws Exception {
    // Arrange: Create borrower and guarantor
    Member borrower = createTestMember("Borrower");
    Member guarantor = createTestMember("Guarantor");

    borrower = memberRepository.save(borrower);
    guarantor = memberRepository.save(guarantor);

    // Create mix of active and completed loans
    int totalLoans = 4;
    for (int i = 0; i < totalLoans; i++) {
      Loan loan = new Loan();
      loan.setBorrowerMember(borrower);
      loan.setPrincipalAmount(BigDecimal.valueOf(10000.0 + (i * 1000)));
      loan.setIssueDate(LocalDate.now().minusMonths(i + 1));

      // Alternate between active and completed loans
      loan.setLoanStatus((i % 2 == 0) ? LoanStatus.ACTIVE : LoanStatus.COMPLETED);
      if (loan.getLoanStatus() == LoanStatus.COMPLETED) {
        loan.setCompletionDate(LocalDate.now().minusMonths(1));
      }
      loan = loanRepository.save(loan);

      // Create guarantor relationship only for active loans
      if (loan.getLoanStatus() == LoanStatus.ACTIVE) {
        Guarantor guarantorRelation = new Guarantor();
        guarantorRelation.setLoan(loan);
        guarantorRelation.setMember(guarantor);
        guarantorRelation.setGuaranteeDate(LocalDate.now().minusMonths(i));
        guarantorRepository.save(guarantorRelation);
      }
    }

    // Act: Get guaranteed loans
    List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());

    // Assert: Should only show active loans
    long expectedActiveLoans = totalLoans / 2;
    assertEquals(expectedActiveLoans, guaranteedLoans.size(),
        "Should only show active guaranteed loans");

    for (Loan guaranteedLoan : guaranteedLoans) {
      assertEquals(LoanStatus.ACTIVE, guaranteedLoan.getLoanStatus(),
          "All shown loans should be active");
      assertNull(guaranteedLoan.getCompletionDate(),
          "Shown loans should not have completion date");
    }
  }

  /**
   * Property: Widget accuracy with no guaranteed loans
   */
  @Test
  void widgetAccuracyWithNoGuaranteedLoans() {
    // Arrange: Create member with no guaranteed loans
    Member member = createTestMember("NoGuarantees");
    member = memberRepository.save(member);

    // Act: Get guaranteed loans
    List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(member.getId());

    // Assert: Should return empty list
    assertTrue(guaranteedLoans.isEmpty(),
        "Member with no guaranteed loans should see empty list");
  }

  /**
   * Property: Widget performance with large dataset
   */
  @Property
  void widgetPerformanceWithLargeDataset(@ForAll Random random) throws Exception {
    // Arrange: Create member and many guaranteed loans
    Member guarantor = createTestMember("HeavyGuarantor");
    guarantor = memberRepository.save(guarantor);

    // Create many loans
    int loanCount = random.nextInt(10, 50); // 10 to 49 loans
    for (int i = 0; i < loanCount; i++) {
      Member borrower = createTestMember("Borrower" + i);
      borrower = memberRepository.save(borrower);

      Loan loan = new Loan();
      loan.setBorrowerMember(borrower);
      loan.setPrincipalAmount(BigDecimal.valueOf(5000.0 + random.nextDouble(0.0, 20000.0)));
      loan.setIssueDate(LocalDate.now().minusDays(random.nextInt(365)));
      loan.setLoanStatus(LoanStatus.ACTIVE);
      loan = loanRepository.save(loan);

      Guarantor guarantorRelation = new Guarantor();
      guarantorRelation.setLoan(loan);
      guarantorRelation.setMember(guarantor);
      guarantorRelation.setGuaranteeDate(LocalDate.now().minusDays(random.nextInt(365)));
      guarantorRepository.save(guarantorRelation);
    }

    // Act: Get guaranteed loans with performance check
    long startTime = System.currentTimeMillis();
    List<Loan> guaranteedLoans = guarantorAccessEvaluator.getGuaranteedLoans(guarantor.getId());
    long endTime = System.currentTimeMillis();

    // Assert: Verify accuracy and performance
    assertEquals(loanCount, guaranteedLoans.size(),
        "Should show all guaranteed loans even with large dataset");
    assertTrue((endTime - startTime) < 5000, // 5 seconds max
        "Widget should perform well even with large dataset");
  }

  private Member createTestMember(String name) {
    Member member = new Member();
    member.setFullName(name);
    member.setFullName(name);
    member.setEmail(name.toLowerCase() + "@test.com");
    member.setPhoneNumber("123-456-" + name.hashCode() % 10000);
    member.setMembershipNumber("M" + System.currentTimeMillis() % 100000);
    member.setJoinDate(LocalDate.now());
    return member;
  }

  /**
   * Custom arbitrary for generating loans
   */
  @Provide
  Arbitrary<List<Loan>> loans() {
    return Arbitraries.listOf(
        Arbitraries.lazy(() -> createTestLoan())).ofMinSize(1).ofMaxSize(5);
  }

  private Loan createTestLoan() {
    Loan loan = new Loan();
    loan.setPrincipalAmount(BigDecimal.valueOf(10000.0 + Math.random() * 40000.0));
    loan.setIssueDate(LocalDate.now().minusDays((int) (Math.random() * 365)));
    loan.setLoanStatus(LoanStatus.ACTIVE);
    return loan;
  }
}
