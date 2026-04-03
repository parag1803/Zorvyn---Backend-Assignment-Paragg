package com.zorvyn.finance.shared.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * In-memory token blacklist using Caffeine cache.
 * Tokens are blacklisted on logout and automatically evicted after the access token TTL.
 * In a multi-instance deployment, this would be replaced with Redis.
 */
@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklist;

    public TokenBlacklistService(@Value("${app.jwt.access-token-expiry-ms}") long accessTokenExpiryMs) {
        this.blacklist = Caffeine.newBuilder()
                .expireAfterWrite(accessTokenExpiryMs, TimeUnit.MILLISECONDS)
                .maximumSize(10_000)
                .build();
    }

    /**
     * Blacklist a token (called on logout).
     */
    public void blacklist(String token) {
        blacklist.put(token, Boolean.TRUE);
    }

    /**
     * Check if a token has been blacklisted.
     */
    public boolean isBlacklisted(String token) {
        return blacklist.getIfPresent(token) != null;
    }
}
