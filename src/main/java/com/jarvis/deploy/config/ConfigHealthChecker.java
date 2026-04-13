package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Performs health checks on a loaded EnvironmentConfig,
 * verifying that required keys are present, values are non-empty,
 * and no placeholder tokens remain unresolved.
 */
public class ConfigHealthChecker {

    private static final String PLACEHOLDER_PATTERN = "\\$\\{[^}]+}";

    private final List<String> requiredKeys;

    public ConfigHealthChecker(List<String> requiredKeys) {
        if (requiredKeys == null) {
            throw new IllegalArgumentException("requiredKeys must not be null");
        }
        this.requiredKeys = Collections.unmodifiableList(new ArrayList<>(requiredKeys));
    }

    /**
     * Runs all health checks against the given config.
     *
     * @param config the environment config to check
     * @return a {@link HealthCheckResult} summarising any issues found
     */
    public HealthCheckResult check(EnvironmentConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }

        List<String> issues = new ArrayList<>();
        Map<String, String> properties = config.getProperties();

        for (String key : requiredKeys) {
            if (!properties.containsKey(key)) {
                issues.add("Missing required key: " + key);
            } else {
                String value = properties.get(key);
                if (value == null || value.trim().isEmpty()) {
                    issues.add("Empty value for required key: " + key);
                }
            }
        }

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getValue() != null && entry.getValue().matches(".*" + PLACEHOLDER_PATTERN + ".*")) {
                issues.add("Unresolved placeholder in key: " + entry.getKey());
            }
        }

        return new HealthCheckResult(issues);
    }

    /**
     * Immutable result of a health check run.
     */
    public static class HealthCheckResult {
        private final List<String> issues;

        public HealthCheckResult(List<String> issues) {
            this.issues = Collections.unmodifiableList(new ArrayList<>(issues));
        }

        public boolean isHealthy() {
            return issues.isEmpty();
        }

        public List<String> getIssues() {
            return issues;
        
        }

        @Override
        public String toString() {
            return isHealthy() ? "HealthCheckResult{healthy}" : "HealthCheckResult{issues=" + issues + "}";
        }
    }
}
