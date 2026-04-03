package com.zorvyn.finance.shared.config;

import com.zorvyn.finance.shared.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Core Spring Security configuration.
 *
 * Security features:
 * - Stateless JWT authentication
 * - BCrypt password encoding (work factor 12)
 * - CORS whitelist
 * - CSRF disabled (stateless API)
 * - OWASP security headers
 * - Method-level authorization via @PreAuthorize
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Disable CSRF (stateless JWT API) ──────────────────────
            .csrf(AbstractHttpConfigurer::disable)

            // ── CORS ──────────────────────────────────────────────────
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── Security Headers (OWASP) ──────────────────────────────
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(contentType -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                .cacheControl(cache -> {})
            )

            // ── Session Management (Stateless) ───────────────────────
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Authorization Rules ──────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // Admin-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/records/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/records/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/records/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                // Analyst + Admin can read records
                .requestMatchers(HttpMethod.GET, "/api/v1/records/**").hasAnyRole("ADMIN", "ANALYST")

                // Dashboard - summary & recent activity accessible to all authenticated users
                .requestMatchers("/api/v1/dashboard/summary", "/api/v1/dashboard/recent-activity").authenticated()
                // Dashboard - detailed analytics for ANALYST + ADMIN
                .requestMatchers("/api/v1/dashboard/**").hasAnyRole("ADMIN", "ANALYST")

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // ── JWT Filter ───────────────────────────────────────────
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key",
                "X-Correlation-ID", "If-Match"));
        configuration.setExposedHeaders(List.of("X-Correlation-ID", "ETag", "Retry-After",
                "X-RateLimit-Remaining"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
