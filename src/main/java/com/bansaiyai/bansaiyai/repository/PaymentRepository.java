package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.enums.PaymentStatus;
import com.bansaiyai.bansaiyai.entity.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity.
 * Provides database operations for payment management and reporting.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

        /**
         * Sums revenue (interest, fees, penalties) for completed payments within a date
         * range.
         */
        @Query("SELECT SUM(COALESCE(p.interestAmount, 0) + COALESCE(p.feeAmount, 0) + COALESCE(p.penaltyAmount, 0)) " +
                        "FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.paymentStatus = 'COMPLETED'")
        BigDecimal sumRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        /**
         * Find payment by payment number
         */
        Optional<Payment> findByPaymentNumber(String paymentNumber);

        /**
         * Find payments by member
         */
        Page<Payment> findByMemberId(Long memberId, Pageable pageable);

        /**
         * Find payments by loan
         */
        Page<Payment> findByLoanId(Long loanId, Pageable pageable);

        /**
         * Find payments by savings account
         */
        Page<Payment> findBySavingAccountId(Long savingAccountId, Pageable pageable);

        /**
         * Find payments by status
         */
        Page<Payment> findByPaymentStatus(PaymentStatus status, Pageable pageable);

        /**
         * Find payments by type
         */
        Page<Payment> findByPaymentType(PaymentType type, Pageable pageable);

        /**
         * Find payments by member and status
         */
        Page<Payment> findByMemberIdAndPaymentStatus(Long memberId, PaymentStatus status, Pageable pageable);

        /**
         * Find payments by loan and status
         */
        List<Payment> findByLoanIdAndPaymentStatus(Long loanId, PaymentStatus status);

        /**
         * Find overdue payments
         */
        @Query("SELECT p FROM Payment p WHERE p.dueDate < :currentDate AND p.paymentStatus NOT IN (:completedStatuses)")
        List<Payment> findOverduePayments(@Param("currentDate") LocalDate currentDate,
                        @Param("completedStatuses") List<PaymentStatus> completedStatuses);

        /**
         * Find pending payments requiring processing
         */
        @Query("SELECT p FROM Payment p WHERE p.paymentStatus IN (:pendingStatuses) ORDER BY p.paymentDate ASC")
        List<Payment> findPendingPayments(@Param("pendingStatuses") List<PaymentStatus> pendingStatuses);

        /**
         * Find payments due within date range
         */
        @Query("SELECT p FROM Payment p WHERE p.dueDate BETWEEN :startDate AND :endDate ORDER BY p.dueDate ASC")
        List<Payment> findPaymentsDueBetween(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find payments by date range
         */
        @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
        Page<Payment> findPaymentsByDateRange(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate, Pageable pageable);

        /**
         * Find payments by member and date range
         */
        @Query("SELECT p FROM Payment p WHERE p.member.id = :memberId AND p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
        Page<Payment> findPaymentsByMemberAndDateRange(@Param("memberId") Long memberId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        /**
         * Find loan payments by loan
         */
        @Query("SELECT p FROM Payment p WHERE p.loan.id = :loanId AND p.paymentType IN (:loanPaymentTypes) ORDER BY p.paymentDate DESC")
        List<Payment> findLoanPaymentsByLoan(@Param("loanId") Long loanId,
                        @Param("loanPaymentTypes") List<PaymentType> loanPaymentTypes);

        /**
         * Find payments by payment method
         */
        Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);

        /**
         * Find verified but not processed payments
         */
        @Query("SELECT p FROM Payment p WHERE p.isVerified = true AND p.paymentStatus = :status")
        List<Payment> findVerifiedPaymentsByStatus(@Param("status") PaymentStatus status);

        /**
         * Find failed payments
         */
        @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :failedStatus ORDER BY p.updatedAt DESC")
        List<Payment> findFailedPayments(@Param("failedStatus") PaymentStatus failedStatus);

        /**
         * Find payments requiring attention
         */
        @Query("SELECT p FROM Payment p WHERE p.paymentStatus IN (:attentionStatuses) ORDER BY p.dueDate ASC")
        List<Payment> findPaymentsRequiringAttention(@Param("attentionStatuses") List<PaymentStatus> attentionStatuses);

        /**
         * Count payments by status
         */
        @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = :status")
        Long countByPaymentStatus(@Param("status") PaymentStatus status);

        /**
         * Count payments by member and status
         */
        @Query("SELECT COUNT(p) FROM Payment p WHERE p.member.id = :memberId AND p.paymentStatus = :status")
        Long countByMemberAndStatus(@Param("memberId") Long memberId, @Param("status") PaymentStatus status);

        /**
         * Sum payments by type and date range
         */
        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = :type AND p.paymentDate BETWEEN :startDate AND :endDate")
        BigDecimal sumPaymentsByTypeAndDateRange(@Param("type") PaymentType type,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        /**
         * Sum payments by member and type
         */
        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.member.id = :memberId AND p.paymentType = :type AND p.paymentStatus = :status")
        BigDecimal sumPaymentsByMemberAndType(@Param("memberId") Long memberId, @Param("type") PaymentType type,
                        @Param("status") PaymentStatus status);

        /**
         * Sum loan payments for a loan
         */
        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.loan.id = :loanId AND p.paymentType IN (:paymentTypes) AND p.paymentStatus = :status")
        BigDecimal sumLoanPayments(@Param("loanId") Long loanId, @Param("paymentTypes") List<PaymentType> paymentTypes,
                        @Param("status") PaymentStatus status);

        /**
         * Get daily payment totals
         */
        @Query("SELECT p.paymentDate, COUNT(p), COALESCE(SUM(p.amount), 0) " +
                        "FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY p.paymentDate ORDER BY p.paymentDate DESC")
        List<Object[]> getDailyPaymentTotals(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Get monthly payment totals by type
         */
        @Query("SELECT p.paymentType, YEAR(p.paymentDate), MONTH(p.paymentDate), COUNT(p), COALESCE(SUM(p.amount), 0) "
                        +
                        "FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY p.paymentType, YEAR(p.paymentDate), MONTH(p.paymentDate) " +
                        "ORDER BY YEAR(p.paymentDate), MONTH(p.paymentDate), p.paymentType")
        List<Object[]> getMonthlyPaymentTotalsByType(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find payments by reference number
         */
        List<Payment> findByReferenceNumber(String referenceNumber);

        /**
         * Find payments by receipt number
         */
        Optional<Payment> findByReceiptNumber(String receiptNumber);

        /**
         * Find recurring payments
         */
        @Query("SELECT p FROM Payment p WHERE p.isRecurring = true ORDER BY p.recurringEndDate DESC")
        Page<Payment> findRecurringPayments(Pageable pageable);

        /**
         * Find auto-debit payments
         */
        @Query("SELECT p FROM Payment p WHERE p.autoDebit = true AND p.paymentStatus IN (:statuses)")
        List<Payment> findAutoDebitPayments(@Param("statuses") List<PaymentStatus> statuses);

        /**
         * Find payments with failed transactions
         */
        @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status AND p.failedReason IS NOT NULL ORDER BY p.updatedAt DESC")
        List<Payment> findPaymentsWithFailures(@Param("status") PaymentStatus status);

        /**
         * Get payment statistics for a member
         */
        @Query("SELECT p.paymentType, COUNT(p), COALESCE(SUM(p.amount), 0) " +
                        "FROM Payment p WHERE p.member.id = :memberId AND p.paymentDate BETWEEN :startDate AND :endDate "
                        +
                        "GROUP BY p.paymentType")
        List<Object[]> getMemberPaymentStatistics(@Param("memberId") Long memberId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        /**
         * Find payments updated since
         */
        @Query("SELECT p FROM Payment p WHERE p.updatedAt > :since ORDER BY p.updatedAt DESC")
        List<Payment> findPaymentsUpdatedSince(@Param("since") LocalDateTime since);

        /**
         * Check if payment number exists
         */
        boolean existsByPaymentNumber(String paymentNumber);

        /**
         * Check if reference number exists
         */
        boolean existsByReferenceNumber(String referenceNumber);

        /**
         * Check if receipt number exists
         */
        boolean existsByReceiptNumber(String receiptNumber);

        // Dashboard query methods
        Long countByPaymentStatusIn(List<PaymentStatus> statuses);

        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
        java.math.BigDecimal sumPaymentsByDateRange(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        List<Payment> findTop10ByMemberIdOrderByPaymentDateDesc(Long memberId);

        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.member.id = :memberId AND p.paymentDate BETWEEN :startDate AND :endDate")
        java.math.BigDecimal sumPaymentsByMemberAndDateRange(@Param("memberId") Long memberId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentDate = :date")
        Long countByProcessedDate(@Param("date") LocalDate date);

        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.verifiedBy = :officer AND p.paymentDate BETWEEN :startDate AND :endDate")
        java.math.BigDecimal sumPaymentsByProcessedByAndDateRange(@Param("officer") String officer,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
