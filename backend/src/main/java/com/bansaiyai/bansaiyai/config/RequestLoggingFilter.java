package com.bansaiyai.bansaiyai.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP Request/Response logging filter for debugging and monitoring.
 * Logs request details, response status, and execution time.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Value("${logging.http.enabled:true}")
  private boolean loggingEnabled;

  @Value("${logging.http.include-payload:false}")
  private boolean includePayload;

  @Value("${logging.http.max-payload-length:1000}")
  private int maxPayloadLength;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if (!loggingEnabled || isAsyncDispatch(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    // Skip logging for actuator and swagger endpoints
    String path = request.getRequestURI();
    if (path.contains("/actuator") || path.contains("/swagger") || path.contains("/v3/api-docs")) {
      filterChain.doFilter(request, response);
      return;
    }

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, maxPayloadLength);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    long startTime = System.currentTimeMillis();

    try {
      // Log request
      logRequest(wrappedRequest, requestId);

      filterChain.doFilter(wrappedRequest, wrappedResponse);

    } finally {
      // Log response
      long duration = System.currentTimeMillis() - startTime;
      logResponse(wrappedResponse, requestId, duration);

      // Copy response body back to original response
      wrappedResponse.copyBodyToResponse();
    }
  }

  private void logRequest(ContentCachingRequestWrapper request, String requestId) {
    String queryString = request.getQueryString();
    String path = queryString != null ? request.getRequestURI() + "?" + queryString : request.getRequestURI();

    log.info("[{}] --> {} {} (from: {}, user-agent: {})",
        requestId,
        request.getMethod(),
        path,
        getClientIP(request),
        truncate(request.getHeader("User-Agent"), 50));
  }

  private void logResponse(ContentCachingResponseWrapper response, String requestId, long duration) {
    int status = response.getStatus();

    if (status >= 500) {
      log.error("[{}] <-- {} ({} ms)", requestId, status, duration);
    } else if (status >= 400) {
      log.warn("[{}] <-- {} ({} ms)", requestId, status, duration);
    } else {
      log.info("[{}] <-- {} ({} ms)", requestId, status, duration);
    }
  }

  private String getClientIP(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private String truncate(String value, int maxLength) {
    if (value == null)
      return "unknown";
    return value.length() > maxLength ? value.substring(0, maxLength) + "..." : value;
  }
}
