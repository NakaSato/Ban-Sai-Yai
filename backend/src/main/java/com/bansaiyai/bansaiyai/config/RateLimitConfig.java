package com.bansaiyai.bansaiyai.config;

import com.bansaiyai.bansaiyai.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting configuration for API endpoints.
 * Uses a simple token bucket algorithm for rate limiting.
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "api.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitConfig {

  @Value("${api.rate-limit.requests-per-minute:60}")
  private int requestsPerMinute;

  @Value("${api.rate-limit.burst-capacity:10}")
  private int burstCapacity;

  @Bean
  public RateLimitFilter rateLimitFilter() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return new RateLimitFilter(requestsPerMinute, burstCapacity, objectMapper);
  }

  public static class RateLimitFilter extends OncePerRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RateLimitFilter.class);

    private final int requestsPerMinute;
    private final int burstCapacity;
    private final ObjectMapper objectMapper;
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(int requestsPerMinute, int burstCapacity, ObjectMapper objectMapper) {
      this.requestsPerMinute = requestsPerMinute;
      this.burstCapacity = burstCapacity;
      this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

      String clientKey = getClientKey(request);
      RateLimitBucket bucket = buckets.computeIfAbsent(clientKey,
          k -> new RateLimitBucket(requestsPerMinute, burstCapacity));

      if (bucket.tryConsume()) {
        // Add rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(bucket.getResetTime()));

        filterChain.doFilter(request, response);
      } else {
        log.warn("Rate limit exceeded for client: {}", clientKey);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(bucket.getRetryAfterSeconds()));

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Too Many Requests",
            "Rate limit exceeded. Please try again later.",
            request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      }
    }

    private String getClientKey(HttpServletRequest request) {
      // Try to get real IP from proxy headers
      String forwardedFor = request.getHeader("X-Forwarded-For");
      if (forwardedFor != null && !forwardedFor.isEmpty()) {
        return forwardedFor.split(",")[0].trim();
      }

      String realIp = request.getHeader("X-Real-IP");
      if (realIp != null && !realIp.isEmpty()) {
        return realIp;
      }

      return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
      String path = request.getRequestURI();
      // Skip rate limiting for health checks and static resources
      return path.startsWith("/actuator") ||
          path.startsWith("/swagger") ||
          path.startsWith("/v3/api-docs") ||
          path.endsWith(".html") ||
          path.endsWith(".css") ||
          path.endsWith(".js");
    }
  }

  /**
   * Simple token bucket implementation for rate limiting.
   */
  public static class RateLimitBucket {
    private final int maxTokens;
    private final double refillRate; // tokens per second
    private final AtomicInteger tokens;
    private volatile long lastRefillTime;

    public RateLimitBucket(int requestsPerMinute, int burstCapacity) {
      this.maxTokens = burstCapacity;
      this.refillRate = requestsPerMinute / 60.0;
      this.tokens = new AtomicInteger(burstCapacity);
      this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
      refill();
      if (tokens.get() > 0) {
        tokens.decrementAndGet();
        return true;
      }
      return false;
    }

    private void refill() {
      long now = System.currentTimeMillis();
      long elapsed = now - lastRefillTime;
      int tokensToAdd = (int) (elapsed / 1000.0 * refillRate);

      if (tokensToAdd > 0) {
        int newTokens = Math.min(maxTokens, tokens.get() + tokensToAdd);
        tokens.set(newTokens);
        lastRefillTime = now;
      }
    }

    public int getRemaining() {
      return tokens.get();
    }

    public long getResetTime() {
      return lastRefillTime / 1000 + 60;
    }

    public long getRetryAfterSeconds() {
      return Math.max(1, (long) (1.0 / refillRate));
    }
  }
}
