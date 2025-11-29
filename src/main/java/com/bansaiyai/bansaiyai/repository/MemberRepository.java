package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  @Query("SELECT m FROM Member m WHERE m.user.id = :userId")
  Optional<Member> findByUserId(@Param("userId") Long userId);

  List<Member> findByIsActive(boolean isActive);

  @Query("SELECT m FROM Member m WHERE " +
      "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(m.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Member> searchMembers(@Param("keyword") String keyword);

  @Query("SELECT m FROM Member m WHERE m.isActive = true ORDER BY m.name")
  List<Member> findActiveMembersOrderBy();

  @Query("SELECT COUNT(m) FROM Member m WHERE m.isActive = true")
  long countActiveMembers();

  @Query("SELECT COUNT(m) FROM Member m WHERE m.isActive = false")
  long countInactiveMembers();

  long countByIsActive(Boolean isActive);
}
