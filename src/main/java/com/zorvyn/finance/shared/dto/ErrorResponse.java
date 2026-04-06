package com.zorvyn.finance.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response envelope.
 * Provides structured error information without leaking internal details.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean success;
    private final String message;
    private final String errorCode;
    private final Map<String, String> errors;
    private final String path;
    private final String requestId;
    private final Instant timestamp;

    private ErrorResponse(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.errorCode = builder.errorCode;
        this.errors = builder.errors;
        this.path = builder.path;
        this.requestId = builder.requestId;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ErrorResponse of(String message, String errorCode, String path) {
        return builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .build();
    }

    public static ErrorResponse validation(Map<String, String> fieldErrors, String path) {
        return builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .errors(fieldErrors)
                .path(path)
                .build();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private String errorCode;
        private Map<String, String> errors;
        private String path;
        private String requestId;
        private Instant timestamp;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
        public Builder errors(Map<String, String> errors) { this.errors = errors; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        
        public ErrorResponse build() { return new ErrorResponse(this); }
    }
}
