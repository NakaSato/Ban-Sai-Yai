package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.SavingTransaction;
import com.bansaiyai.bansaiyai.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingTransactionRepository extends JpaRepository<SavingTransaction, Long> {

        @Query("SELECT st FROM SavingTransaction st ORDER BY st.transactionDate DESC, st.createdAt DESC")
        List<SavingTransaction> findRecentTransactions(Pageable pageable);

        List<SavingTransaction> findByApprovalStatus(ApprovalStatus approvalStatus);

        List<SavingTransaction> findBySavingAccountIdOrderByTransactionDateDesc(Long savingAccountId);

        @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SavingTransaction st WHERE st.transactionType = :type AND st.transactionDate BETWEEN :startDate AND :endDate")
        java.math.BigDecimal sumAmountByTypeAndDateRange(
                        @Param("type") com.bansaiyai.bansaiyai.entity.enums.TransactionType type,
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate);

        @Query("SELECT st FROM SavingTransaction st WHERE st.savingAccount.member.id = :memberId AND st.transactionDate BETWEEN :startDate AND :endDate ORDER BY st.transactionDate ASC")
        List<SavingTransaction> findByMemberAndDateRange(@Param("memberId") Long memberId,
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate);
}
