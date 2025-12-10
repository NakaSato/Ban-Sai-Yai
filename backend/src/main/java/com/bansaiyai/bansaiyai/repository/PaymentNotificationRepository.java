package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.PaymentNotification;
import com.bansaiyai.bansaiyai.entity.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentNotification entity
 */
@Repository
public interface PaymentNotificationRepository extends JpaRepository<PaymentNotification, Long> {

        /**
         * Find all notifications by status, ordered by creation date (oldest first)
         */
        List<PaymentNotification> findByStatusOrderByCreatedAtAsc(NotificationStatus status);

        /**
         * Find all pending notifications (most common query)
         */
        @Query("SELECT pn FROM PaymentNotification pn " +
                        "WHERE pn.status = 'PENDING' " +
                        "ORDER BY pn.createdAt ASC")
        List<PaymentNotification> findPendingNotifications();

        /**
         * Find all notifications for a specific member
         */
        @Query("SELECT pn FROM PaymentNotification pn " +
                        "WHERE pn.member.id = :memberId " +
                        "ORDER BY pn.createdAt DESC")
        List<PaymentNotification> findByMemberId(@Param("memberId") Long memberId);

        /**
         * Find notifications for a specific member with status filter
         */
        @Query("SELECT pn FROM PaymentNotification pn " +
                        "WHERE pn.member.id = :memberId " +
                        "AND pn.status = :status " +
                        "ORDER BY pn.createdAt DESC")
        List<PaymentNotification> findByMemberIdAndStatus(
                        @Param("memberId") Long memberId,
                        @Param("status") NotificationStatus status);

        /**
         * Find all notifications for a specific loan
         */
        List<PaymentNotification> findByLoanIdOrderByCreatedAtDesc(Long loanId);

        /**
         * Find pending notifications for a specific loan
         */
        @Query("SELECT pn FROM PaymentNotification pn " +
                        "WHERE pn.loan.id = :loanId " +
                        "AND pn.status = 'PENDING'")
        List<PaymentNotification> findPendingByLoanId(@Param("loanId") Long loanId);

        /**
         * Check if member has pending notification for the same loan within time window
         * (Duplicate detection)
         */
        @Query("SELECT COUNT(pn) > 0 FROM PaymentNotification pn " +
                        "WHERE pn.member.id = :memberId " +
                        "AND pn.loan.id = :loanId " +
                        "AND pn.status = 'PENDING' " +
                        "AND pn.createdAt > :since")
        boolean hasPendingNotificationForLoan(
                        @Param("memberId") Long memberId,
                        @Param("loanId") Long loanId,
                        @Param("since") LocalDateTime since);

        /**
         * Count pending notifications for a member (for rate limiting)
         */
        @Query("SELECT COUNT(pn) FROM PaymentNotification pn " +
                        "WHERE pn.member.id = :memberId " +
                        "AND pn.status = 'PENDING' " +
                        "AND pn.createdAt > :since")
        long countPendingByMemberSince(
                        @Param("memberId") Long memberId,
                        @Param("since") LocalDateTime since);

        /**
         * Find overdue pending notifications (pending for more than specified days)
         */
        @Query("SELECT pn FROM PaymentNotification pn " +
                        "WHERE pn.status = 'PENDING' " +
                        "AND pn.createdAt < :before " +
                        "ORDER BY pn.createdAt ASC")
        List<PaymentNotification> findOverduePending(@Param("before") LocalDateTime before);

        /**
         * Count notifications by status
         */
        long countByStatus(NotificationStatus status);

        /**
         * Find notifications created within date range
         */
        @Query("SELECT pn FROM PaymentNotification pn " +
                        "WHERE pn.createdAt BETWEEN :startDate AND :endDate " +
                        "ORDER BY pn.createdAt DESC")
        List<PaymentNotification> findByCreatedAtBetween(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find notifications approved by a specific officer
         */
        List<PaymentNotification> findByApprovedByUserIdOrderByApprovedAtDesc(Long userId);

        /**
         * Find notifications rejected by a specific officer
         */
        List<PaymentNotification> findByRejectedByUserIdOrderByRejectedAtDesc(Long userId);

        /**
         * Get statistics for dashboard
         */
        @Query("SELECT " +
                        "COUNT(CASE WHEN pn.status = 'PENDING' THEN 1 END) as pending, " +
                        "COUNT(CASE WHEN pn.status = 'APPROVED' THEN 1 END) as approved, " +
                        "COUNT(CASE WHEN pn.status = 'REJECTED' THEN 1 END) as rejected " +
                        "FROM PaymentNotification pn " +
                        "WHERE pn.createdAt > :since")
        Object[] getStatisticsSince(@Param("since") LocalDateTime since);

        /**
         * Find notification by payment reference
         */
        Optional<PaymentNotification> findByPaymentId(Long paymentId);

        /**
         * Find notification by receipt reference
         */
        Optional<PaymentNotification> findByReceiptId(Long receiptId);

        /**
         * Find payment notification by UUID (for API endpoints)
         */
        Optional<PaymentNotification> findByUuid(java.util.UUID uuid);

        /**
         * Check if notification exists by UUID
         */
        boolean existsByUuid(java.util.UUID uuid);
}
