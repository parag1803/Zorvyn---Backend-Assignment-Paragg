package com.zorvyn.finance.shared.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashing utilities for idempotency request fingerprinting.
 */
public final class HashUtils {

    private HashUtils() {}

    /**
     * Compute SHA-256 hash of the input string.
     */
    public static String sha256(String input) {
        if (input == null || input.isEmpty()) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
