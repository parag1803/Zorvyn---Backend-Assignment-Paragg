package com.zorvyn.finance.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response envelope.
 * Provides structured error information without leaking internal details.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean success;
    private final String message;
    private final String errorCode;
    private final Map<String, String> errors;
    private final String path;
    private final String requestId;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    public static ErrorResponse of(String message, String errorCode, String path) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .build();
    }

    public static ErrorResponse validation(Map<String, String> fieldErrors, String path) {
        return ErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .errors(fieldErrors)
                .path(path)
                .build();
    }
}
