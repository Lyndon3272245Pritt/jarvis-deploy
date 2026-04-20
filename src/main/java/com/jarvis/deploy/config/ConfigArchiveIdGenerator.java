package com.jarvis.deploy.config;

import java.util.UUID;

/**
 * Generates unique, deterministic-prefix IDs for config archive entries.
 * Format: archive-{environment}-{uuid-short}
 */
public class ConfigArchiveIdGenerator {

    /**
     * Generates a unique archive ID for the given environment name.
     *
     * @param environment the environment being archived
     * @return a unique archive ID string
     */
    public String generate(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("environment must not be null or blank");
        }
        String uuidShort = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "archive-" + environment.toLowerCase() + "-" + uuidShort;
    }
}
