package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for tracking login attempts and managing rate limiting.
 * Used to prevent brute force attacks by tracking failed login attempts
 * and temporarily locking accounts after exceeding the threshold.
 */
@Entity
@Table(name = "login_attempts", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_first_attempt_time", columnList = "first_attempt_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt extends BaseEntity {

  @Column(nullable = false, length = 100)
  private String username;

  @Column(name = "failed_attempts", nullable = false)
  @Builder.Default
  private Integer failedAttempts = 0;

  @Column(name = "first_attempt_time")
  private LocalDateTime firstAttemptTime;

  @Column(name = "lockout_until")
  private LocalDateTime lockoutUntil;

  /**
   * Check if the account is currently locked.
   * @return true if the account is locked, false otherwise
   */
  public boolean isLocked() {
    return lockoutUntil != null && LocalDateTime.now().isBefore(lockoutUntil);
  }

  /**
   * Get the remaining lockout time in seconds.
   * @return remaining seconds, or 0 if not locked
   */
  public long getRemainingLockoutSeconds() {
    if (!isLocked()) {
      return 0;
    }
    return java.time.Duration.between(LocalDateTime.now(), lockoutUntil).getSeconds();
  }

  /**
   * Check if the attempt window has expired (15 minutes).
   * @return true if the window has expired, false otherwise
   */
  public boolean isAttemptWindowExpired() {
    if (firstAttemptTime == null) {
      return true;
    }
    return LocalDateTime.now().isAfter(firstAttemptTime.plusMinutes(15));
  }

  /**
   * Reset the failed attempts counter.
   */
  public void reset() {
    this.failedAttempts = 0;
    this.firstAttemptTime = null;
    this.lockoutUntil = null;
  }

  /**
   * Increment the failed attempts counter.
   */
  public void incrementFailedAttempts() {
    if (firstAttemptTime == null) {
      firstAttemptTime = LocalDateTime.now();
    }
    failedAttempts++;
  }
}
