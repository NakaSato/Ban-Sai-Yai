package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for role management operations
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

  private final RoleService roleService;

  /**
   * Get all available roles in the system
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT', 'SECRETARY')")
  public ResponseEntity<Map<String, Object>> getAllRoles() {
    List<User.Role> roles = roleService.getAllRoles();
    List<Map<String, Object>> roleDetails = roles.stream()
        .map(role -> {
          Map<String, Object> roleInfo = new HashMap<>();
          roleInfo.put("name", role.name());
          roleInfo.put("description", roleService.getRoleDescription(role));
          roleInfo.put("permissions", roleService.getRolePermissions(role));
          return roleInfo;
        })
        .collect(Collectors.toList());

    Map<String, Object> response = new HashMap<>();
    response.put("roles", roleDetails);
    response.put("total", roles.size());

    return ResponseEntity.ok(response);
  }

  /**
   * Get role hierarchy
   */
  @GetMapping("/hierarchy")
  @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getRoleHierarchy() {
    List<User.Role> hierarchy = roleService.getRoleHierarchy();

    Map<String, Object> response = new HashMap<>();
    response.put("hierarchy", hierarchy);
    response.put("description", "Role hierarchy from highest to lowest privilege");

    return ResponseEntity.ok(response);
  }

  /**
   * Get permissions for a specific role
   */
  @GetMapping("/{role}/permissions")
  @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT', 'SECRETARY')")
  public ResponseEntity<Map<String, Object>> getRolePermissions(@PathVariable User.Role role) {
    Set<String> permissions = roleService.getRolePermissions(role);
    String description = roleService.getRoleDescription(role);

    Map<String, Object> response = new HashMap<>();
    response.put("role", role.name());
    response.put("description", description);
    response.put("permissions", permissions);
    response.put("permissionCount", permissions.size());

    return ResponseEntity.ok(response);
  }

  /**
   * Get all permissions available in the system
   */
  @GetMapping("/permissions")
  @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getAllPermissions() {
    Set<String> permissions = roleService.getAllPermissions();

    Map<String, Object> response = new HashMap<>();
    response.put("permissions", new ArrayList<>(permissions));
    response.put("total", permissions.size());

    return ResponseEntity.ok(response);
  }

  /**
   * Get roles that have a specific permission
   */
  @GetMapping("/by-permission/{permission}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getRolesByPermission(@PathVariable String permission) {
    List<User.Role> roles = roleService.getRolesWithPermission(permission);

    Map<String, Object> response = new HashMap<>();
    response.put("permission", permission);
    response.put("roles", roles);
    response.put("count", roles.size());

    return ResponseEntity.ok(response);
  }

  /**
   * Check if a role can manage another role
   */
  @GetMapping("/{managerRole}/can-manage/{targetRole}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT', 'SECRETARY')")
  public ResponseEntity<Map<String, Object>> canManageRole(
      @PathVariable User.Role managerRole,
      @PathVariable User.Role targetRole) {

    boolean canManage = roleService.canManageRole(managerRole, targetRole);

    Map<String, Object> response = new HashMap<>();
    response.put("managerRole", managerRole.name());
    response.put("targetRole", targetRole.name());
    response.put("canManage", canManage);

    return ResponseEntity.ok(response);
  }

  /**
   * Validate role assignment
   */
  @PostMapping("/validate-assignment")
  @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT', 'SECRETARY', 'OFFICER')")
  public ResponseEntity<Map<String, Object>> validateRoleAssignment(
      @RequestBody Map<String, String> request) {

    // Get current user's role
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    // For now, we'll use a simplified approach - in a real implementation,
    // you'd fetch the current user's role from the database
    User.Role currentRole = getCurrentUserRole(authentication);
    User.Role targetRole = User.Role.valueOf(request.get("targetRole").toUpperCase());

    boolean isValid = roleService.isValidRoleAssignment(currentRole, targetRole);

    Map<String, Object> response = new HashMap<>();
    response.put("currentRole", currentRole.name());
    response.put("targetRole", targetRole.name());
    response.put("isValid", isValid);
    response.put("message",
        isValid ? "Role assignment is valid" : "Current role does not have permission to assign this role");

    return ResponseEntity.ok(response);
  }

  /**
   * Get role statistics
   */
  @GetMapping("/statistics")
  @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getRoleStatistics() {
    List<User.Role> allRoles = roleService.getAllRoles();

    Map<String, Object> statistics = new HashMap<>();
    statistics.put("totalRoles", allRoles.size());
    statistics.put("roleNames", allRoles.stream().map(Enum::name).collect(Collectors.toList()));

    // Count permissions per role
    Map<String, Integer> permissionCounts = new HashMap<>();
    for (User.Role role : allRoles) {
      permissionCounts.put(role.name(), roleService.getRolePermissions(role).size());
    }
    statistics.put("permissionCounts", permissionCounts);

    // Get all unique permissions
    Set<String> allPermissions = roleService.getAllPermissions();
    statistics.put("totalPermissions", allPermissions.size());

    return ResponseEntity.ok(statistics);
  }

  /**
   * Helper method to get current user's role (simplified version)
   * In a real implementation, you'd fetch this from the database
   */
  private User.Role getCurrentUserRole(Authentication authentication) {
    // This is a simplified approach. In a real application, you'd:
    // 1. Get the current user from the database
    // 2. Return their actual role

    // For demo purposes, we'll extract role from authorities
    return authentication.getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .filter(auth -> auth.startsWith("ROLE_"))
        .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
        .map(roleStr -> {
          try {
            return User.Role.valueOf(roleStr);
          } catch (IllegalArgumentException e) {
            return User.Role.MEMBER; // Default fallback
          }
        })
        .findFirst()
        .orElse(User.Role.MEMBER);
  }
}
