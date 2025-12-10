package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {
    Optional<FiscalPeriod> findByMonthAndYear(Integer month, Integer year);
}
