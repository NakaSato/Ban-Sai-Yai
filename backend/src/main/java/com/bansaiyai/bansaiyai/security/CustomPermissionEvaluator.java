package com.bansaiyai.bansaiyai.security;

import com.bansaiyai.bansaiyai.entity.Payment;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom permission evaluator for Spring Security integration.
 * Implements method-level security with permission checks and Separation of Duties (SoD) enforcement.
 * 
 * Requirements: 2.2, 15.3
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final RolePermissionService rolePermissionService;

    /**
     * Evaluates whether the authenticated user has the specified permission on the target domain object.
     * 
     * @param authentication the authentication object containing user details
     * @param targetDomainObject the domain object being accessed (can be null for general permission checks)
     * @param permission the permission to check (e.g., "loan.approve", "transaction.create")
     * @return true if the user has the permission, false otherwise
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        if (permission == null) {
            log.warn("Permission is null");
            return false;
        }

        String permissionSlug = permission.toString();
        
        // Get the user from authentication
        User user = extractUser(authentication);
        if (user == null) {
            log.warn("Could not extract user from authentication");
            return false;
        }

        // Check if user has the permission through their role
        boolean hasPermission = rolePermissionService.hasPermission(user, permissionSlug);
        
        log.debug("User {} {} permission {}", 
                user.getUsername(), 
                hasPermission ? "has" : "does not have", 
                permissionSlug);
        
        return hasPermission;
    }

    /**
     * Evaluates whether the authenticated user has the specified permission on a target identified by ID and type.
     * 
     * @param authentication the authentication object containing user details
     * @param targetId the ID of the target object
     * @param targetType the type of the target object (e.g., "Loan", "Transaction")
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        if (permission == null) {
            log.warn("Permission is null");
            return false;
        }

        String permissionSlug = permission.toString();
        
        // Get the user from authentication
        User user = extractUser(authentication);
        if (user == null) {
            log.warn("Could not extract user from authentication");
            return false;
        }

        // Check if user has the permission through their role
        boolean hasPermission = rolePermissionService.hasPermission(user, permissionSlug);
        
        log.debug("User {} {} permission {} for {} with ID {}", 
                user.getUsername(), 
                hasPermission ? "has" : "does not have", 
                permissionSlug,
                targetType,
                targetId);
        
        return hasPermission;
    }

    /**
     * Checks if a user can approve their own transaction (Separation of Duties enforcement).
     * This method enforces the SoD principle by preventing users from approving transactions they created.
     * 
     * @param user the user attempting to approve
     * @param transaction the transaction to be approved (Payment entity)
     * @return false if the user created the transaction, true otherwise
     * 
     * Requirements: 15.3 - Self-approval denial
     */
    public boolean canApproveOwnTransaction(User user, Payment transaction) {
        if (user == null || transaction == null) {
            log.warn("User or transaction is null");
            return false;
        }

        // Check if the user is the creator of the transaction
        String createdBy = transaction.getCreatedBy();
        if (createdBy == null) {
            log.debug("Transaction has no creator recorded, allowing approval");
            return true;
        }

        boolean isSelfApproval = user.getUsername().equals(createdBy);
        
        if (isSelfApproval) {
            log.warn("User {} attempted to approve their own transaction (ID: {}). Self-approval denied.",
                    user.getUsername(), transaction.getId());
            return false;
        }

        log.debug("User {} can approve transaction {} (not self-approval)",
                user.getUsername(), transaction.getId());
        return true;
    }

    /**
     * Extracts the User entity from the Spring Security Authentication object.
     * 
     * @param authentication the authentication object
     * @return the User entity, or null if extraction fails
     */
    private User extractUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUser();
        } else if (principal instanceof User) {
            return (User) principal;
        } else {
            log.warn("Unknown principal type: {}", principal.getClass().getName());
            return null;
        }
    }
}
