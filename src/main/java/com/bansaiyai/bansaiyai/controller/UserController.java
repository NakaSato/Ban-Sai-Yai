package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.CreateUserRequest;
import com.bansaiyai.bansaiyai.dto.UpdateRoleRequest;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for user management operations
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;

  /**
   * Get all users with pagination
   */
  @GetMapping
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getAllUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<User> users = userService.getAllUsers(pageable);

    Map<String, Object> response = new HashMap<>();
    response.put("users", users.getContent());
    response.put("currentPage", users.getNumber());
    response.put("totalItems", users.getTotalElements());
    response.put("totalPages", users.getTotalPages());
    response.put("pageSize", users.getSize());

    return ResponseEntity.ok(response);
  }

  /**
   * Create a new user
   */
  @PostMapping
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> createUser(
      @Valid @RequestBody CreateUserRequest request,
      Authentication authentication) {

    try {
      User currentUser = getCurrentUser(authentication);
      User.Role roleEnum = User.Role.valueOf(request.getRole());

      User createdUser = userService.createUser(
          request.getUsername(),
          request.getEmail(),
          request.getPassword(),
          request.getFirstName(),
          request.getLastName(),
          request.getPhone(),
          roleEnum,
          currentUser);

      Map<String, Object> response = new HashMap<>();
      response.put("id", createdUser.getId());
      response.put("username", createdUser.getUsername());
      response.put("email", createdUser.getEmail());
      response.put("firstName", createdUser.getFirstName());
      response.put("lastName", createdUser.getLastName());
      response.put("role", createdUser.getRole());
      response.put("status", createdUser.getStatus());
      response.put("message", "User created successfully");

      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (IllegalArgumentException e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Validation failed");
      error.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (Exception e) {
      log.error("Error creating user", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to create user");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Update user role
   */
  @PutMapping("/{id}/role")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> updateUserRole(
      @PathVariable Long id,
      @Valid @RequestBody UpdateRoleRequest request,
      Authentication authentication) {

    try {
      User currentUser = getCurrentUser(authentication);

      User updatedUser = userService.updateUserRole(id, request.getRole(), currentUser);

      Map<String, Object> response = new HashMap<>();
      response.put("id", updatedUser.getId());
      response.put("username", updatedUser.getUsername());
      response.put("email", updatedUser.getEmail());
      response.put("role", updatedUser.getRole());
      response.put("message", "User role updated successfully");

      return ResponseEntity.ok(response);

    } catch (UserService.UserNotFoundException e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "User not found");
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (IllegalArgumentException e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Validation failed");
      error.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (Exception e) {
      log.error("Error updating user role", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to update user role");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Suspend a user
   */
  @PutMapping("/{id}/suspend")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> suspendUser(
      @PathVariable Long id,
      Authentication authentication) {

    try {
      User currentUser = getCurrentUser(authentication);
      User suspendedUser = userService.suspendUser(id, currentUser);

      Map<String, Object> response = new HashMap<>();
      response.put("id", suspendedUser.getId());
      response.put("username", suspendedUser.getUsername());
      response.put("status", suspendedUser.getStatus());
      response.put("message", "User suspended successfully");

      return ResponseEntity.ok(response);

    } catch (UserService.UserNotFoundException e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "User not found");
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
      log.error("Error suspending user", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to suspend user");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Soft delete a user
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> deleteUser(
      @PathVariable Long id,
      Authentication authentication) {

    try {
      User currentUser = getCurrentUser(authentication);
      userService.deleteUser(id, currentUser);

      Map<String, Object> response = new HashMap<>();
      response.put("id", id);
      response.put("message", "User deleted successfully");

      return ResponseEntity.ok(response);

    } catch (UserService.UserNotFoundException e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "User not found");
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
      log.error("Error deleting user", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal server error");
      error.put("message", "Failed to delete user");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Get user by ID
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
    try {
      User user = userService.getUserById(id);

      Map<String, Object> response = new HashMap<>();
      response.put("id", user.getId());
      response.put("username", user.getUsername());
      response.put("email", user.getEmail());
      response.put("firstName", user.getFirstName());
      response.put("lastName", user.getLastName());
      response.put("phone", user.getPhoneNumber());
      response.put("role", user.getRole());
      response.put("status", user.getStatus());

      return ResponseEntity.ok(response);

    } catch (UserService.UserNotFoundException e) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "User not found");
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
  }

  /**
   * Helper method to get current user from authentication
   * In a real implementation, you'd fetch this from database
   */
  private User getCurrentUser(Authentication authentication) {
    // This is a simplified approach. In a real application, you'd:
    // 1. Get the current user from database
    // 2. Return the actual User object

    // For demo purposes, we'll create a mock user
    User mockUser = new User();
    mockUser.setUsername(authentication.getName());
    mockUser.setRole(User.Role.PRESIDENT); // Default for admin operations

    return mockUser;
  }
}
