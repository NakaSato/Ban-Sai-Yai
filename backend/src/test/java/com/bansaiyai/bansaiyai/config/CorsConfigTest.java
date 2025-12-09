package com.bansaiyai.bansaiyai.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CorsConfig.
 */
@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

  @Test
  void corsConfigurationSource_shouldReturnValidConfiguration() {
    // Arrange
    CorsConfig corsConfig = new CorsConfig();

    // Use reflection to set the values (since @Value won't work in unit tests)
    setField(corsConfig, "allowedOrigins", "http://localhost:5173,http://localhost:3000");
    setField(corsConfig, "allowedMethods", "GET,POST,PUT,DELETE");
    setField(corsConfig, "allowedHeaders", "*");
    setField(corsConfig, "exposedHeaders", "Authorization");
    setField(corsConfig, "allowCredentials", true);
    setField(corsConfig, "maxAge", 3600L);

    // Act
    CorsConfigurationSource source = corsConfig.corsConfigurationSource();

    // Assert
    assertNotNull(source);
  }

  private void setField(Object target, String fieldName, Object value) {
    try {
      var field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set field: " + fieldName, e);
    }
  }
}
