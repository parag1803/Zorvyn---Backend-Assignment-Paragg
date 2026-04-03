package com.zorvyn.finance.shared.filter;

import com.zorvyn.finance.shared.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token-bucket rate limiter per client IP.
 * Protects against brute force and API abuse.
 *
 * - Configurable requests per minute via application properties
 * - Returns 429 Too Many Requests with Retry-After header when limit exceeded
 * - Uses ConcurrentHashMap for thread-safe per-IP bucket management
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimitFilter(@Value("${app.rate-limit.requests-per-minute}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::createBucket);

        if (bucket.tryConsume(1)) {
            long remainingTokens = bucket.getAvailableTokens();
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            filterChain.doFilter(request, response);
        } else {
            throw new RateLimitExceededException(60);
        }
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.startsWith("/actuator");
    }
}
