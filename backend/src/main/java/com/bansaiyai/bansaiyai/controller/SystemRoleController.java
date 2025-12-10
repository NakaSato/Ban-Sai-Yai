package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.PermissionRequest;
import com.bansaiyai.bansaiyai.dto.RoleDto;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.RolePermissionService;
import com.bansaiyai.bansaiyai.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for role management operations
 */
@RestController
@RequestMapping({ "/api/roles", "/roles" })
@Slf4j
public class SystemRoleController {

  @jakarta.annotation.PostConstruct
  public void init() {
    log.info("SystemRoleController INITIALIZED");
  }

  private final RoleService roleService;
  private final RolePermissionService rolePermissionService;

  public SystemRoleController(RoleService roleService, RolePermissionService rolePermissionService) {
    this.roleService = roleService;
    this.rolePermissionService = rolePermissionService;
    log.debug("SystemRoleController Constructor Called with: {}, {}", roleService, rolePermissionService);
  }

  /**
   * Get all available roles in system
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
  public ResponseEntity<Map<String, Object>> getAllRoles() {
    // Get roles from database entities using RolePermissionService
    Map<String, Set<String>> rolePermissionMatrix = rolePermissionService.getRolePermissionMatrix();
    List<RoleDto> roleDetails = rolePermissionMatrix.entrySet().stream()
        .map(entry -> {
          String roleName = entry.getKey();
          Set<String> permissions = entry.getValue();

          return RoleDto.builder()
              .id(0L) // Will be set when we get from proper repository
              .roleName(roleName)
              .description(getRoleDescriptionFromName(roleName))
              .permissions(permissions.stream()
                  .map(perm -> RoleDto.PermissionDto.builder()
                      .id(0L) // Will be set when we get from proper repository
                      .permissionSlug(perm)
                      .description("")
                      .build())
                  .collect(Collectors.toList()))
              .build();
        })
        .collect(Collectors.toList());

    Map<String, Object> response = new HashMap<>();
    response.put("roles", roleDetails);
    response.put("total", roleDetails.size());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/hierarchy")
  @PreAuthorize("hasRole('PRESIDENT')")
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
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
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
  @PreAuthorize("hasRole('PRESIDENT')")
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
  @PreAuthorize("hasRole('PRESIDENT')")
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
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
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
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
  public ResponseEntity<Map<String, Object>> validateRoleAssignment(
      @RequestBody Map<String, String> request) {

    // Get current user's role
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // For now, we'll use a simplified approach - in a real implementation,
    // you'd fetch the current user's role from the database
    User.Role currentRole = getCurrentUserRole(authentication);
    User.Role targetRole = User.Role.valueOf(request.get("targetRole").toUpperCase());

    // Only PRESIDENT can assign roles
    // Actually, targetRole parsing might fail if ADMIN is passed. Logic upstream
    // handles "canManage".
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
   * Get permissions for a specific role by ID
   */
  @GetMapping("/by-id/{id}/permissions")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
  public ResponseEntity<Map<String, Object>> getRolePermissionsById(@PathVariable Integer id) {
    try {
      String roleName = "ROLE_" + User.Role.values()[id].name();
      Set<String> permissions = rolePermissionService.getPermissionsForRole(roleName);

      Map<String, Object> response = new HashMap<>();
      response.put("roleId", id);
      response.put("roleName", roleName);
      response.put("permissions", permissions);
      response.put("permissionCount", permissions.size());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Role not found");
      error.put("message", "Role with ID " + id + " does not exist");
      return ResponseEntity.badRequest().body(error);
    }
  }

  /**
   * Add permission to role
   */
  @PostMapping("/{id}/permissions")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> addPermissionToRole(
      @PathVariable Integer id,
      @Valid @RequestBody PermissionRequest request) {

    try {
      String roleName = "ROLE_" + User.Role.values()[id].name();

      // Add permission to role
      rolePermissionService.addPermissionToRole(roleName, request.getPermissionSlug());

      log.info("Added permission {} to role {}", request.getPermissionSlug(), roleName);

      Map<String, Object> response = new HashMap<>();
      response.put("roleId", id);
      response.put("roleName", roleName);
      response.put("permissionSlug", request.getPermissionSlug());
      response.put("message", "Permission successfully added to role");

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Validation failed");
      error.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (Exception e) {
      log.error("Error adding permission to role", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to add permission to role");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Remove permission from role
   */
  @DeleteMapping("/{id}/permissions/{permId}")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> removePermissionFromRole(
      @PathVariable Integer id,
      @PathVariable Integer permId) {

    try {
      String roleName = "ROLE_" + User.Role.values()[id].name();

      // For simplicity, we'll use permId as permission slug - in real implementation
      // you'd fetch the actual permission from database
      String permissionSlug = "permission_" + permId;

      // Remove permission from role
      rolePermissionService.removePermissionFromRole(roleName, permissionSlug);

      log.info("Removed permission {} from role {}", permissionSlug, roleName);

      Map<String, Object> response = new HashMap<>();
      response.put("roleId", id);
      response.put("roleName", roleName);
      response.put("permissionId", permId);
      response.put("permissionSlug", permissionSlug);
      response.put("message", "Permission successfully removed from role");

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Validation failed");
      error.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (Exception e) {
      log.error("Error removing permission from role", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to remove permission from role");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Get role statistics
   */
  @GetMapping("/statistics")
  @PreAuthorize("hasRole('PRESIDENT')")
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
   * Helper method to get role description from role name
   */
  private String getRoleDescriptionFromName(String roleName) {
    switch (roleName.replace("ROLE_", "")) {

      case "PRESIDENT":
        return "President - Organization leadership with full operational access";
      case "SECRETARY":
        return "Secretary - Record keeping and reporting responsibilities";
      case "OFFICER":
        return "Officer - Daily operations and member services";
      case "MEMBER":
        return "Member - Basic access to own information";
      default:
        return "Unknown role";
    }
  }

  /**
   * Helper method to get current user's role (simplified version)
   * In a real implementation, you'd fetch this from database
   */
  private User.Role getCurrentUserRole(Authentication authentication) {
    // This is a simplified approach. In a real application, you'd:
    // 1. Get the current user from database
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
