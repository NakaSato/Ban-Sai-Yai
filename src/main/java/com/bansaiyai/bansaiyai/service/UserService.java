package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.repository.RoleRepository;
import com.bansaiyai.bansaiyai.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuditService auditService;

  /**
   * Get all users with pagination
   */
  public Page<User> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  /**
   * Get user by ID
   */
  public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
  }

  /**
   * Create a new user
   */
  public User createUser(String username, String email, String password, String firstName, String lastName,
      String phone, User.Role role, User createdBy) {

    // Validate role assignment
    validateRoleChange(createdBy.getRole(), role);

    // Check if username or email already exists
    if (userRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("Username already exists: " + username);
    }

    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already exists: " + email);
    }

    // Get role entity
    com.bansaiyai.bansaiyai.entity.Role roleEntity = roleRepository.findByRoleName("ROLE_" + role.name())
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + role));

    // Hash password
    String hashedPassword = passwordEncoder.encode(password);

    User newUser = User.builder()
        .username(username)
        .email(email)
        .password(hashedPassword)
        .firstName(firstName)
        .lastName(lastName)
        .phoneNumber(phone)
        .role(role)
        .rbacRole(roleEntity)
        .status(User.UserStatus.ACTIVE)
        .enabled(true)
        .build();

    User savedUser = userRepository.save(newUser);

    // Log the action
    auditService.logAction(
        createdBy,
        "USER_CREATED",
        "User",
        savedUser.getId(),
        null,
        savedUser);

    log.info("Created new user: {} with role: {}", username, role);
    return savedUser;
  }

  /**
   * Update user role
   */
  public User updateUserRole(Long userId, String newRoleName, User updatedBy) {
    User user = getUserById(userId);
    User.Role oldRole = user.getRole();
    User.Role newRole = User.Role.valueOf(newRoleName);

    // Validate role assignment
    validateRoleChange(updatedBy.getRole(), newRole);

    // Get new role entity
    com.bansaiyai.bansaiyai.entity.Role roleEntity = roleRepository.findByRoleName("ROLE_" + newRole.name())
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + newRole));

    // Update role
    user.setRole(newRole);
    user.setRbacRole(roleEntity);

    User savedUser = userRepository.save(user);

    // Log the role change
    auditService.logRoleChange(
        user,
        oldRole.name(),
        newRole.name(),
        updatedBy);

    log.info("Updated user {} role from {} to {}", user.getUsername(), oldRole, newRole);
    return savedUser;
  }

  /**
   * Suspend user
   */
  public User suspendUser(Long userId, User suspendedBy) {
    User user = getUserById(userId);

    if (user.getStatus() == User.UserStatus.SUSPENDED) {
      throw new IllegalArgumentException("User is already suspended: " + user.getUsername());
    }

    user.setStatus(User.UserStatus.SUSPENDED);
    user.setAccountNonLocked(false);

    User savedUser = userRepository.save(user);

    // Log the suspension
    auditService.logAction(
        suspendedBy,
        "USER_SUSPENDED",
        "User",
        userId,
        user,
        savedUser);

    log.info("Suspended user: {}", user.getUsername());
    return savedUser;
  }

  /**
   * Delete user (soft delete)
   */
  public void deleteUser(Long userId, User deletedBy) {
    User user = getUserById(userId);

    user.setDeletedAt(LocalDateTime.now());
    user.setStatus(User.UserStatus.SUSPENDED);
    user.setEnabled(false);
    user.setAccountNonLocked(false);

    userRepository.save(user);

    // Log the deletion
    auditService.logAction(
        deletedBy,
        "USER_DELETED",
        "User",
        userId,
        user,
        null);

    log.info("Deleted user: {}", user.getUsername());
  }

  /**
   * Validate role change
   */
  private void validateRoleChange(User.Role currentUserRole, User.Role targetRole) {
    // Only ADMIN or PRESIDENT can manage users
    if (currentUserRole != User.Role.ADMIN && currentUserRole != User.Role.PRESIDENT) {
      throw new IllegalArgumentException("Current role does not have permission to manage users");
    }

    // Cannot assign ADMIN role (only system can do this)
    if (targetRole == User.Role.ADMIN) {
      throw new IllegalArgumentException("Cannot assign ADMIN role through user management");
    }
  }

  /**
   * Check if username exists
   */
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  /**
   * Check if email exists
   */
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  /**
   * Find user by username
   */
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  /**
   * Exception for user not found
   */
  public static class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
      super(message);
    }
  }
}
