package com.zorvyn.finance.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Standardized API response envelope used across all endpoints.
 * Ensures consistent response structure for frontend consumption.
 *
 * @param <T> The type of the response payload
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String requestId;
    private final Instant timestamp;

    private ApiResponse(Builder<T> builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
        this.requestId = builder.requestId;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static class Builder<T> {
        private boolean success;
        private String message;
        private T data;
        private String requestId;
        private Instant timestamp;

        public Builder<T> success(boolean success) { this.success = success; return this; }
        public Builder<T> message(String message) { this.message = message; return this; }
        public Builder<T> data(T data) { this.data = data; return this; }
        public Builder<T> requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder<T> timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public ApiResponse<T> build() { return new ApiResponse<>(this); }
    }
}
