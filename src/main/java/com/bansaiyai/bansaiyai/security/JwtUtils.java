package com.bansaiyai.bansaiyai.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtils {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expiration}")
  private int jwtExpirationMs;

  private SecretKey getSigningKey() {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateTokenFromUsername(String username) {
    return Jwts.builder()
        .subject(username)
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS)))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateTokenFromUsername(String username, List<String> roles, List<String> permissions) {
    return Jwts.builder()
        .subject(username)
        .claim("roles", roles)
        .claim("permissions", permissions)
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS)))
        .signWith(getSigningKey())
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public List<String> getRolesFromToken(String token) {
    return getClaimFromToken(token, claims -> claims.get("roles", List.class));
  }

  public List<String> getPermissionsFromToken(String token) {
    return getClaimFromToken(token, claims -> claims.get("permissions", List.class));
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  public Boolean validateToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token);
      return true;
    } catch (MalformedJwtException e) {
      System.err.println("Invalid JWT token: " + e.getMessage());
    } catch (ExpiredJwtException e) {
      System.err.println("JWT token is expired: " + e.getMessage());
    } catch (UnsupportedJwtException e) {
      System.err.println("JWT token is unsupported: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.err.println("JWT claims string is empty: " + e.getMessage());
    }
    return false;
  }

  public String refreshToken(String token) {
    final Claims claims = getAllClaimsFromToken(token);

    // Create a new claims map with existing claims
    java.util.Map<String, Object> updatedClaims = new java.util.HashMap<>();
    updatedClaims.putAll(claims);
    updatedClaims.put("iat", new Date());
    updatedClaims.put("exp", Date.from(Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS)));

    return Jwts.builder()
        .claims(updatedClaims)
        .signWith(getSigningKey())
        .compact();
  }

  public long getExpirationTime() {
    return jwtExpirationMs;
  }
}
