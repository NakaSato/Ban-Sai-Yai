package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.LoginRequest;
import com.bansaiyai.bansaiyai.dto.LoginResponse;
import com.bansaiyai.bansaiyai.dto.SignUpRequest;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.security.UserPrincipal;
import com.bansaiyai.bansaiyai.service.AuthService;
import com.bansaiyai.bansaiyai.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private AuthService authService;

  @Autowired
  private TokenService tokenService;

  @Operation(summary = "Authenticate user", description = "Authenticate user with username and password, returns JWT tokens")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @ApiResponse(responseCode = "429", description = "Too many login attempts")
  })
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    String username = loginRequest.getUsername();

    try {
      // Attempt authentication
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));

      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

      List<String> authorities = userPrincipal.getAuthorities().stream()
          .map(grantedAuthority -> grantedAuthority.getAuthority())
          .toList();

      // Use TokenService to generate tokens with rememberMe parameter (Requirement
      // 2.1, 2.2)
      TokenService.TokenPair tokenPair = tokenService.generateTokens(
          userPrincipal,
          loginRequest.isRememberMe());

      LoginResponse loginResponse = new LoginResponse();
      loginResponse.setToken(tokenPair.getAccessToken());
      loginResponse.setRefreshToken(tokenPair.getRefreshToken()); // Only set if rememberMe is true
      loginResponse.setExpiresIn(tokenPair.getAccessTokenExpiresIn());
      loginResponse.setType("Bearer");
      loginResponse.setId(userPrincipal.getId());
      loginResponse.setUsername(userPrincipal.getUsername());
      loginResponse.setEmail(userPrincipal.getEmail());
      loginResponse.setRole(userPrincipal.getRole().name());
      loginResponse.setPermissions(authorities);

      return ResponseEntity.ok(loginResponse);

    } catch (AuthenticationException e) {
      // Return generic error message that doesn't reveal which credential was
      // incorrect (Requirement 5.1)
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Invalid username or password"));
    }
  }

  @Operation(summary = "Register new user", description = "Register a new user account")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Registration successful"),
      @ApiResponse(responseCode = "400", description = "Invalid request data or user already exists")
  })
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

  @Operation(summary = "Logout user", description = "Logout current user and revoke all refresh tokens")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Logout successful"),
      @ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> logoutUser(@RequestBody(required = false) Map<String, String> request) {
    try {
      // Get the authenticated user
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

      // Revoke all refresh tokens for this user (Requirement 2.5)
      tokenService.revokeAllUserTokens(userPrincipal.getId());

      // If a specific refresh token was provided, revoke it as well
      if (request != null && request.containsKey("refreshToken")) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken != null && !refreshToken.isEmpty()) {
          tokenService.revokeRefreshToken(refreshToken);
        }
      }

      // Clear security context
      SecurityContextHolder.clearContext();

      return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
    } catch (Exception e) {
      // Even if token revocation fails, clear the security context
      SecurityContextHolder.clearContext();
      return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
    }
  }

  @Operation(summary = "Refresh access token", description = "Get new access token using refresh token with token rotation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
      @ApiResponse(responseCode = "400", description = "Refresh token is required"),
      @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
  })
  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("token");

    // Validate refresh token is provided
    if (refreshToken == null || refreshToken.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "Refresh token is required"));
    }

    try {
      // Use TokenService to refresh access token with token rotation (Requirement
      // 8.1, 8.2)
      TokenService.TokenPair tokenPair = tokenService.refreshAccessToken(refreshToken);

      // Return new token pair
      return ResponseEntity.ok(Map.of(
          "token", tokenPair.getAccessToken(),
          "refreshToken", tokenPair.getRefreshToken(),
          "expiresIn", tokenPair.getAccessTokenExpiresIn(),
          "type", "Bearer"));

    } catch (RuntimeException e) {
      // Enhanced error handling for different failure scenarios (Requirement 8.3)
      String errorMessage = e.getMessage();

      if (errorMessage.contains("Invalid refresh token")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "Invalid refresh token", "code", "INVALID_TOKEN"));
      } else if (errorMessage.contains("expired") || errorMessage.contains("revoked")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "Refresh token has expired or been revoked", "code", "TOKEN_EXPIRED"));
      } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "Failed to refresh token", "code", "REFRESH_FAILED"));
      }
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
