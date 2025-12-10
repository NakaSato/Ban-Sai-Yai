package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.AccountingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for AccountingEntry entity.
 * Provides methods for querying accounting entries and calculating trial
 * balance.
 */
@Repository
public interface AccountingRepository extends JpaRepository<AccountingEntry, Long> {

        /**
         * Find all accounting entries for a specific fiscal period
         */
        List<AccountingEntry> findByFiscalPeriod(String fiscalPeriod);

        /**
         * Calculate sum of all debits for a fiscal period
         */
        @Query("SELECT COALESCE(SUM(a.debit), 0) FROM AccountingEntry a WHERE a.fiscalPeriod = :fiscalPeriod")
        BigDecimal sumDebitsByFiscalPeriod(@Param("fiscalPeriod") String fiscalPeriod);

        /**
         * Calculate sum of all credits for a fiscal period
         */
        @Query("SELECT COALESCE(SUM(a.credit), 0) FROM AccountingEntry a WHERE a.fiscalPeriod = :fiscalPeriod")
        BigDecimal sumCreditsByFiscalPeriod(@Param("fiscalPeriod") String fiscalPeriod);

        /**
         * Find entries by account code range (for financial statement classification)
         */
        @Query("SELECT a FROM AccountingEntry a WHERE a.fiscalPeriod = :fiscalPeriod AND a.accountCode LIKE :codePattern")
        List<AccountingEntry> findByFiscalPeriodAndAccountCodePattern(
                        @Param("fiscalPeriod") String fiscalPeriod,
                        @Param("codePattern") String codePattern);

        /**
         * Find all accounting entries for a specific account code before a given date
         */
        @Query("SELECT a FROM AccountingEntry a WHERE a.accountCode = :accountCode AND a.transactionDate < :date")
        List<AccountingEntry> findByAccountCodeAndTransactionDateBefore(
                        @Param("accountCode") String accountCode,
                        @Param("date") java.time.LocalDate date);

        /**
         * Sum credits by account code pattern and date range
         */
        @Query("SELECT a.accountName, SUM(a.credit) FROM AccountingEntry a WHERE a.transactionDate BETWEEN :startDate AND :endDate AND a.accountCode LIKE :codePattern GROUP BY a.accountName")
        List<Object[]> sumCreditByCodePatternAndDateRange(
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate,
                        @Param("codePattern") String codePattern);

        /**
         * Sum debits by account code pattern and date range
         */
        @Query("SELECT a.accountName, SUM(a.debit) FROM AccountingEntry a WHERE a.transactionDate BETWEEN :startDate AND :endDate AND a.accountCode LIKE :codePattern GROUP BY a.accountName")
        List<Object[]> sumDebitByCodePatternAndDateRange(
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate,
                        @Param("codePattern") String codePattern);

        boolean existsByAccountCode(String accountCode);
}
