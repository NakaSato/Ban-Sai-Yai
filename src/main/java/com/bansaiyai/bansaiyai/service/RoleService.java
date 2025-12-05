package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing account roles and permissions
 */
@Service
@Slf4j
public class RoleService {

  /**
   * Get all available roles in the system
   */
  public List<User.Role> getAllRoles() {
    return Arrays.asList(User.Role.values());
  }

  /**
   * Get role hierarchy (from highest to lowest privilege)
   */
  public List<User.Role> getRoleHierarchy() {
    return Arrays.asList(
        User.Role.ADMIN,
        User.Role.PRESIDENT,
        User.Role.SECRETARY,
        User.Role.OFFICER,
        User.Role.MEMBER);
  }

  /**
   * Get permissions for a specific role
   */
  public Set<String> getRolePermissions(User.Role role) {
    Set<String> permissions = new HashSet<>();

    switch (role) {
      case ADMIN:
        permissions.addAll(Set.of(
            "MEMBER_READ", "MEMBER_WRITE", "MEMBER_DELETE",
            "LOAN_READ", "LOAN_WRITE", "LOAN_DELETE", "LOAN_APPROVE",
            "SAVINGS_READ", "SAVINGS_WRITE", "SAVINGS_DELETE",
            "PAYMENT_READ", "PAYMENT_WRITE", "PAYMENT_DELETE",
            "REPORT_READ", "REPORT_WRITE", "ADMIN_READ", "ADMIN_WRITE",
            "SYSTEM_CONFIG", "USER_MANAGEMENT"));
        break;
      case PRESIDENT:
        permissions.addAll(Set.of(
            "MEMBER_READ", "MEMBER_WRITE", "MEMBER_DELETE",
            "LOAN_READ", "LOAN_WRITE", "LOAN_DELETE", "LOAN_APPROVE",
            "SAVINGS_READ", "SAVINGS_WRITE", "SAVINGS_DELETE",
            "PAYMENT_READ", "PAYMENT_WRITE", "PAYMENT_DELETE",
            "REPORT_READ", "REPORT_WRITE", "ADMIN_READ", "ADMIN_WRITE"));
        break;
      case SECRETARY:
        permissions.addAll(Set.of(
            "MEMBER_READ", "MEMBER_WRITE",
            "LOAN_READ", "LOAN_WRITE",
            "SAVINGS_READ", "SAVINGS_WRITE",
            "PAYMENT_READ", "PAYMENT_WRITE",
            "REPORT_READ", "REPORT_WRITE"));
        break;
      case OFFICER:
        permissions.addAll(Set.of(
            "MEMBER_READ", "MEMBER_WRITE",
            "LOAN_READ", "LOAN_WRITE",
            "SAVINGS_READ", "SAVINGS_WRITE",
            "PAYMENT_READ", "PAYMENT_WRITE"));
        break;
      case MEMBER:
        permissions.addAll(Set.of(
            "MEMBER_READ_SELF",
            "LOAN_READ_SELF",
            "SAVINGS_READ_SELF",
            "PAYMENT_READ_SELF"));
        break;
    }

    return permissions;
  }

  /**
   * Get role description
   */
  public String getRoleDescription(User.Role role) {
    switch (role) {
      case ADMIN:
        return "System Administrator - Full system access and user management";
      case PRESIDENT:
        return "President - Organization leadership with full operational access";
      case SECRETARY:
        return "Secretary - Record keeping and reporting responsibilities";
      case OFFICER:
        return "Officer - Daily operations and member services";
      case MEMBER:
        return "Member - Basic access to own information";
      default:
        return "Unknown role";
    }
  }

  /**
   * Check if a role can manage another role
   */
  public boolean canManageRole(User.Role managerRole, User.Role targetRole) {
    List<User.Role> hierarchy = getRoleHierarchy();
    int managerIndex = hierarchy.indexOf(managerRole);
    int targetIndex = hierarchy.indexOf(targetRole);

    return managerIndex >= 0 && targetIndex >= 0 && managerIndex < targetIndex;
  }

  /**
   * Get all permissions available in the system
   */
  public Set<String> getAllPermissions() {
    Set<String> allPermissions = new HashSet<>();

    for (User.Role role : User.Role.values()) {
      allPermissions.addAll(getRolePermissions(role));
    }

    return allPermissions;
  }

  /**
   * Get roles by permission
   */
  public List<User.Role> getRolesWithPermission(String permission) {
    List<User.Role> roles = new ArrayList<>();

    for (User.Role role : User.Role.values()) {
      if (getRolePermissions(role).contains(permission)) {
        roles.add(role);
      }
    }

    return roles;
  }

  /**
   * Register all roles in the system (for initialization purposes)
   */
  public void registerAllRoles() {
    log.info("Registering all account roles in the system");

    for (User.Role role : User.Role.values()) {
      Set<String> permissions = getRolePermissions(role);
      log.info("Registered role: {} with {} permissions", role, permissions.size());
    }

    log.info("All {} roles successfully registered", User.Role.values().length);
  }

  /**
   * Validate role assignment
   */
  public boolean isValidRoleAssignment(User.Role currentRole, User.Role targetRole) {
    // Only ADMIN and PRESIDENT can assign any role
    if (currentRole == User.Role.ADMIN || currentRole == User.Role.PRESIDENT) {
      return true;
    }

    // SECRETARY can assign OFFICER and MEMBER roles
    if (currentRole == User.Role.SECRETARY) {
      return targetRole == User.Role.OFFICER || targetRole == User.Role.MEMBER;
    }

    // OFFICER can only assign MEMBER role
    if (currentRole == User.Role.OFFICER) {
      return targetRole == User.Role.MEMBER;
    }

    // MEMBER cannot assign roles
    return false;
  }
}
