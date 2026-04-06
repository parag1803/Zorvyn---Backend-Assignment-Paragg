package com.zorvyn.finance.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration.
 * Provides interactive API documentation with JWT authentication support.
 *
 * Quick Start:
 *  1. POST /api/v1/auth/register  → create account
 *  2. POST /api/v1/auth/login     → get JWT token
 *  3. Click "Authorize" → enter:  Bearer <token>
 *  4. Explore all endpoints
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zorvyn Finance Dashboard API")
                        .description("""
                                ## Zorvyn Finance Dashboard — Backend API
                                
                                Enterprise-grade Finance Dashboard built as a **Modular Monolith** with Spring Boot 3.
                                
                                ---
                                
                                ## 🚀 Quick Start Guide
                                
                                Follow these steps to test the API end-to-end in Swagger UI:
                                
                                ### Step 1 — Register a new user
                                ```
                                POST /api/v1/auth/register
                                {
                                  "email": "admin@zorvyn.com",
                                  "password": "SecurePass123!",
                                  "firstName": "John",
                                  "lastName": "Doe"
                                }
                                ```
                                > New users get the **VIEWER** role by default.
                                
                                ### Step 2 — Login and get JWT token
                                ```
                                POST /api/v1/auth/login
                                { "email": "admin@zorvyn.com", "password": "SecurePass123!" }
                                ```
                                Copy the `accessToken` from the response.
                                
                                ### Step 3 — Authorize in Swagger
                                Click the **🔒 Authorize** button (top right) and enter:
                                ```
                                Bearer eyJhbGciOiJIUzUxMiJ9...
                                ```
                                
                                ### Step 4 — Use pre-seeded Admin account (recommended for full testing)
                                The database is pre-seeded with test accounts:
                                | Email | Password | Role |
                                |-------|----------|------|
                                | `admin@zorvyn.com` | `Admin@123!` | ADMIN |
                                | `analyst@zorvyn.com` | `Analyst@123!` | ANALYST |
                                | `viewer@zorvyn.com` | `Viewer@123!` | VIEWER |
                                
                                > **ADMIN** → full access (create/update/delete records, manage users)
                                > **ANALYST** → read records + view detailed analytics
                                > **VIEWER** → dashboard summary + recent activity only
                                
                                ---
                                
                                ## 🔐 Authentication
                                - All endpoints (except `/auth/register` and `/auth/login`) require a valid JWT token
                                - Tokens expire after **15 minutes** — use `/auth/refresh` with your refresh token
                                - Refresh tokens expire after **7 days**
                                
                                ## 🛡️ RBAC Summary
                                | Role | Records | Analytics | Users |
                                |------|---------|-----------|-------|
                                | VIEWER | ❌ | Summary only | ❌ |
                                | ANALYST | Read only | Full access | ❌ |
                                | ADMIN | Full CRUD | Full access | Full CRUD |
                                
                                ## ⚡ Key Features
                                - **Idempotency**: Add `Idempotency-Key: <uuid>` header to `POST /records` to prevent duplicate records
                                - **Optimistic Locking**: Include current `version` field when updating records
                                - **Filtering**: Records support filtering by type, category, date range, amount range, and text search
                                - **Rate Limiting**: 100 requests/minute per IP (429 Too Many Requests when exceeded)
                                - **Correlation ID**: Every request gets an `X-Correlation-ID` for distributed tracing
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Paragg — Zorvyn Engineering")
                                .email("engineering@zorvyn.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development (H2 DB)"),
                        new Server().url("http://localhost:8080").description("Docker (PostgreSQL)")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("GitHub Repository")
                        .url("https://github.com/paragg/Zorvyn---Backend-Assignment"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token obtained from POST /api/v1/auth/login")));
    }
}
