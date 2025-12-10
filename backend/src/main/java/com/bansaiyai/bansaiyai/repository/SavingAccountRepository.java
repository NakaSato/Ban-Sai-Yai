package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
    List<SavingAccount> findByMemberId(Long memberId);

    Optional<SavingAccount> findByAccountNumber(String accountNumber);

    List<SavingAccount> findByIsActiveTrue();
}
