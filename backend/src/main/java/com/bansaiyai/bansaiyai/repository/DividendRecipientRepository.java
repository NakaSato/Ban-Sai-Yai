package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.DividendRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DividendRecipientRepository extends JpaRepository<DividendRecipient, Long> {

    @Query("SELECT dr FROM DividendRecipient dr WHERE dr.dividendDistribution.id = :distId")
    List<DividendRecipient> findByDividendDistributionId(@Param("distId") Long distId);

    @Query("SELECT dr FROM DividendRecipient dr WHERE dr.member.id = :memberId")
    List<DividendRecipient> findByMemberId(@Param("memberId") Long memberId);
}
