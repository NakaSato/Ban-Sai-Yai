package com.bansaiyai.bansaiyai.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be automatically audited.
 * When applied to a method, the AuditAspect will automatically log the action
 * with old and new values for compliance and investigation purposes.
 * 
 * Requirements: 11.1, 11.2
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    
    /**
     * The action name to log. If not specified, the method name will be used.
     * 
     * @return the action name
     */
    String action() default "";
    
    /**
     * The entity type being affected. If not specified, it will be extracted
     * from method parameters.
     * 
     * @return the entity type
     */
    String entityType() default "";
}
