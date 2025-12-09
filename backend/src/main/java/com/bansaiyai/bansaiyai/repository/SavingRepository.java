package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.SavingAccount;
import com.bansaiyai.bansaiyai.entity.SavingTransaction;
import com.bansaiyai.bansaiyai.entity.SavingBalance;
import com.bansaiyai.bansaiyai.entity.enums.AccountType;
import com.bansaiyai.bansaiyai.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavingRepository extends JpaRepository<SavingAccount, Long> {

        Optional<SavingAccount> findByAccountNumber(String accountNumber);

        Page<SavingAccount> findByMemberId(Long memberId, Pageable pageable);

        Page<SavingAccount> findByMemberIdAndIsActive(Long memberId, Boolean isActive, Pageable pageable);

        Page<SavingAccount> findByAccountType(AccountType accountType, Pageable pageable);

        Page<SavingAccount> findByAccountTypeAndMemberId(AccountType accountType, Long memberId, Pageable pageable);

        List<SavingAccount> findByMemberIdAndIsActive(Long memberId, Boolean isActive);

        @Query("SELECT sa FROM SavingAccount sa WHERE sa.member.id = :memberId AND sa.isActive = true")
        List<SavingAccount> findActiveAccountsByMemberId(@Param("memberId") Long memberId);

        @Query("SELECT sa FROM SavingAccount sa WHERE sa.isActive = true AND sa.isFrozen = false")
        Page<SavingAccount> findActiveAccounts(Pageable pageable);

        @Query("SELECT SUM(sa.balance) FROM SavingAccount sa WHERE sa.member.id = :memberId AND sa.isActive = true")
        BigDecimal sumBalancesByMemberId(@Param("memberId") Long memberId);

        @Query("SELECT COUNT(sa) FROM SavingAccount sa WHERE sa.member.id = :memberId AND sa.isActive = true")
        Long countActiveAccountsByMemberId(@Param("memberId") Long memberId);

        @Query("SELECT sa FROM SavingAccount sa WHERE sa.balance < sa.minimumBalance AND sa.isActive = true")
        List<SavingAccount> findAccountsBelowMinimumBalance();

        @Query("SELECT sa FROM SavingAccount sa WHERE sa.lastInterestDate < :date AND sa.isActive = true")
        List<SavingAccount> findAccountsRequiringInterest(@Param("date") LocalDate date);

        // Transaction queries
        @Query("SELECT st FROM SavingTransaction st WHERE st.savingAccount.id = :savingAccountId ORDER BY st.transactionDate DESC")
        Page<SavingTransaction> findBySavingAccountIdOrderByTransactionDateDesc(
                        @Param("savingAccountId") Long savingAccountId, Pageable pageable);

        @Query("SELECT st FROM SavingTransaction st WHERE st.savingAccount.id = :savingAccountId AND st.transactionDate BETWEEN :startDate AND :endDate")
        List<SavingTransaction> findBySavingAccountIdAndTransactionDateBetween(
                        @Param("savingAccountId") Long savingAccountId, @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT st FROM SavingTransaction st WHERE st.savingAccount.id = :accountId AND st.transactionDate BETWEEN :startDate AND :endDate ORDER BY st.transactionDate DESC")
        List<SavingTransaction> findTransactionsByDateRange(@Param("accountId") Long accountId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT st FROM SavingTransaction st WHERE st.savingAccount.id = :accountId AND st.transactionType = :type ORDER BY st.transactionDate DESC")
        List<SavingTransaction> findBySavingAccountIdAndTransactionType(@Param("accountId") Long accountId,
                        @Param("type") TransactionType type);

        @Query("SELECT st FROM SavingTransaction st WHERE st.transactionNumber = :transactionNumber")
        Optional<SavingTransaction> findByTransactionNumber(@Param("transactionNumber") String transactionNumber);

        @Query("SELECT SUM(st.amount) FROM SavingTransaction st WHERE st.savingAccount.id = :accountId AND st.transactionType IN :types AND st.transactionDate BETWEEN :startDate AND :endDate")
        BigDecimal sumTransactionsByTypeAndDateRange(@Param("accountId") Long accountId,
                        @Param("types") List<TransactionType> types, @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT COUNT(st) FROM SavingTransaction st WHERE st.savingAccount.id = :accountId AND st.isReversed = false AND st.transactionDate = :date")
        Long countTransactionsByAccountAndDate(@Param("accountId") Long accountId, @Param("date") LocalDate date);

        // Balance queries
        @Query("SELECT sb FROM SavingBalance sb WHERE sb.savingAccount.id = :savingAccountId ORDER BY sb.balanceDate DESC")
        Page<SavingBalance> findBySavingAccountIdOrderByBalanceDateDesc(@Param("savingAccountId") Long savingAccountId,
                        Pageable pageable);

        @Query("SELECT sb FROM SavingBalance sb WHERE sb.savingAccount.id = :accountId AND sb.balanceDate BETWEEN :startDate AND :endDate ORDER BY sb.balanceDate DESC")
        List<SavingBalance> findBalancesByDateRange(@Param("accountId") Long accountId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT sb FROM SavingBalance sb WHERE sb.savingAccount.id = :accountId AND sb.balanceDate = :date")
        Optional<SavingBalance> findBySavingAccountIdAndBalanceDate(@Param("accountId") Long accountId,
                        @Param("date") LocalDate date);

        @Query("SELECT sb FROM SavingBalance sb WHERE sb.savingAccount.id = :accountId AND sb.isMonthEnd = true ORDER BY sb.balanceDate DESC")
        List<SavingBalance> findMonthEndBalances(@Param("accountId") Long accountId);

        @Query("SELECT sb FROM SavingBalance sb WHERE sb.balanceDate = :date AND sb.isMonthEnd = true")
        List<SavingBalance> findMonthEndBalancesByDate(@Param("date") LocalDate date);

        // Utility queries
        boolean existsByAccountNumber(String accountNumber);

        @Query("SELECT sa FROM SavingAccount sa WHERE sa.accountNumber = :accountNumber AND sa.isActive = true")
        Optional<SavingAccount> findActiveByAccountNumber(@Param("accountNumber") String accountNumber);

        @Query("SELECT COUNT(sa) FROM SavingAccount sa WHERE sa.accountType = :type AND sa.isActive = true")
        Long countByAccountType(@Param("type") AccountType type);

        @Query("SELECT SUM(sa.balance) FROM SavingAccount sa WHERE sa.accountType = :type AND sa.isActive = true")
        BigDecimal sumBalancesByAccountType(@Param("type") AccountType type);

        @Query("SELECT sa FROM SavingAccount sa WHERE sa.openingDate BETWEEN :startDate AND :endDate AND sa.isActive = true")
        List<SavingAccount> findByOpeningDateBetween(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT sa FROM SavingAccount sa WHERE sa.member.id = :memberId AND sa.accountType = :type AND sa.isActive = true")
        Optional<SavingAccount> findByMemberIdAndAccountType(@Param("memberId") Long memberId,
                        @Param("type") AccountType type);

        // Dashboard query methods
        List<SavingAccount> findByMemberId(Long memberId);

        @Query("SELECT COALESCE(SUM(sa.balance), 0) FROM SavingAccount sa WHERE sa.isActive = true")
        java.math.BigDecimal sumTotalSavings();
}
