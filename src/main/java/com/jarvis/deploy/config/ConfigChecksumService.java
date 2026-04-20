package com.jarvis.deploy.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Computes and verifies SHA-256 checksums for environment configs.
 * Useful for detecting tampering or unexpected drift between environments.
 */
public class ConfigChecksumService {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Computes a deterministic checksum over all key-value pairs in the config.
     * Keys are sorted to ensure consistent ordering.
     *
     * @param config the environment config
     * @return hex-encoded SHA-256 checksum string
     */
    public String computeChecksum(EnvironmentConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        Map<String, String> sorted = new TreeMap<>(config.getProperties());
        StringBuilder sb = new StringBuilder();
        sb.append("env:").append(config.getEnvironmentName()).append("\n");
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sha256Hex(sb.toString());
    }

    /**
     * Verifies that the given config matches an expected checksum.
     *
     * @param config           the environment config to check
     * @param expectedChecksum the previously recorded checksum
     * @return true if the config matches the expected checksum
     */
    public boolean verify(EnvironmentConfig config, String expectedChecksum) {
        if (expectedChecksum == null || expectedChecksum.isBlank()) {
            throw new IllegalArgumentException("Expected checksum must not be null or blank");
        }
        String actual = computeChecksum(config);
        return actual.equalsIgnoreCase(expectedChecksum);
    }

    /**
     * Compares checksums of two configs to detect any differences.
     *
     * @param a first config
     * @param b second config
     * @return true if both configs produce identical checksums
     */
    public boolean matches(EnvironmentConfig a, EnvironmentConfig b) {
        return computeChecksum(a).equals(computeChecksum(b));
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
