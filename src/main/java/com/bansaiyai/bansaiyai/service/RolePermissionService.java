package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Permission;
import com.bansaiyai.bansaiyai.entity.Role;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.PermissionRepository;
import com.bansaiyai.bansaiyai.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing role-permission mappings and permission checks.
 * Implements caching for performance optimization.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Get all permissions for a specific role.
     * Results are cached for performance.
     * 
     * @param roleName the name of the role (e.g., "ROLE_OFFICER")
     * @return a set of permission slugs associated with the role
     * @throws IllegalArgumentException if the role is not found
     */
    @Cacheable(value = "rolePermissions", key = "#roleName")
    public Set<String> getPermissionsForRole(String roleName) {
        log.debug("Loading permissions for role: {}", roleName);

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        Set<String> permissionSlugs = role.getPermissions().stream()
                .map(Permission::getPermSlug)
                .collect(Collectors.toSet());

        log.debug("Loaded {} permissions for role {}", permissionSlugs.size(), roleName);
        return permissionSlugs;
    }

    /**
     * Check if a user has a specific permission.
     * 
     * @param user           the user to check
     * @param permissionSlug the permission slug to check (e.g., "loan.approve")
     * @return true if the user's role has the permission, false otherwise
     */
    public boolean hasPermission(User user, String permissionSlug) {
        if (user == null || user.getRbacRole() == null) {
            log.warn("User or user role is null");
            return false;
        }

        String roleName = user.getRbacRole().getRoleName();
        Set<String> permissions = getPermissionsForRole(roleName);

        boolean hasPermission = permissions.contains(permissionSlug);
        log.debug("User {} with role {} {} permission {}",
                user.getUsername(), roleName,
                hasPermission ? "has" : "does not have",
                permissionSlug);

        return hasPermission;
    }

    /**
     * Add a permission to a role.
     * Invalidates the cache for the role.
     * 
     * @param roleName       the name of the role
     * @param permissionSlug the permission slug to add
     * @throws IllegalArgumentException if the role or permission is not found
     */
    @Transactional
    @CacheEvict(value = "rolePermissions", key = "#roleName")
    public void addPermissionToRole(String roleName, String permissionSlug) {
        log.info("Adding permission {} to role {}", permissionSlug, roleName);

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        Permission permission = permissionRepository.findByPermSlug(permissionSlug)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionSlug));

        // Add permission to role's permission set
        role.getPermissions().add(permission);
        roleRepository.save(role);

        log.info("Successfully added permission {} to role {}", permissionSlug, roleName);
    }

    /**
     * Remove a permission from a role.
     * Invalidates the cache for the role.
     * 
     * @param roleName       the name of the role
     * @param permissionSlug the permission slug to remove
     * @throws IllegalArgumentException if the role or permission is not found
     */
    @Transactional
    @CacheEvict(value = "rolePermissions", key = "#roleName")
    public void removePermissionFromRole(String roleName, String permissionSlug) {
        log.info("Removing permission {} from role {}", permissionSlug, roleName);

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        Permission permission = permissionRepository.findByPermSlug(permissionSlug)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionSlug));

        // Remove permission from role's permission set
        role.getPermissions().remove(permission);
        roleRepository.save(role);

        log.info("Successfully removed permission {} from role {}", permissionSlug, roleName);
    }

    /**
     * Get the complete role-permission matrix.
     * 
     * @return a map of role names to their permission slugs
     */
    public Map<String, Set<String>> getRolePermissionMatrix() {
        log.debug("Building role-permission matrix");

        List<Role> allRoles = roleRepository.findAll();
        Map<String, Set<String>> matrix = new HashMap<>();

        for (Role role : allRoles) {
            Set<String> permissionSlugs = role.getPermissions().stream()
                    .map(Permission::getPermSlug)
                    .collect(Collectors.toSet());
            matrix.put(role.getRoleName(), permissionSlugs);
        }

        log.debug("Built role-permission matrix with {} roles", matrix.size());
        return matrix;
    }

    /**
     * Initialize default permissions for all roles.
     * This method is called on application startup.
     * 
     * Note: This assumes that roles and permissions are already seeded in the
     * database
     * via migration scripts. This method only ensures the mappings are correct.
     */
    @PostConstruct
    public void initializeDefaultPermissions() {
        log.info("Initializing default role-permission mappings");

        try {
            // Verify that all expected roles exist
            List<String> expectedRoles = Arrays.asList(
                    "ROLE_OFFICER", "ROLE_SECRETARY", "ROLE_PRESIDENT", "ROLE_MEMBER");

            for (String roleName : expectedRoles) {
                Optional<Role> role = roleRepository.findByRoleName(roleName);
                if (role.isPresent()) {
                    int permissionCount = role.get().getPermissions().size();
                    log.info("Role {} has {} permissions", roleName, permissionCount);
                } else {
                    log.warn("Expected role {} not found in database", roleName);
                }
            }

            log.info("Default role-permission initialization complete");
        } catch (Exception e) {
            log.error("Error during role-permission initialization", e);
        }
    }
}
