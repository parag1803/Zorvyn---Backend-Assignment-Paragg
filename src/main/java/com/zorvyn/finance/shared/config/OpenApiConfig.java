package com.zorvyn.finance.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 * Provides interactive API documentation with JWT authentication support.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zorvyn Finance Dashboard API")
                        .description("Enterprise-grade Finance Dashboard Backend — Modular Monolith.\n\n" +
                                "## Authentication\n" +
                                "Use the `/api/v1/auth/login` endpoint to obtain a JWT token, " +
                                "then click the 'Authorize' button above and enter: `Bearer <your-token>`\n\n" +
                                "## Roles\n" +
                                "- **VIEWER**: Can view dashboard summary and recent activity\n" +
                                "- **ANALYST**: Can view records and access detailed analytics\n" +
                                "- **ADMIN**: Full management access including user management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Zorvyn Engineering")
                                .email("engineering@zorvyn.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token")));
    }
}
