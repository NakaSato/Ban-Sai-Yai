package com.bansaiyai.bansaiyai.config;

import com.bansaiyai.bansaiyai.repository.AuditLogRepository;
import com.bansaiyai.bansaiyai.repository.LoginAttemptRepository;
import com.bansaiyai.bansaiyai.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled tasks for maintenance operations.
 * Runs cleanup jobs for expired tokens, login attempts, etc.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksConfig {

  private final RefreshTokenRepository refreshTokenRepository;
  private final LoginAttemptRepository loginAttemptRepository;
  private final AuditLogRepository auditLogRepository;

  @Value("${scheduler.cleanup.enabled:true}")
  private boolean cleanupEnabled;

  @Value("${scheduler.audit-retention.months:12}")
  private int auditRetentionMonths;

  /**
   * Clean up expired refresh tokens.
   * Runs every day at 2:00 AM.
   */
  @Scheduled(cron = "${scheduler.token-cleanup.cron:0 0 2 * * ?}")
  @Transactional
  public void cleanupExpiredTokens() {
    if (!cleanupEnabled)
      return;

    log.info("Starting expired refresh token cleanup");
    try {
      LocalDateTime now = LocalDateTime.now();
      int deleted = refreshTokenRepository.deleteByExpiryDateBefore(now);
      log.info("Deleted {} expired refresh tokens", deleted);
    } catch (Exception e) {
      log.error("Error during refresh token cleanup", e);
    }
  }

  /**
   * Clean up old login attempts.
   * Runs every day at 3:00 AM.
   * Keeps attempts from last 7 days for security audit.
   */
  @Scheduled(cron = "${scheduler.login-attempts-cleanup.cron:0 0 3 * * ?}")
  @Transactional
  public void cleanupOldLoginAttempts() {
    if (!cleanupEnabled)
      return;

    log.info("Starting old login attempts cleanup");
    try {
      LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
      int deleted = loginAttemptRepository.deleteByAttemptTimeBefore(cutoff);
      log.info("Deleted {} old login attempts", deleted);
    } catch (Exception e) {
      log.error("Error during login attempts cleanup", e);
    }
  }

  /**
   * Clean up revoked refresh tokens.
   * Runs every 6 hours.
   */
  @Scheduled(cron = "${scheduler.revoked-token-cleanup.cron:0 0 */6 * * ?}")
  @Transactional
  public void cleanupRevokedTokens() {
    if (!cleanupEnabled)
      return;

    log.info("Starting revoked token cleanup");
    try {
      // Keep revoked tokens for 24 hours for audit purposes
      LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
      int deleted = refreshTokenRepository.deleteByIsRevokedTrueAndExpiryDateBefore(cutoff);
      log.info("Deleted {} revoked tokens", deleted);
    } catch (Exception e) {
      log.error("Error during revoked token cleanup", e);
    }
  }

  /**
   * Health check logging.
   * Runs every 30 minutes.
   */
  @Scheduled(fixedRateString = "${scheduler.health-check.rate:1800000}")
  public void logHealthStatus() {
    if (!cleanupEnabled)
      return;

    log.debug("Scheduled tasks health check - OK");
  }

  /**
   * Clean up old audit logs based on retention policy.
   * Runs weekly on Sunday at 4:00 AM.
   * Default retention period is 12 months.
   */
  @Scheduled(cron = "${scheduler.audit-cleanup.cron:0 0 4 ? * SUN}")
  @Transactional
  public void cleanupOldAuditLogs() {
    if (!cleanupEnabled)
      return;

    log.info("Starting audit log retention cleanup (retention: {} months)", auditRetentionMonths);
    try {
      LocalDateTime cutoff = LocalDateTime.now().minusMonths(auditRetentionMonths);

      // First, count how many will be deleted
      long count = auditLogRepository.countByTimestampBefore(cutoff);
      log.info("Found {} audit logs older than {} to delete", count, cutoff);

      if (count > 0) {
        // Delete in batches to avoid memory issues
        int deleted = auditLogRepository.deleteOldLogs(cutoff);
        log.info("Deleted {} old audit logs", deleted);
      }
    } catch (Exception e) {
      log.error("Error during audit log cleanup", e);
    }
  }
}
