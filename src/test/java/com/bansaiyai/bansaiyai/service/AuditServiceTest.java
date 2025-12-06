package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.AuditLog;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditService.
 * Tests specific examples and edge cases for audit logging functionality.
 * 
 * Requirements: 11.1, 11.5
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditService auditService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(User.Role.OFFICER)
                .enabled(true)
                .build();
        testUser.setId(1L);
    }

    /**
     * Test that logAction creates a complete audit entry.
     * Requirements: 11.1
     */
    @Test
    void testLogActionCreatesCompleteAuditEntry() throws Exception {
        // Arrange
        String action = "CREATE";
        String entityType = "Member";
        Long entityId = 123L;
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("name", "Old Name");
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("name", "New Name");

        String oldValuesJson = "{\"name\":\"Old Name\"}";
        String newValuesJson = "{\"name\":\"New Name\"}";

        when(objectMapper.writeValueAsString(oldValues)).thenReturn(oldValuesJson);
        when(objectMapper.writeValueAsString(newValues)).thenReturn(newValuesJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAction(testUser, action, entityType, entityId, oldValues, newValues);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals(testUser, capturedLog.getUser());
        assertEquals(action, capturedLog.getAction());
        assertEquals(entityType, capturedLog.getEntityType());
        assertEquals(entityId, capturedLog.getEntityId());
        assertEquals(oldValuesJson, capturedLog.getOldValues());
        assertEquals(newValuesJson, capturedLog.getNewValues());
        assertNotNull(capturedLog.getIpAddress());
    }

    /**
     * Test JSON serialization of old/new values.
     * Requirements: 11.5
     */
    @Test
    void testJSONSerializationOfOldNewValues() throws Exception {
        // Arrange
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("status", "PENDING");
        oldValues.put("amount", 1000);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("status", "APPROVED");
        newValues.put("amount", 1000);

        String oldValuesJson = "{\"status\":\"PENDING\",\"amount\":1000}";
        String newValuesJson = "{\"status\":\"APPROVED\",\"amount\":1000}";

        when(objectMapper.writeValueAsString(oldValues)).thenReturn(oldValuesJson);
        when(objectMapper.writeValueAsString(newValues)).thenReturn(newValuesJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAction(testUser, "UPDATE", "Loan", 456L, oldValues, newValues);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();
        assertEquals(oldValuesJson, capturedLog.getOldValues());
        assertEquals(newValuesJson, capturedLog.getNewValues());

        // Verify JSON is valid by checking it contains expected data
        assertTrue(capturedLog.getOldValues().contains("PENDING"));
        assertTrue(capturedLog.getNewValues().contains("APPROVED"));
    }

    /**
     * Test that logAccessDenied creates an access denied audit entry.
     * Requirements: 10.4
     */
    @Test
    void testLogAccessDeniedCreatesAuditEntry() throws Exception {
        // Arrange
        String resource = "/api/admin/users";
        String permission = "system.manage_users";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"resource\":\"/api/admin/users\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAccessDenied(testUser, resource, permission);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals(testUser, capturedLog.getUser());
        assertEquals("ACCESS_DENIED", capturedLog.getAction());
        assertEquals("Authorization", capturedLog.getEntityType());
        assertNull(capturedLog.getEntityId());
        assertNull(capturedLog.getOldValues());
        assertNotNull(capturedLog.getNewValues());
    }

    /**
     * Test that logRoleChange creates a role change audit entry.
     * Requirements: 11.3
     */
    @Test
    void testLogRoleChangeCreatesAuditEntry() throws Exception {
        // Arrange
        User targetUser = User.builder()
                .username("targetuser")
                .email("target@example.com")
                .password("password")
                .role(User.Role.MEMBER)
                .enabled(true)
                .build();
        targetUser.setId(2L);

        User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("password")
                .role(User.Role.PRESIDENT)
                .enabled(true)
                .build();
        admin.setId(3L);

        String oldRole = "ROLE_MEMBER";
        String newRole = "ROLE_OFFICER";

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logRoleChange(targetUser, oldRole, newRole, admin);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals(admin, capturedLog.getUser());
        assertEquals("ROLE_CHANGE", capturedLog.getAction());
        assertEquals("User", capturedLog.getEntityType());
        assertEquals(targetUser.getId(), capturedLog.getEntityId());
        assertNotNull(capturedLog.getOldValues());
        assertNotNull(capturedLog.getNewValues());
    }

    /**
     * Test getCriticalActions filtering.
     * Requirements: 10.1
     */
    @Test
    void testGetCriticalActionsFiltering() {
        // Arrange
        int limit = 10;
        List<AuditLog> criticalLogs = new ArrayList<>();
        
        AuditLog deleteLog = AuditLog.builder()
                .user(testUser)
                .action("DELETE")
                .entityType("Member")
                .entityId(1L)
                .timestamp(LocalDateTime.now())
                .build();
        
        AuditLog overrideLog = AuditLog.builder()
                .user(testUser)
                .action("OVERRIDE")
                .entityType("Transaction")
                .entityId(2L)
                .timestamp(LocalDateTime.now())
                .build();
        
        criticalLogs.add(deleteLog);
        criticalLogs.add(overrideLog);

        when(auditLogRepository.findCriticalActions(any(Pageable.class))).thenReturn(criticalLogs);

        // Act
        List<AuditLog> result = auditService.getCriticalActions(limit);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(auditLogRepository).findCriticalActions(PageRequest.of(0, limit));
    }

    /**
     * Test getRoleViolations returns access denied logs.
     * Requirements: 10.4, 10.5
     */
    @Test
    void testGetRoleViolationsReturnsAccessDeniedLogs() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<AuditLog> violations = new ArrayList<>();
        
        AuditLog violation = AuditLog.builder()
                .user(testUser)
                .action("ACCESS_DENIED")
                .entityType("Authorization")
                .timestamp(LocalDateTime.now())
                .build();
        
        violations.add(violation);

        when(auditLogRepository.findRoleViolations(since)).thenReturn(violations);

        // Act
        List<AuditLog> result = auditService.getRoleViolations(since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACCESS_DENIED", result.get(0).getAction());
        verify(auditLogRepository).findRoleViolations(since);
    }

    /**
     * Test getActivityHeatmap returns user activity counts.
     * Requirements: 10.5
     */
    @Test
    void testGetActivityHeatmapReturnsUserActivityCounts() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Map<String, Object>> rawData = new ArrayList<>();
        
        Map<String, Object> userData1 = new HashMap<>();
        userData1.put("userId", 1L);
        userData1.put("username", "user1");
        userData1.put("actionCount", 50L);
        
        Map<String, Object> userData2 = new HashMap<>();
        userData2.put("userId", 2L);
        userData2.put("username", "user2");
        userData2.put("actionCount", 30L);
        
        rawData.add(userData1);
        rawData.add(userData2);

        when(auditLogRepository.getActivityHeatmap(since)).thenReturn(rawData);

        // Act
        Map<String, Integer> result = auditService.getActivityHeatmap(since);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(50, result.get("user1"));
        assertEquals(30, result.get("user2"));
        verify(auditLogRepository).getActivityHeatmap(since);
    }

    /**
     * Test error handling for audit logging failures.
     * Requirements: 11.1
     */
    @Test
    void testErrorHandlingForAuditLoggingFailures() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON serialization failed"));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        assertThrows(AuditService.AuditLoggingException.class, () -> {
            auditService.logAction(testUser, "CREATE", "Member", 1L, 
                    Collections.singletonMap("key", "value"), 
                    Collections.singletonMap("key", "newValue"));
        });

        // Verify that a failure log was attempted to be saved
        verify(auditLogRepository, atLeastOnce()).save(any(AuditLog.class));
    }

    /**
     * Test that null values are handled gracefully.
     */
    @Test
    void testNullValuesHandledGracefully() throws Exception {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAction(testUser, "DELETE", "Member", 1L, null, null);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();
        assertNotNull(capturedLog);
        assertNull(capturedLog.getOldValues());
        assertNull(capturedLog.getNewValues());
    }

    /**
     * Test getOffHoursActivity returns logs outside normal hours.
     * Requirements: 10.3
     */
    @Test
    void testGetOffHoursActivityReturnsLogsOutsideNormalHours() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<AuditLog> offHoursLogs = new ArrayList<>();
        
        AuditLog offHoursLog = AuditLog.builder()
                .user(testUser)
                .action("CREATE")
                .entityType("Transaction")
                .entityId(1L)
                .timestamp(LocalDateTime.now().withHour(22)) // 10 PM
                .build();
        
        offHoursLogs.add(offHoursLog);

        when(auditLogRepository.findOffHoursActivity(since)).thenReturn(offHoursLogs);

        // Act
        List<AuditLog> result = auditService.getOffHoursActivity(since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository).findOffHoursActivity(since);
    }

    /**
     * Test getAuditLogsForEntity returns logs for a specific entity.
     */
    @Test
    void testGetAuditLogsForEntityReturnsLogsForSpecificEntity() {
        // Arrange
        String entityType = "Loan";
        Long entityId = 123L;
        List<AuditLog> entityLogs = new ArrayList<>();
        
        AuditLog log1 = AuditLog.builder()
                .user(testUser)
                .action("CREATE")
                .entityType(entityType)
                .entityId(entityId)
                .timestamp(LocalDateTime.now())
                .build();
        
        AuditLog log2 = AuditLog.builder()
                .user(testUser)
                .action("UPDATE")
                .entityType(entityType)
                .entityId(entityId)
                .timestamp(LocalDateTime.now())
                .build();
        
        entityLogs.add(log1);
        entityLogs.add(log2);

        when(auditLogRepository.findByEntity(entityType, entityId)).thenReturn(entityLogs);

        // Act
        List<AuditLog> result = auditService.getAuditLogsForEntity(entityType, entityId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(entityType, result.get(0).getEntityType());
        assertEquals(entityId, result.get(0).getEntityId());
        verify(auditLogRepository).findByEntity(entityType, entityId);
    }

    /**
     * Test getRoleChanges returns role change logs.
     */
    @Test
    void testGetRoleChangesReturnsRoleChangeLogs() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<AuditLog> roleChangeLogs = new ArrayList<>();
        
        AuditLog roleChangeLog = AuditLog.builder()
                .user(testUser)
                .action("ROLE_CHANGE")
                .entityType("User")
                .entityId(2L)
                .timestamp(LocalDateTime.now())
                .build();
        
        roleChangeLogs.add(roleChangeLog);

        when(auditLogRepository.findRoleChanges(since)).thenReturn(roleChangeLogs);

        // Act
        List<AuditLog> result = auditService.getRoleChanges(since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ROLE_CHANGE", result.get(0).getAction());
        verify(auditLogRepository).findRoleChanges(since);
    }
}
