package com.bansaiyai.bansaiyai.config;

import com.bansaiyai.bansaiyai.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // Security headers
        .headers(headers -> headers
            .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'self'"))
            .frameOptions(frame -> frame.sameOrigin()))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
              response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
              org.springframework.http.ProblemDetail pd = org.springframework.http.ProblemDetail
                  .forStatusAndDetail(org.springframework.http.HttpStatus.UNAUTHORIZED, authException.getMessage());
              objectMapper.writeValue(response.getOutputStream(), pd);
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
              response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
              org.springframework.http.ProblemDetail pd = org.springframework.http.ProblemDetail
                  .forStatusAndDetail(org.springframework.http.HttpStatus.FORBIDDEN,
                      accessDeniedException.getMessage());
              objectMapper.writeValue(response.getOutputStream(), pd);
            }))
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()
            .requestMatchers(HttpMethod.GET, "/auth/check-username/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/auth/check-email/**").permitAll()

            // Swagger/OpenAPI endpoints
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()

            // Health check endpoints
            .requestMatchers("/api/health/**").permitAll()
            .requestMatchers("/actuator/health").permitAll()

            // Admin endpoints
            .requestMatchers("/api/admin/**").hasRole("PRESIDENT")

            // Payment endpoints restrictions
            .requestMatchers(HttpMethod.POST, "/api/payments").hasRole("OFFICER")

            // All other endpoints need authentication
            .anyRequest().authenticated());

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
