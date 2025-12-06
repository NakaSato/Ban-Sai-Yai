package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.security.AuditAspect;
import com.bansaiyai.bansaiyai.security.Audited;
import com.bansaiyai.bansaiyai.security.UserPrincipal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for AuditAspect.
 * Tests that the @Audited annotation correctly captures state changes.
 * 
 * Feature: rbac-security-system, Property 31: Loan approval audit with state
 * capture
 * Validates: Requirements 11.1, 11.2
 */
@ExtendWith(MockitoExtension.class)
public class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditAspect auditAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    private User testUser;

    @BeforeEach
    public void setUp() {
        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(User.Role.PRESIDENT)
                .enabled(true)
                .build();
        testUser.setId(1L); // Set ID after building

        // Set up security context
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities()));
    }

    /**
     * Test that the audit aspect captures method execution and logs it.
     * 
     * Feature: rbac-security-system, Property 31: Loan approval audit with state
     * capture
     * Validates: Requirements 11.2
     */
    @Test
    public void testAuditAspectCapturesMethodExecution() throws Throwable {
        // Setup: Create a mock method with @Audited annotation
        Method method = TestService.class.getMethod("approveEntity", Long.class, String.class);
        Audited auditedAnnotation = method.getAnnotation(Audited.class);
        assertNotNull(auditedAnnotation, "Method should have @Audited annotation");

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 1L, "Approved" });
        when(joinPoint.proceed()).thenReturn("Success");

        // Action: Execute the aspect
        Object result = auditAspect.auditMethod(joinPoint);

        // Verify: The method was executed
        assertEquals("Success", result);

        // Verify: Audit service was called
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> entityTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> entityIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Object> oldValuesCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> newValuesCaptor = ArgumentCaptor.forClass(Object.class);

        verify(auditService, times(1)).logAction(
                userCaptor.capture(),
                actionCaptor.capture(),
                entityTypeCaptor.capture(),
                entityIdCaptor.capture(),
                oldValuesCaptor.capture(),
                newValuesCaptor.capture());

        // Property 1: The user should be captured
        assertEquals(testUser.getId(), userCaptor.getValue().getId());

        // Property 2: The action should be captured from annotation
        assertEquals("TEST_APPROVAL", actionCaptor.getValue());

        // Property 3: The entity type should be captured from annotation
        assertEquals("TestEntity", entityTypeCaptor.getValue());

        // Property 4: The entity ID should be extracted from arguments
        assertEquals(1L, entityIdCaptor.getValue());

        // Property 5: Old values should be captured (method arguments)
        assertNotNull(oldValuesCaptor.getValue());

        // Property 6: New values should be captured (method result)
        assertNotNull(newValuesCaptor.getValue());
    }

    /**
     * Test that the audit aspect handles method failures and logs them.
     */
    @Test
    public void testAuditAspectHandlesMethodFailure() throws Throwable {
        // Setup: Create a mock method that throws an exception
        Method method = TestService.class.getMethod("approveEntity", Long.class, String.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 1L, "Approved" });
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));

        // Action & Verify: Execute the aspect and expect exception
        assertThrows(RuntimeException.class, () -> {
            auditAspect.auditMethod(joinPoint);
        });

        // Verify: Audit service was called with FAILED action
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> newValuesCaptor = ArgumentCaptor.forClass(Object.class);

        verify(auditService, times(1)).logAction(
                any(User.class),
                actionCaptor.capture(),
                anyString(),
                anyLong(),
                any(),
                newValuesCaptor.capture());

        // Property: Failed actions should be logged with _FAILED suffix
        assertTrue(actionCaptor.getValue().endsWith("_FAILED"));

        // Property: Error information should be captured
        assertNotNull(newValuesCaptor.getValue());
        if (newValuesCaptor.getValue() instanceof Map) {
            Map<?, ?> errorState = (Map<?, ?>) newValuesCaptor.getValue();
            assertTrue(errorState.containsKey("error") || errorState.containsKey("errorType"));
        }
    }

    /**
     * Test that the audit aspect works without authenticated user (graceful
     * degradation).
     */
    @Test
    public void testAuditAspectWithoutAuthenticatedUser() throws Throwable {
        // Setup: Clear security context
        SecurityContextHolder.clearContext();

        Method method = TestService.class.getMethod("approveEntity", Long.class, String.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("Success");

        // Action: Execute the aspect
        Object result = auditAspect.auditMethod(joinPoint);

        // Verify: The method was still executed
        assertEquals("Success", result);

        // Verify: Audit service was NOT called (no user to log)
        verify(auditService, never()).logAction(any(), any(), any(), any(), any(), any());
    }

    /**
     * Test service class with @Audited annotation for testing.
     */
    public static class TestService {
        @Audited(action = "TEST_APPROVAL", entityType = "TestEntity")
        public String approveEntity(Long entityId, String notes) {
            return "Approved";
        }
    }
}
