package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a refresh token for maintaining user sessions.
 * Refresh tokens are long-lived tokens used to obtain new access tokens
 * without requiring the user to re-authenticate.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token", columnList = "token"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

  @Column(nullable = false, unique = true, length = 512)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean revoked = false;

  /**
   * Check if the refresh token is expired.
   * @return true if the token has expired, false otherwise
   */
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  /**
   * Check if the refresh token is valid (not expired and not revoked).
   * @return true if the token is valid, false otherwise
   */
  public boolean isValid() {
    return !isExpired() && !revoked;
  }
}
