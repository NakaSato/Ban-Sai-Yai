package com.bansaiyai.bansaiyai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Accessible at /api/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

  @Value("${server.port:9090}")
  private String serverPort;

  @Bean
  public OpenAPI customOpenAPI() {
    final String securitySchemeName = "bearerAuth";

    return new OpenAPI()
        .info(new Info()
            .title("Ban Sai Yai Savings Group API")
            .version("1.0.0")
            .description("""
                REST API for the Ban Sai Yai Savings Group Financial Accounting System.

                ## Features
                - Member registration and management
                - Savings accounts and transactions
                - Loan applications and approvals
                - Payment processing
                - Financial reporting
                - Role-based access control (RBAC)

                ## Authentication
                All endpoints except `/auth/**` require JWT authentication.
                Use the login endpoint to obtain a token, then include it in the Authorization header.

                ## Roles
                - **PRESIDENT**: Full system access
                - **SECRETARY**: Member management, loan approvals
                - **OFFICER**: Daily operations, payments
                - **MEMBER**: View own data only
                """)
            .contact(new Contact()
                .name("Ban Sai Yai Development Team")
                .email("support@bansaiyai.com"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")))
        .servers(List.of(
            new Server()
                .url("http://localhost:" + serverPort + "/api")
                .description("Development Server"),
            new Server()
                .url("https://api.bansaiyai.com")
                .description("Production Server")))
        .tags(List.of(
            new Tag().name("Authentication").description("Login, logout, and token refresh"),
            new Tag().name("Members").description("Member registration and management"),
            new Tag().name("Savings").description("Savings accounts and transactions"),
            new Tag().name("Loans").description("Loan applications and management"),
            new Tag().name("Payments").description("Payment processing"),
            new Tag().name("Reports").description("Financial reports and exports"),
            new Tag().name("Dashboard").description("Dashboard statistics and widgets"),
            new Tag().name("Audit").description("Audit logs and security monitoring"),
            new Tag().name("Admin").description("Administrative functions")))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(new Components()
            .addSecuritySchemes(securitySchemeName,
                new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter JWT token obtained from /auth/login")));
  }
}
