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
 * Provides methods for querying accounting entries and calculating trial balance.
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
        @Param("codePattern") String codePattern
    );
}
