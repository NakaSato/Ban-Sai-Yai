package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.DividendDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DividendDistributionRepository extends JpaRepository<DividendDistribution, Long> {
    Optional<DividendDistribution> findByYear(Integer year);

    boolean existsByYear(Integer year);
}
