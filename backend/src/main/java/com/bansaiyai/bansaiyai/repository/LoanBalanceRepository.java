package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.LoanBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanBalanceRepository extends JpaRepository<LoanBalance, Long> {

    /**
     * Find all balance history for a loan, ordered by date descending
     */
    List<LoanBalance> findByLoanIdOrderByBalanceDateDesc(Long loanId);

    /**
     * Find specific balance record for a loan and date
     */
    Optional<LoanBalance> findByLoanIdAndBalanceDate(Long loanId, LocalDate balanceDate);

    /**
     * Find latest balance record for a loan
     */
    Optional<LoanBalance> findFirstByLoanIdOrderByBalanceDateDesc(Long loanId);

    /**
     * Find balances by formatted Month-Year (if we stored it, but we store
     * LocalDate so we might query by range or rely on service logic)
     * For now, the service will likely calculate date ranges.
     */
    @Query("SELECT b FROM LoanBalance b WHERE b.balanceDate = :date")
    List<LoanBalance> findByBalanceDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(b.outstandingBalance), 0) FROM LoanBalance b WHERE b.balanceDate = :date")
    java.math.BigDecimal sumOutstandingBalanceByDate(@Param("date") LocalDate date);
}
