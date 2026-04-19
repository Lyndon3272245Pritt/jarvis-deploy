package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines quota limits for config entries per environment.
 */
public class ConfigQuotaPolicy {

    private final int defaultMaxKeys;
    private final Map<String, Integer> environmentLimits;

    public ConfigQuotaPolicy(int defaultMaxKeys) {
        this.defaultMaxKeys = defaultMaxKeys;
        this.environmentLimits = new HashMap<>();
    }

    public void setLimit(String environment, int maxKeys) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (maxKeys < 1) {
            throw new IllegalArgumentException("maxKeys must be >= 1");
        }
        environmentLimits.put(environment, maxKeys);
    }

    public int getLimit(String environment) {
        return environmentLimits.getOrDefault(environment, defaultMaxKeys);
    }

    public int getDefaultMaxKeys() {
        return defaultMaxKeys;
    }

    public boolean hasCustomLimit(String environment) {
        return environmentLimits.containsKey(environment);
    }

    public Map<String, Integer> getAllLimits() {
        return Map.copyOf(environmentLimits);
    }
}
