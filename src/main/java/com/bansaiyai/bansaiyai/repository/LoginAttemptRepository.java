package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for managing LoginAttempt entities.
 * Provides methods for tracking and managing failed login attempts.
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

  /**
   * Find login attempt record by username.
   * 
   * @param username the username
   * @return Optional containing the LoginAttempt if found
   */
  Optional<LoginAttempt> findByUsername(String username);

  /**
   * Delete login attempt record by username.
   * Used when resetting attempts after successful login.
   * 
   * @param username the username
   */
  void deleteByUsername(String username);

  /**
   * Delete all expired lockout records.
   * Used for cleanup of old attempt records.
   * 
   * @param now current timestamp
   */
  @Modifying
  @Query("DELETE FROM LoginAttempt la WHERE la.lockoutUntil IS NOT NULL AND la.lockoutUntil < :now")
  void deleteExpiredLockouts(@Param("now") LocalDateTime now);

  /**
   * Delete all attempt records where the attempt window has expired (older than
   * 15 minutes).
   * 
   * @param cutoffTime timestamp 15 minutes ago
   */
  @Modifying
  @Query("DELETE FROM LoginAttempt la WHERE la.firstAttemptTime < :cutoffTime AND la.lockoutUntil IS NULL")
  void deleteExpiredAttemptWindows(@Param("cutoffTime") LocalDateTime cutoffTime);

  /**
   * Check if a username is currently locked.
   * 
   * @param username the username
   * @param now      current timestamp
   * @return true if the username is locked
   */
  @Query("SELECT CASE WHEN COUNT(la) > 0 THEN true ELSE false END FROM LoginAttempt la WHERE la.username = :username AND la.lockoutUntil IS NOT NULL AND la.lockoutUntil > :now")
  boolean isUsernameLocked(@Param("username") String username, @Param("now") LocalDateTime now);

  /**
   * Delete login attempts older than cutoff.
   * 
   * @param cutoff cutoff timestamp
   * @return number of deleted records
   */
  @Modifying
  @Query("DELETE FROM LoginAttempt la WHERE la.firstAttemptTime < :cutoff")
  int deleteByAttemptTimeBefore(@Param("cutoff") LocalDateTime cutoff);
}
