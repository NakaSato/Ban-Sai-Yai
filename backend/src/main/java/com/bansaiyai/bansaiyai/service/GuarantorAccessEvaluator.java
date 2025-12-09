package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Guarantor;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.repository.GuarantorRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for evaluating guarantor-based access control.
 * Implements relationship-based access control for guarantors to view loans
 * they guarantee.
 */
@Service
public class GuarantorAccessEvaluator {

    @Autowired
    private GuarantorRepository guarantorRepository;

    @Autowired
    private LoanRepository loanRepository;

    /**
     * Check if a user can view a specific loan.
     * Access is granted if:
     * 1. The user is the borrower (loan owner)
     * 2. The user is an active guarantor for the loan
     *
     * @param user   the user attempting to access the loan
     * @param loanId the ID of the loan to access
     * @return true if the user can view the loan, false otherwise
     */
    public boolean canViewLoan(User user, Long loanId) {
        if (user == null || user.getMember() == null || loanId == null) {
            return false;
        }

        // Fetch the loan
        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan == null) {
            return false;
        }

        // Check if user is the borrower
        if (loan.getMember() != null &&
                user.getMember().getId().equals(loan.getMember().getId())) {
            return true;
        }

        // Check if user is an active guarantor for this loan
        // AND the loan is still active (not completed or written off)
        LoanStatus status = loan.getStatus();
        if (status == LoanStatus.COMPLETED || status == LoanStatus.WRITTEN_OFF) {
            return false;
        }

        return isActiveGuarantor(user.getMember().getId(), loanId);
    }

    /**
     * Get all loans guaranteed by a specific member.
     * Only returns loans where the member is an active guarantor.
     *
     * @param memberId the ID of the member
     * @return list of loans guaranteed by the member
     */
    public List<Loan> getGuaranteedLoans(Long memberId) {
        if (memberId == null) {
            return List.of();
        }

        // Get all active guarantor relationships for this member
        List<Guarantor> guarantors = guarantorRepository.findActiveByMemberId(memberId);

        // Extract and return the loans
        return guarantors.stream()
                .map(Guarantor::getLoan)
                .filter(loan -> loan != null)
                .filter(loan -> {
                    // Only include loans that are not completed or written off
                    LoanStatus status = loan.getStatus();
                    return status != LoanStatus.COMPLETED &&
                            status != LoanStatus.WRITTEN_OFF;
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if a member is an active guarantor for a specific loan.
     *
     * @param memberId the ID of the member
     * @param loanId   the ID of the loan
     * @return true if the member is an active guarantor, false otherwise
     */
    public boolean isActiveGuarantor(Long memberId, Long loanId) {
        if (memberId == null || loanId == null) {
            return false;
        }

        return guarantorRepository.existsActiveByLoanIdAndMemberId(loanId, memberId);
    }
}
