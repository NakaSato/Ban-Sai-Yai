package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.RefreshToken;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.RefreshTokenRepository;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.security.JwtUtils;
import com.bansaiyai.bansaiyai.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing JWT token lifecycle including generation, validation,
 * refresh, and revocation of both access and refresh tokens.
 */
@Service
public class TokenService {

  private final JwtUtils jwtUtils;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  @Value("${app.jwt.access-token-expiration}")
  private long accessTokenExpirationMs;

  @Value("${app.jwt.refresh-token-expiration}")
  private long refreshTokenExpirationMs;

  public TokenService(JwtUtils jwtUtils, 
                     RefreshTokenRepository refreshTokenRepository,
                     UserRepository userRepository) {
    this.jwtUtils = jwtUtils;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
  }

  /**
   * Generate a token pair (access token + optional refresh token) for a user.
   * 
   * @param userPrincipal the authenticated user principal
   * @param rememberMe whether to generate a refresh token
   * @return TokenPair containing access token and optional refresh token
   */
  @Transactional
  public TokenPair generateTokens(UserPrincipal userPrincipal, boolean rememberMe) {
    // Generate access token with roles and permissions
    List<String> roles = List.of("ROLE_" + userPrincipal.getRole().name());
    List<String> permissions = userPrincipal.getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .filter(auth -> !auth.startsWith("ROLE_"))
        .collect(Collectors.toList());
    
    String accessToken = jwtUtils.generateTokenFromUsername(
        userPrincipal.getUsername(), 
        roles, 
        permissions
    );

    // Generate refresh token only if rememberMe is true
    String refreshTokenString = null;
    if (rememberMe) {
      refreshTokenString = generateRefreshToken(userPrincipal);
    }

    return new TokenPair(
        accessToken,
        refreshTokenString,
        accessTokenExpirationMs,
        rememberMe ? refreshTokenExpirationMs : 0
    );
  }

  /**
   * Generate and persist a refresh token for a user.
   * 
   * @param userPrincipal the user principal
   * @return the refresh token string
   */
  private String generateRefreshToken(UserPrincipal userPrincipal) {
    // Find the user entity
    User user = userRepository.findById(userPrincipal.getId())
        .orElseThrow(() -> new RuntimeException("User not found"));

    // Generate unique token string
    String tokenString = UUID.randomUUID().toString();

    // Create and save refresh token entity
    RefreshToken refreshToken = RefreshToken.builder()
        .token(tokenString)
        .user(user)
        .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
        .revoked(false)
        .build();

    refreshTokenRepository.save(refreshToken);

    return tokenString;
  }

  /**
   * Validate an access token by checking signature and expiration.
   * 
   * @param token the access token to validate
   * @return true if token is valid, false otherwise
   */
  public boolean validateToken(String token) {
    return jwtUtils.validateToken(token);
  }

  /**
   * Refresh an access token using a refresh token.
   * Implements token rotation by issuing a new refresh token and revoking the old one.
   * 
   * @param refreshTokenString the refresh token
   * @return new TokenPair with fresh access and refresh tokens
   * @throws RuntimeException if refresh token is invalid, expired, or revoked
   */
  @Transactional
  public TokenPair refreshAccessToken(String refreshTokenString) {
    // Find and validate refresh token
    RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
        .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    if (!refreshToken.isValid()) {
      throw new RuntimeException("Refresh token is expired or revoked");
    }

    // Get user and create UserPrincipal
    User user = refreshToken.getUser();
    UserPrincipal userPrincipal = UserPrincipal.create(user);

    // Revoke the old refresh token (token rotation)
    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);

    // Generate new token pair with new refresh token
    return generateTokens(userPrincipal, true);
  }

  /**
   * Revoke a specific refresh token.
   * 
   * @param refreshTokenString the refresh token to revoke
   */
  @Transactional
  public void revokeRefreshToken(String refreshTokenString) {
    Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenString);
    
    if (refreshTokenOpt.isPresent()) {
      RefreshToken refreshToken = refreshTokenOpt.get();
      refreshToken.setRevoked(true);
      refreshTokenRepository.save(refreshToken);
    }
  }

  /**
   * Revoke all refresh tokens for a specific user.
   * Used during logout to invalidate all sessions.
   * 
   * @param userId the user ID
   */
  @Transactional
  public void revokeAllUserTokens(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    refreshTokenRepository.revokeAllTokensByUser(user);
  }

  /**
   * Clean up expired refresh tokens from the database.
   * Should be called periodically by a scheduled task.
   */
  @Transactional
  public void cleanupExpiredTokens() {
    refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
  }

  /**
   * Data class representing a pair of access and refresh tokens.
   */
  public static class TokenPair {
    private final String accessToken;
    private final String refreshToken;
    private final long accessTokenExpiresIn;
    private final long refreshTokenExpiresIn;

    public TokenPair(String accessToken, String refreshToken, 
                    long accessTokenExpiresIn, long refreshTokenExpiresIn) {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
      this.accessTokenExpiresIn = accessTokenExpiresIn;
      this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public String getRefreshToken() {
      return refreshToken;
    }

    public long getAccessTokenExpiresIn() {
      return accessTokenExpiresIn;
    }

    public long getRefreshTokenExpiresIn() {
      return refreshTokenExpiresIn;
    }
  }
}
