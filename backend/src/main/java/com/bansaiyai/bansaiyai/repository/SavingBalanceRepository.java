package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.SavingBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavingBalanceRepository extends JpaRepository<SavingBalance, Long> {

    List<SavingBalance> findBySavingAccountIdOrderByBalanceDateDesc(Long savingAccountId);

    Optional<SavingBalance> findBySavingAccountIdAndBalanceDate(Long savingAccountId, LocalDate balanceDate);

    boolean existsBySavingAccountIdAndBalanceDate(Long savingAccountId, LocalDate balanceDate);

    @Query("SELECT b FROM SavingBalance b WHERE b.balanceDate = :date")
    List<SavingBalance> findByBalanceDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(b.closingBalance), 0) FROM SavingBalance b WHERE b.balanceDate = :date")
    BigDecimal sumClosingBalanceByDate(@Param("date") LocalDate date);
}
