package com.zorvyn.finance.shared.idempotency;

import com.zorvyn.finance.shared.exception.IdempotencyConflictException;
import com.zorvyn.finance.shared.util.HashUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * Idempotency filter for mutating operations (POST, PUT, PATCH).
 * 
 * Flow:
 * 1. Read Idempotency-Key header
 * 2. If key exists and COMPLETED → replay cached response
 * 3. If key exists and STARTED → return 409 (in-progress)
 * 4. If key absent → create STARTED entry, proceed, then COMPLETE with cached response
 * 5. On error → FAIL the key (allows retry)
 * 
 * Request body is hashed (SHA-256) to detect payload mismatches on same key.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final Set<String> IDEMPOTENT_METHODS = Set.of("POST", "PUT", "PATCH");

    private final IdempotencyService idempotencyService;

    public IdempotencyFilter(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        if (!IDEMPOTENT_METHODS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (!StringUtils.hasText(idempotencyKey)) {
            // Idempotency key is optional — proceed without it
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request to read body multiple times
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Check for existing key
        Optional<IdempotencyKey> existing = idempotencyService.findExisting(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKey ik = existing.get();
            if (ik.getStatus() == IdempotencyKey.Status.COMPLETED) {
                log.info("Replaying cached response for idempotency key: {}", idempotencyKey);
                response.setStatus(ik.getResponseStatus());
                response.setContentType("application/json");
                response.getWriter().write(ik.getResponseBody());
                return;
            }
            if (ik.getStatus() == IdempotencyKey.Status.STARTED) {
                throw new IdempotencyConflictException(
                        "A request with this idempotency key is currently being processed");
            }
        }

        try {
            // Start processing — record the key
            String requestHash = HashUtils.sha256(
                    new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8));
            
            // If body hasn't been read yet, we need to trigger the read first
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
            // Compute hash from the body after it's been read
            byte[] requestBody = wrappedRequest.getContentAsByteArray();
            if (requestBody.length > 0) {
                requestHash = HashUtils.sha256(new String(requestBody, StandardCharsets.UTF_8));
            }

            // Check if an existing key was created by the filter or already existed
            // and store the response
            int status = wrappedResponse.getStatus();
            byte[] responseBody = wrappedResponse.getContentAsByteArray();
            String responseBodyStr = new String(responseBody, StandardCharsets.UTF_8);

            if (status >= 200 && status < 300) {
                // Only cache successful responses
                try {
                    idempotencyService.startProcessing(idempotencyKey, requestHash,
                            request.getMethod(), request.getRequestURI());
                    idempotencyService.markCompleted(idempotencyKey, status, responseBodyStr);
                } catch (IdempotencyConflictException e) {
                    // Key was created by concurrent request — that's fine
                    log.debug("Concurrent idempotency key creation for: {}", idempotencyKey);
                }
            }

            // Copy the cached response body to the actual response
            wrappedResponse.copyBodyToResponse();

        } catch (IdempotencyConflictException e) {
            throw e;
        } catch (Exception e) {
            idempotencyService.markFailed(idempotencyKey);
            throw e;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.startsWith("/actuator");
    }
}
