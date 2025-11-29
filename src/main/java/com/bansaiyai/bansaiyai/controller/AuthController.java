package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.LoginRequest;
import com.bansaiyai.bansaiyai.dto.LoginResponse;
import com.bansaiyai.bansaiyai.dto.SignUpRequest;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.security.JwtUtils;
import com.bansaiyai.bansaiyai.security.UserPrincipal;
import com.bansaiyai.bansaiyai.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private AuthService authService;

  @Autowired
  private JwtUtils jwtUtils;

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    String jwt = jwtUtils.generateTokenFromUsername(
        userPrincipal.getUsername(),
        List.of("ROLE_" + userPrincipal.getRole().name()),
        userPrincipal.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .toList());

    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setToken(jwt);
    loginResponse.setType("Bearer");
    loginResponse.setId(userPrincipal.getId());
    loginResponse.setUsername(userPrincipal.getUsername());
    loginResponse.setEmail(userPrincipal.getEmail());
    loginResponse.setRole(userPrincipal.getRole().name());
    loginResponse.setPermissions(userPrincipal.getAuthorities().stream()
        .map(grantedAuthority -> grantedAuthority.getAuthority())
        .toList());

    return ResponseEntity.ok(loginResponse);
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
    try {
      User user = authService.registerUser(signUpRequest);

      LoginResponse response = new LoginResponse();
      response.setMessage("User registered successfully");
      response.setUserId(user.getId());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> logoutUser() {
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("token");

    if (refreshToken != null && jwtUtils.validateToken(refreshToken)) {
      String newToken = jwtUtils.refreshToken(refreshToken);
      return ResponseEntity.ok(Map.of("token", newToken));
    } else {
      return ResponseEntity.badRequest().body(Map.of("message", "Invalid refresh token"));
    }
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return ResponseEntity.ok(Map.of(
        "id", userPrincipal.getId(),
        "username", userPrincipal.getUsername(),
        "email", userPrincipal.getEmail(),
        "role", userPrincipal.getRole().name(),
        "permissions", userPrincipal.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .toList()));
  }

  @GetMapping("/check-username/{username}")
  public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
    Boolean isAvailable = authService.isUsernameAvailable(username);
    return ResponseEntity.ok(Map.of("available", isAvailable));
  }

  @GetMapping("/check-email/{email}")
  public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
    Boolean isAvailable = authService.isEmailAvailable(email);
    return ResponseEntity.ok(Map.of("available", isAvailable));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
    try {
      authService.initiatePasswordReset(request.get("email"));
      return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
    try {
      authService.resetPassword(
          request.get("token"),
          request.get("newPassword"),
          request.get("confirmPassword"));
      return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }
}
