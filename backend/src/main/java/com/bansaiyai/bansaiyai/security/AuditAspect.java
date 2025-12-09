package com.bansaiyai.bansaiyai.security;

import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP aspect for automatic audit logging of annotated methods.
 * Captures method arguments before execution and return values after execution
 * to log state changes for compliance and investigation.
 * 
 * Requirements: 11.1, 11.2
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditService auditService;

    /**
     * Around advice for methods annotated with @Audited.
     * Captures state before and after method execution and logs the action.
     * 
     * @param joinPoint the join point representing the method execution
     * @return the result of the method execution
     * @throws Throwable if the method execution fails
     */
    @Around("@annotation(com.bansaiyai.bansaiyai.security.Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audited auditedAnnotation = method.getAnnotation(Audited.class);
        
        // Get current user from security context
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            logger.warn("No authenticated user found for auditing method: {}", method.getName());
            // Still execute the method even if we can't audit it
            return joinPoint.proceed();
        }
        
        // Capture method arguments before execution
        Object[] args = joinPoint.getArgs();
        Object oldState = captureState(args);
        
        // Determine action name
        String action = auditedAnnotation.action().isEmpty() 
            ? method.getName().toUpperCase() 
            : auditedAnnotation.action();
        
        // Determine entity type and ID
        String entityType = auditedAnnotation.entityType().isEmpty()
            ? extractEntityType(args)
            : auditedAnnotation.entityType();
        Long entityId = extractEntityId(args);
        
        try {
            // Execute the method
            Object result = joinPoint.proceed();
            
            // Capture state after execution
            Object newState = captureState(result);
            
            // Log the action
            auditService.logAction(currentUser, action, entityType, entityId, oldState, newState);
            
            return result;
        } catch (Exception e) {
            // Log the failed action attempt
            Map<String, Object> errorState = new HashMap<>();
            errorState.put("error", e.getMessage());
            errorState.put("errorType", e.getClass().getSimpleName());
            
            auditService.logAction(currentUser, action + "_FAILED", entityType, entityId, oldState, errorState);
            
            // Re-throw the exception
            throw e;
        }
    }

    /**
     * Get the current authenticated user from the security context.
     * 
     * @return the current user, or null if not authenticated
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                return userPrincipal.getUser();
            }
        } catch (Exception e) {
            logger.debug("Failed to get current user from security context", e);
        }
        return null;
    }

    /**
     * Capture the state of an object for audit logging.
     * Converts the object to a map representation suitable for JSON serialization.
     * 
     * @param object the object to capture
     * @return a map representation of the object's state
     */
    private Object captureState(Object object) {
        if (object == null) {
            return null;
        }
        
        // If it's an array (method arguments), capture the first relevant entity
        if (object instanceof Object[]) {
            Object[] args = (Object[]) object;
            for (Object arg : args) {
                if (isEntity(arg)) {
                    return captureEntityState(arg);
                }
            }
            // If no entity found, return a simple representation
            return createArgumentsMap(args);
        }
        
        // If it's a single entity, capture its state
        if (isEntity(object)) {
            return captureEntityState(object);
        }
        
        // For other types, return as-is (will be serialized by AuditService)
        return object;
    }

    /**
     * Check if an object is an entity (has an ID field).
     * 
     * @param object the object to check
     * @return true if the object is an entity, false otherwise
     */
    private boolean isEntity(Object object) {
        if (object == null) {
            return false;
        }
        
        try {
            // Check if the object has a getId() method
            Method getIdMethod = object.getClass().getMethod("getId");
            return getIdMethod != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Capture the state of an entity as a map.
     * 
     * @param entity the entity to capture
     * @return a map representation of the entity's state
     */
    private Map<String, Object> captureEntityState(Object entity) {
        Map<String, Object> state = new HashMap<>();
        
        try {
            // Use reflection to get all getter methods
            Method[] methods = entity.getClass().getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                
                // Only capture getter methods (excluding getClass and complex relationships)
                if (methodName.startsWith("get") && 
                    !methodName.equals("getClass") &&
                    method.getParameterCount() == 0 &&
                    !isComplexType(method.getReturnType())) {
                    
                    try {
                        Object value = method.invoke(entity);
                        String fieldName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
                        state.put(fieldName, value);
                    } catch (Exception e) {
                        // Skip fields that can't be accessed
                        logger.debug("Failed to capture field {} from entity", methodName, e);
                    }
                }
            }
            
            // Add entity type
            state.put("_entityType", entity.getClass().getSimpleName());
            
        } catch (Exception e) {
            logger.error("Failed to capture entity state", e);
            state.put("error", "Failed to capture state: " + e.getMessage());
        }
        
        return state;
    }

    /**
     * Check if a type is complex (collection, map, or custom entity).
     * Complex types are excluded from state capture to avoid circular references.
     * 
     * @param type the type to check
     * @return true if the type is complex, false otherwise
     */
    private boolean isComplexType(Class<?> type) {
        return java.util.Collection.class.isAssignableFrom(type) ||
               java.util.Map.class.isAssignableFrom(type) ||
               (type.getName().startsWith("com.bansaiyai.bansaiyai.entity") && 
                !type.isEnum());
    }

    /**
     * Create a simple map representation of method arguments.
     * 
     * @param args the method arguments
     * @return a map representation of the arguments
     */
    private Map<String, Object> createArgumentsMap(Object[] args) {
        Map<String, Object> argsMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && !isComplexType(args[i].getClass())) {
                argsMap.put("arg" + i, args[i]);
            }
        }
        return argsMap;
    }

    /**
     * Extract the entity type from method arguments.
     * Looks for the first entity parameter and returns its simple class name.
     * 
     * @param args the method arguments
     * @return the entity type, or "Unknown" if not found
     */
    private String extractEntityType(Object[] args) {
        for (Object arg : args) {
            if (arg != null && isEntity(arg)) {
                return arg.getClass().getSimpleName();
            }
        }
        
        // Check for Long ID parameters (common pattern)
        for (Object arg : args) {
            if (arg instanceof Long) {
                // Try to infer from method name or context
                return "Entity";
            }
        }
        
        return "Unknown";
    }

    /**
     * Extract the entity ID from method arguments.
     * Looks for the first Long parameter or entity with an ID.
     * 
     * @param args the method arguments
     * @return the entity ID, or null if not found
     */
    private Long extractEntityId(Object[] args) {
        // First, check for entity objects with IDs
        for (Object arg : args) {
            if (arg != null && isEntity(arg)) {
                try {
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(arg);
                    if (id instanceof Long) {
                        return (Long) id;
                    }
                } catch (Exception e) {
                    logger.debug("Failed to extract ID from entity", e);
                }
            }
        }
        
        // If no entity found, look for Long parameters (common for ID-based operations)
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        
        return null;
    }
}
