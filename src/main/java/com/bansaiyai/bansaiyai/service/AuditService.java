package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.AuditLog;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing audit logging of security-relevant actions.
 * Provides comprehensive audit trail for compliance and investigation.
 * 
 * Requirements: 10.1, 10.4, 10.5, 11.1, 11.3, 11.4
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Log a general action with old and new values.
     * 
     * @param user the user performing the action
     * @param action the action being performed
     * @param entityType the type of entity being affected
     * @param entityId the ID of the entity being affected
     * @param oldValues the old values before the action
     * @param newValues the new values after the action
     * 
     * Requirements: 11.1
     */
    @Transactional
    public void logAction(User user, String action, String entityType, Long entityId,
                         Object oldValues, Object newValues) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(getCurrentIpAddress())
                    .oldValues(serializeToJson(oldValues))
                    .newValues(serializeToJson(newValues))
                    .build();

            auditLogRepository.save(auditLog);
            
            logger.debug("Audit log created: user={}, action={}, entityType={}, entityId={}",
                    user.getUsername(), action, entityType, entityId);
        } catch (Exception e) {
            // Critical: Audit logging must not fail silently
            logger.error("CRITICAL: Failed to create audit log for user={}, action={}, entityType={}, entityId={}",
                    user.getUsername(), action, entityType, entityId, e);
            
            // Attempt to log the failure itself
            try {
                AuditLog failureLog = AuditLog.builder()
                        .user(user)
                        .action("AUDIT_LOG_FAILURE")
                        .entityType("AuditLog")
                        .entityId(null)
                        .ipAddress(getCurrentIpAddress())
                        .oldValues(null)
                        .newValues("{\"error\": \"" + e.getMessage() + "\", \"originalAction\": \"" + action + "\"}")
                        .build();
                
                auditLogRepository.save(failureLog);
            } catch (Exception innerException) {
                logger.error("CRITICAL: Failed to log audit failure", innerException);
            }
            
            // Re-throw to ensure the calling code is aware of the failure
            throw new AuditLoggingException("Failed to create audit log", e);
        }
    }

    /**
     * Log an access denied event (403 error).
     * 
     * @param user the user who was denied access
     * @param resource the resource that was attempted to be accessed
     * @param permission the permission that was required
     * 
     * Requirements: 10.4
     */
    @Transactional
    public void logAccessDenied(User user, String resource, String permission) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("resource", resource);
            details.put("requiredPermission", permission);
            details.put("userRole", user.getRbacRole() != null ? user.getRbacRole().getRoleName() : "NONE");
            details.put("timestamp", LocalDateTime.now().toString());

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action("ACCESS_DENIED")
                    .entityType("Authorization")
                    .entityId(null)
                    .ipAddress(getCurrentIpAddress())
                    .oldValues(null)
                    .newValues(serializeToJson(details))
                    .build();

            auditLogRepository.save(auditLog);
            
            logger.warn("Access denied logged: user={}, resource={}, permission={}",
                    user.getUsername(), resource, permission);
        } catch (Exception e) {
            logger.error("Failed to log access denied event for user={}, resource={}",
                    user.getUsername(), resource, e);
            // Don't throw exception for access denied logging to avoid disrupting the authorization flow
        }
    }

    /**
     * Log a role change event.
     * 
     * @param targetUser the user whose role is being changed
     * @param oldRole the old role name
     * @param newRole the new role name
     * @param admin the administrator performing the change
     * 
     * Requirements: 11.3
     */
    @Transactional
    public void logRoleChange(User targetUser, String oldRole, String newRole, User admin) {
        try {
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("userId", targetUser.getId());
            oldValues.put("username", targetUser.getUsername());
            oldValues.put("role", oldRole);

            Map<String, Object> newValues = new HashMap<>();
            newValues.put("userId", targetUser.getId());
            newValues.put("username", targetUser.getUsername());
            newValues.put("role", newRole);
            newValues.put("changedBy", admin.getUsername());
            newValues.put("changedById", admin.getId());

            AuditLog auditLog = AuditLog.builder()
                    .user(admin)
                    .action("ROLE_CHANGE")
                    .entityType("User")
                    .entityId(targetUser.getId())
                    .ipAddress(getCurrentIpAddress())
                    .oldValues(serializeToJson(oldValues))
                    .newValues(serializeToJson(newValues))
                    .build();

            auditLogRepository.save(auditLog);
            
            logger.info("Role change logged: targetUser={}, oldRole={}, newRole={}, admin={}",
                    targetUser.getUsername(), oldRole, newRole, admin.getUsername());
        } catch (Exception e) {
            logger.error("Failed to log role change for user={}, oldRole={}, newRole={}",
                    targetUser.getUsername(), oldRole, newRole, e);
            throw new AuditLoggingException("Failed to log role change", e);
        }
    }

    /**
     * Get the last N critical actions (DELETE or OVERRIDE operations).
     * 
     * @param limit the maximum number of results to return
     * @return a list of critical audit logs
     * 
     * Requirements: 10.1
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getCriticalActions(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return auditLogRepository.findCriticalActions(pageable);
        } catch (Exception e) {
            logger.error("Failed to retrieve critical actions", e);
            throw new RuntimeException("Failed to retrieve critical actions", e);
        }
    }

    /**
     * Get role violation attempts (HTTP 403 errors) since a given time.
     * 
     * @param since the start time to search from
     * @return a list of role violation audit logs
     * 
     * Requirements: 10.4, 10.5
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRoleViolations(LocalDateTime since) {
        try {
            return auditLogRepository.findRoleViolations(since);
        } catch (Exception e) {
            logger.error("Failed to retrieve role violations since {}", since, e);
            throw new RuntimeException("Failed to retrieve role violations", e);
        }
    }

    /**
     * Get activity heatmap data (entry volume by user) since a given time.
     * 
     * @param since the start time to search from
     * @return a map of user IDs to action counts
     * 
     * Requirements: 10.5
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getActivityHeatmap(LocalDateTime since) {
        try {
            List<Map<String, Object>> rawData = auditLogRepository.getActivityHeatmap(since);
            Map<String, Integer> heatmap = new HashMap<>();
            
            for (Map<String, Object> entry : rawData) {
                String username = (String) entry.get("username");
                Long actionCount = (Long) entry.get("actionCount");
                heatmap.put(username, actionCount.intValue());
            }
            
            return heatmap;
        } catch (Exception e) {
            logger.error("Failed to retrieve activity heatmap since {}", since, e);
            throw new RuntimeException("Failed to retrieve activity heatmap", e);
        }
    }

    /**
     * Get off-hours activity (actions outside normal meeting hours).
     * Normal meeting hours are typically 9 AM to 5 PM.
     * 
     * @param since the start time to search from
     * @return a list of audit logs for off-hours activity
     * 
     * Requirements: 10.3
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getOffHoursActivity(LocalDateTime since) {
        try {
            return auditLogRepository.findOffHoursActivity(since);
        } catch (Exception e) {
            logger.error("Failed to retrieve off-hours activity since {}", since, e);
            throw new RuntimeException("Failed to retrieve off-hours activity", e);
        }
    }

    /**
     * Get audit logs for a specific entity.
     * 
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return a list of audit logs for the entity
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForEntity(String entityType, Long entityId) {
        try {
            return auditLogRepository.findByEntity(entityType, entityId);
        } catch (Exception e) {
            logger.error("Failed to retrieve audit logs for entity type={}, id={}", entityType, entityId, e);
            throw new RuntimeException("Failed to retrieve audit logs for entity", e);
        }
    }

    /**
     * Get role change audit logs since a given time.
     * 
     * @param since the start time to search from
     * @return a list of role change audit logs
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRoleChanges(LocalDateTime since) {
        try {
            return auditLogRepository.findRoleChanges(since);
        } catch (Exception e) {
            logger.error("Failed to retrieve role changes since {}", since, e);
            throw new RuntimeException("Failed to retrieve role changes", e);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Serialize an object to JSON string.
     * 
     * @param object the object to serialize
     * @return the JSON string representation
     */
    private String serializeToJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object to JSON: {}", object.getClass().getName(), e);
            // Return a fallback JSON with error information
            return "{\"error\": \"Failed to serialize\", \"type\": \"" + object.getClass().getName() + "\"}";
        }
    }

    /**
     * Get the current IP address from the HTTP request.
     * 
     * @return the IP address, or "UNKNOWN" if not available
     */
    private String getCurrentIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Check for X-Forwarded-For header (for proxied requests)
                String ipAddress = request.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("X-Real-IP");
                }
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getRemoteAddr();
                }
                
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ipAddress != null && ipAddress.contains(",")) {
                    ipAddress = ipAddress.split(",")[0].trim();
                }
                
                return ipAddress;
            }
        } catch (Exception e) {
            logger.debug("Failed to get IP address from request context", e);
        }
        
        return "UNKNOWN";
    }

    /**
     * Custom exception for audit logging failures.
     */
    public static class AuditLoggingException extends RuntimeException {
        public AuditLoggingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
