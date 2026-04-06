package com.zorvyn.finance.iam.dto;

import lombok.Getter;

@Getter
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserResponse user;

    private LoginResponse(Builder builder) {
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.tokenType = builder.tokenType;
        this.expiresIn = builder.expiresIn;
        this.user = builder.user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserResponse user;

        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public Builder user(UserResponse user) { this.user = user; return this; }

        public LoginResponse build() { return new LoginResponse(this); }
    }
}
