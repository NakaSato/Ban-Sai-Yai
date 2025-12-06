package com.bansaiyai.bansaiyai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for cross-origin requests.
 * Configurable via application properties.
 */
@Configuration
public class CorsConfig {

  @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
  private String allowedOrigins;

  @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
  private String allowedMethods;

  @Value("${cors.allowed-headers:*}")
  private String allowedHeaders;

  @Value("${cors.exposed-headers:Authorization,Content-Disposition}")
  private String exposedHeaders;

  @Value("${cors.allow-credentials:true}")
  private boolean allowCredentials;

  @Value("${cors.max-age:3600}")
  private long maxAge;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Parse allowed origins from comma-separated string
    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    configuration.setAllowedOrigins(origins.stream().map(String::trim).toList());

    // Parse allowed methods
    List<String> methods = Arrays.asList(allowedMethods.split(","));
    configuration.setAllowedMethods(methods.stream().map(String::trim).toList());

    // Parse allowed headers
    if ("*".equals(allowedHeaders)) {
      configuration.addAllowedHeader("*");
    } else {
      List<String> headers = Arrays.asList(allowedHeaders.split(","));
      configuration.setAllowedHeaders(headers.stream().map(String::trim).toList());
    }

    // Parse exposed headers
    List<String> exposed = Arrays.asList(exposedHeaders.split(","));
    configuration.setExposedHeaders(exposed.stream().map(String::trim).toList());

    configuration.setAllowCredentials(allowCredentials);
    configuration.setMaxAge(maxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
