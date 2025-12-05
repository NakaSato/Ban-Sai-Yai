package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.LoginAttempt;
import com.bansaiyai.bansaiyai.repository.LoginAttemptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing login attempts and rate limiting.
 * Implements rate limiting to prevent brute force attacks by tracking
 * failed login attempts and temporarily locking accounts after exceeding
 * threshold.
 * 
 * Account locking can be disabled via configuration:
 * - Set auth.account-lock.enabled=false in application.yml
 * - Or set AUTH_ACCOUNT_LOCK_ENABLED=false in .env file
 */
@Service
@Slf4j
public class LoginAttemptService {

  @Autowired
  private LoginAttemptRepository loginAttemptRepository;

  /**
   * Enable/disable account locking feature.
   * When disabled, failed login attempts are still recorded but accounts won't be
   * locked.
   */
  @Value("${auth.account-lock.enabled:true}")
  private boolean accountLockEnabled;

  @Value("${auth.rate-limit.max-attempts:5}")
  private int maxAttempts;

  @Value("${auth.rate-limit.window-minutes:15}")
  private int windowMinutes;

  @Value("${auth.rate-limit.lockout-minutes:15}")
  private int lockoutMinutes;

  /**
   * Record a failed login attempt for the given username.
   * If the maximum number of attempts is exceeded within the time window,
   * the account will be locked for the configured lockout period (if locking is
   * enabled).
   * 
   * @param username the username that failed to authenticate
   */
  @Transactional
  public void recordFailedAttempt(String username) {
    // If account locking is disabled, just log and return
    if (!accountLockEnabled) {
      log.debug("Account locking is disabled. Skipping failed attempt recording for user: {}", username);
      return;
    }

    Optional<LoginAttempt> existingAttempt = loginAttemptRepository.findByUsername(username);

    if (existingAttempt.isPresent()) {
      LoginAttempt attempt = existingAttempt.get();

      // If already locked, don't extend the lockout time (Requirement 4.5)
      if (attempt.isLocked()) {
        return;
      }

      // If the attempt window has expired, reset the counter
      if (attempt.isAttemptWindowExpired()) {
        attempt.reset();
      }

      // Increment failed attempts
      attempt.incrementFailedAttempts();

      // Check if we've exceeded the threshold
      if (attempt.getFailedAttempts() >= maxAttempts) {
        attempt.setLockoutUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
      }

      loginAttemptRepository.save(attempt);
    } else {
      // Create new login attempt record
      LoginAttempt newAttempt = LoginAttempt.builder()
          .username(username)
          .failedAttempts(1)
          .firstAttemptTime(LocalDateTime.now())
          .build();

      loginAttemptRepository.save(newAttempt);
    }
  }

  /**
   * Record a successful login attempt for the given username.
   * This resets the failed attempt counter (Requirement 4.4).
   * 
   * @param username the username that successfully authenticated
   */
  @Transactional
  public void recordSuccessfulAttempt(String username) {
    if (!accountLockEnabled) {
      return;
    }
    loginAttemptRepository.deleteByUsername(username);
  }

  /**
   * Check if the given username is currently blocked due to too many failed
   * attempts.
   * Always returns false if account locking is disabled.
   * 
   * @param username the username to check
   * @return true if the username is blocked, false otherwise
   */
  public boolean isBlocked(String username) {
    // If account locking is disabled, never block any user
    if (!accountLockEnabled) {
      return false;
    }
    return loginAttemptRepository.isUsernameLocked(username, LocalDateTime.now());
  }

  /**
   * Get the remaining lockout time in seconds for the given username.
   * Returns 0 if account locking is disabled.
   * 
   * @param username the username to check
   * @return remaining lockout time in seconds, or 0 if not locked
   */
  public long getRemainingLockoutSeconds(String username) {
    if (!accountLockEnabled) {
      return 0;
    }

    Optional<LoginAttempt> attempt = loginAttemptRepository.findByUsername(username);

    if (attempt.isPresent()) {
      return attempt.get().getRemainingLockoutSeconds();
    }

    return 0;
  }

  /**
   * Check if account locking feature is enabled.
   * 
   * @return true if account locking is enabled, false otherwise
   */
  public boolean isAccountLockEnabled() {
    return accountLockEnabled;
  }

  /**
   * Automatic cleanup of expired lockout records.
   * Runs every hour to clean up old records and free up database space.
   * This is a scheduled task that runs in the background.
   */
  @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
  @Transactional
  public void cleanupExpiredRecords() {
    LocalDateTime now = LocalDateTime.now();

    // Delete expired lockouts
    loginAttemptRepository.deleteExpiredLockouts(now);

    // Delete expired attempt windows (older than window time)
    LocalDateTime cutoffTime = now.minusMinutes(windowMinutes);
    loginAttemptRepository.deleteExpiredAttemptWindows(cutoffTime);
  }
}
