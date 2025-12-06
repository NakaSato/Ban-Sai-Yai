package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.RefreshToken;
import com.bansaiyai.bansaiyai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing RefreshToken entities.
 * Provides methods for token lookup, validation, and cleanup.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * Find a refresh token by its token string.
   * 
   * @param token the token string
   * @return Optional containing the RefreshToken if found
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * Find all refresh tokens for a specific user.
   * 
   * @param user the user
   * @return list of refresh tokens
   */
  List<RefreshToken> findByUser(User user);

  /**
   * Find all valid (non-revoked, non-expired) tokens for a user.
   * 
   * @param user the user
   * @param now  current timestamp
   * @return list of valid refresh tokens
   */
  @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
  List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

  /**
   * Delete all refresh tokens for a specific user.
   * Used during logout to invalidate all sessions.
   * 
   * @param user the user
   */
  void deleteByUser(User user);

  /**
   * Delete all expired refresh tokens.
   * Used for cleanup of old tokens.
   * 
   * @param now current timestamp
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
  void deleteExpiredTokens(@Param("now") LocalDateTime now);

  /**
   * Revoke all tokens for a specific user.
   * 
   * @param user the user
   */
  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
  void revokeAllTokensByUser(@Param("user") User user);

  /**
   * Check if a token exists and is valid.
   * 
   * @param token the token string
   * @param now   current timestamp
   * @return true if token exists and is valid
   */
  @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiresAt > :now")
  boolean existsValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

  /**
   * Delete expired tokens and return count.
   * 
   * @param now current timestamp
   * @return number of deleted tokens
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
  int deleteByExpiryDateBefore(@Param("now") LocalDateTime now);

  /**
   * Delete revoked tokens older than cutoff.
   * 
   * @param cutoff cutoff timestamp
   * @return number of deleted tokens
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.expiresAt < :cutoff")
  int deleteByIsRevokedTrueAndExpiryDateBefore(@Param("cutoff") LocalDateTime cutoff);
}
