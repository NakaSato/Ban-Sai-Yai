package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.AuditLog;
import com.bansaiyai.bansaiyai.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Repository for AuditLog entity operations.
 * Provides methods for querying audit logs with custom queries for critical actions and violations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  /**
   * Find audit logs by user.
   *
   * @param user the user
   * @param pageable pagination information
   * @return a page of audit logs for the user
   */
  Page<AuditLog> findByUser(User user, Pageable pageable);

  /**
   * Find audit logs by user ID.
   *
   * @param userId the user ID
   * @param pageable pagination information
   * @return a page of audit logs for the user
   */
  Page<AuditLog> findByUserId(Long userId, Pageable pageable);

  /**
   * Find audit logs by action.
   *
   * @param action the action name
   * @param pageable pagination information
   * @return a page of audit logs for the action
   */
  Page<AuditLog> findByAction(String action, Pageable pageable);

  /**
   * Find audit logs by entity type and entity ID.
   *
   * @param entityType the entity type
   * @param entityId the entity ID
   * @param pageable pagination information
   * @return a page of audit logs for the entity
   */
  Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

  /**
   * Find audit logs within a time range.
   *
   * @param startTime the start time
   * @param endTime the end time
   * @param pageable pagination information
   * @return a page of audit logs within the time range
   */
  Page<AuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /**
   * Get the last N critical actions (DELETE or OVERRIDE operations).
   * Critical actions are those involving DELETE or OVERRIDE permissions.
   *
   * @param limit the maximum number of results
   * @return a list of critical audit logs
   */
  @Query("SELECT a FROM AuditLog a WHERE a.action LIKE '%DELETE%' OR a.action LIKE '%OVERRIDE%' " +
         "ORDER BY a.timestamp DESC")
  List<AuditLog> findCriticalActions(Pageable pageable);

  /**
   * Get role violation attempts (HTTP 403 errors / access denied).
   * These are logged when users attempt to access resources without authorization.
   *
   * @param since the start time to search from
   * @return a list of role violation audit logs
   */
  @Query("SELECT a FROM AuditLog a WHERE a.action = 'ACCESS_DENIED' AND a.timestamp >= :since " +
         "ORDER BY a.timestamp DESC")
  List<AuditLog> findRoleViolations(@Param("since") LocalDateTime since);

  /**
   * Get all role violation attempts with pagination.
   *
   * @param pageable pagination information
   * @return a page of role violation audit logs
   */
  @Query("SELECT a FROM AuditLog a WHERE a.action = 'ACCESS_DENIED' ORDER BY a.timestamp DESC")
  Page<AuditLog> findAllRoleViolations(Pageable pageable);

  /**
   * Get activity heatmap data (entry volume by user).
   * Returns a count of actions per user since a given time.
   *
   * @param since the start time to search from
   * @return a list of objects containing user ID and action count
   */
  @Query("SELECT a.user.id as userId, a.user.username as username, COUNT(a) as actionCount " +
         "FROM AuditLog a WHERE a.timestamp >= :since " +
         "GROUP BY a.user.id, a.user.username " +
         "ORDER BY actionCount DESC")
  List<Map<String, Object>> getActivityHeatmap(@Param("since") LocalDateTime since);

  /**
   * Get off-hours activity (actions outside normal meeting hours).
   * Normal meeting hours are typically defined as 9 AM to 5 PM on weekdays.
   *
   * @param since the start time to search from
   * @return a list of audit logs for off-hours activity
   */
  @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since " +
         "AND (HOUR(a.timestamp) < 9 OR HOUR(a.timestamp) >= 17) " +
         "ORDER BY a.timestamp DESC")
  List<AuditLog> findOffHoursActivity(@Param("since") LocalDateTime since);

  /**
   * Get actions by specific action types.
   *
   * @param actions the list of action types
   * @param since the start time to search from
   * @return a list of audit logs matching the action types
   */
  @Query("SELECT a FROM AuditLog a WHERE a.action IN :actions AND a.timestamp >= :since " +
         "ORDER BY a.timestamp DESC")
  List<AuditLog> findByActionIn(@Param("actions") List<String> actions, @Param("since") LocalDateTime since);

  /**
   * Get audit logs for a specific entity.
   *
   * @param entityType the entity type
   * @param entityId the entity ID
   * @return a list of audit logs for the entity
   */
  @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId " +
         "ORDER BY a.timestamp DESC")
  List<AuditLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

  /**
   * Count actions by user within a time range.
   *
   * @param userId the user ID
   * @param startTime the start time
   * @param endTime the end time
   * @return the count of actions
   */
  @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId " +
         "AND a.timestamp BETWEEN :startTime AND :endTime")
  long countByUserIdAndTimestampBetween(@Param("userId") Long userId, 
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

  /**
   * Find role change audit logs.
   *
   * @param since the start time to search from
   * @return a list of role change audit logs
   */
  @Query("SELECT a FROM AuditLog a WHERE a.action = 'ROLE_CHANGE' AND a.timestamp >= :since " +
         "ORDER BY a.timestamp DESC")
  List<AuditLog> findRoleChanges(@Param("since") LocalDateTime since);

  /**
   * Find audit logs by IP address.
   *
   * @param ipAddress the IP address
   * @param pageable pagination information
   * @return a page of audit logs from the IP address
   */
  Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);

  /**
   * Get the most recent audit log for a specific entity.
   *
   * @param entityType the entity type
   * @param entityId the entity ID
   * @return the most recent audit log for the entity
   */
  @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId " +
         "ORDER BY a.timestamp DESC LIMIT 1")
  AuditLog findMostRecentByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
}
