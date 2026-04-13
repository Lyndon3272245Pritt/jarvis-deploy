package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handles promotion of configuration from one environment to another
 * (e.g., staging -> production), with optional key filtering and audit support.
 */
public class ConfigPromotionService {

    private final ConfigLoader configLoader;
    private final ConfigAuditLog auditLog;

    public ConfigPromotionService(ConfigLoader configLoader, ConfigAuditLog auditLog) {
        this.configLoader = Objects.requireNonNull(configLoader, "configLoader must not be null");
        this.auditLog = Objects.requireNonNull(auditLog, "auditLog must not be null");
    }

    /**
     * Promotes all properties from the source environment into the target environment config.
     *
     * @param sourceEnv the environment to promote from
     * @param targetEnv the environment to promote into
     * @param promotedBy the user or system performing the promotion
     * @return a PromotionResult describing what was changed
     * @throws ConfigLoadException if either environment config cannot be loaded
     */
    public PromotionResult promote(String sourceEnv, String targetEnv, String promotedBy) {
        Objects.requireNonNull(sourceEnv, "sourceEnv must not be null");
        Objects.requireNonNull(targetEnv, "targetEnv must not be null");
        Objects.requireNonNull(promotedBy, "promotedBy must not be null");

        EnvironmentConfig source = configLoader.load(sourceEnv);
        EnvironmentConfig target = configLoader.load(targetEnv);

        Map<String, String> sourceProps = source.getProperties();
        Map<String, String> targetProps = new HashMap<>(target.getProperties());

        int added = 0;
        int updated = 0;

        for (Map.Entry<String, String> entry : sourceProps.entrySet()) {
            String key = entry.getKey();
            String newValue = entry.getValue();
            if (!targetProps.containsKey(key)) {
                added++;
            } else if (!newValue.equals(targetProps.get(key))) {
                updated++;
            }
            targetProps.put(key, newValue);
        }

        EnvironmentConfig promoted = new EnvironmentConfig(targetEnv, targetProps);

        String detail = String.format("Promoted from '%s' to '%s': %d added, %d updated",
                sourceEnv, targetEnv, added, updated);
        auditLog.record(new ConfigAuditEntry(targetEnv, promotedBy, "PROMOTE", detail, Instant.now()));

        return new PromotionResult(sourceEnv, targetEnv, promoted, added, updated);
    }
}
