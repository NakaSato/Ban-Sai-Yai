package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.SavingTransaction;
import com.bansaiyai.bansaiyai.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingTransactionRepository extends JpaRepository<SavingTransaction, Long> {

    @Query("SELECT st FROM SavingTransaction st ORDER BY st.transactionDate DESC, st.createdAt DESC")
    List<SavingTransaction> findRecentTransactions(Pageable pageable);

    List<SavingTransaction> findByApprovalStatus(ApprovalStatus approvalStatus);

    List<SavingTransaction> findBySavingAccountIdOrderByTransactionDateDesc(Long savingAccountId);
}
