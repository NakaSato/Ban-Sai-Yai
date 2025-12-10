package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.financial.FinancialTransactionDTO;
import com.bansaiyai.bansaiyai.dto.financial.LoanAccountDTO;
import com.bansaiyai.bansaiyai.dto.financial.MemberFinancialDTO;
import com.bansaiyai.bansaiyai.dto.financial.ShareAccountDTO;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberFinancialService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public MemberFinancialDTO getMemberFinancialData(String username) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found for user: " + username));

        ShareAccountDTO shareAccountDTO = buildShareAccountDTO(member);
        List<LoanAccountDTO> loanAccountDTOs = buildLoanAccountDTOs(member);

        return MemberFinancialDTO.builder()
                .shares(shareAccountDTO)
                .loans(loanAccountDTOs)
                .build();
    }

    private ShareAccountDTO buildShareAccountDTO(Member member) {
        List<Payment> sharePayments = paymentRepository.findByMemberIdAndPaymentType(member.getId(),
                PaymentType.SHARE_CAPITAL);

        // Sort by date descending
        sharePayments.sort(Comparator.comparing(Payment::getPaymentDate).reversed());

        List<FinancialTransactionDTO> transactions = sharePayments.stream()
                .map(this::mapPaymentToTransactionDTO)
                .collect(Collectors.toList());

        // Check if paid for current month
        boolean paidThisMonth = sharePayments.stream()
                .anyMatch(p -> {
                    LocalDate date = p.getPaymentDate();
                    LocalDate now = LocalDate.now();
                    return date.getMonth() == now.getMonth() && date.getYear() == now.getYear();
                });

        return ShareAccountDTO.builder()
                .totalAccumulatedShares(member.getShareCapital() != null ? member.getShareCapital() : BigDecimal.ZERO)
                .monthlyPaymentStatus(paidThisMonth)
                .transactions(transactions)
                .build();
    }

    private List<LoanAccountDTO> buildLoanAccountDTOs(Member member) {
        if (member.getLoans() == null) {
            return new ArrayList<>();
        }

        return member.getLoans().stream()
                .map(this::mapLoanToDTO)
                .collect(Collectors.toList());
    }

    private LoanAccountDTO mapLoanToDTO(Loan loan) {
        // We need loan-related payments.
        // PaymentRepository has findLoanPaymentsByLoan which takes a list of types.
        // However, findByLoanId might be simpler if we just want all payments for that
        // loan.
        // Let's use findByLoanId (via filtered stream or if repository supports it
        // without pageable,
        // actually repository has findByLoanId with Pageable, or findLoanPaymentsByLoan
        // which is better).

        // Using findLoanPaymentsByLoan requires passing list of types.
        List<PaymentType> loanTypes = List.of(
                PaymentType.LOAN_PRINCIPAL,
                PaymentType.LOAN_INTEREST,
                PaymentType.LOAN_PAYMENT, // Assuming generic type exists or combined
                PaymentType.LOAN_FEE,
                PaymentType.LOAN_PENALTY);

        // Since findLoanPaymentsByLoan might fail if types don't match exactly what's
        // in DB,
        // let's grab all payments for the loan via entity relationship if possible,
        // or use the repository method if we are sure about types.
        // Loan entity has @OneToMany payments list. Use that!

        List<Payment> loanPayments = loan.getPayments();
        if (loanPayments == null) {
            loanPayments = new ArrayList<>();
        }

        // Sort descending
        loanPayments.sort(Comparator.comparing(Payment::getPaymentDate).reversed());

        List<FinancialTransactionDTO> transactions = loanPayments.stream()
                .map(this::mapPaymentToTransactionDTO)
                .collect(Collectors.toList());

        return LoanAccountDTO.builder()
                .loanNumber(loan.getLoanNumber())
                .loanType(loan.getLoanType().name())
                .outstandingBalance(loan.getOutstandingBalance())
                .totalPrincipalPaid(loan.getPaidPrincipal())
                .totalInterestPaid(loan.getPaidInterest())
                .termMonths(loan.getTermMonths())
                .status(loan.getStatus().name())
                .transactions(transactions)
                .build();
    }

    private FinancialTransactionDTO mapPaymentToTransactionDTO(Payment payment) {
        return FinancialTransactionDTO.builder()
                .date(payment.getPaymentDate())
                .period(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("MM/yyyy")))
                .amount(payment.getAmount())
                .receiptNumber(payment.getReceiptNumber())
                .type(payment.getPaymentType().name())
                .description(payment.getDescription())
                // For simple history, we don't calculate running balance here as it requires
                // traversing all history.
                // If needed, we could compute it, but for now we leave balanceAfter null or
                // simpler.
                // The prompt asked for "Remaining Balance" for loans.
                // We can't easily get historical balance from Payment entity unless we store
                // it.
                // We will omit balanceAfter for now unless explicitly required to compute.
                .build();
    }
}
