package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a named alias pointing to a specific environment config key.
 * Aliases allow referencing config values by a stable name regardless of key changes.
 */
public class ConfigAlias {

    private final String aliasName;
    private final String targetEnvironment;
    private final String targetKey;
    private final Instant createdAt;
    private final String createdBy;

    public ConfigAlias(String aliasName, String targetEnvironment, String targetKey, String createdBy) {
        if (aliasName == null || aliasName.isBlank()) throw new IllegalArgumentException("Alias name must not be blank");
        if (targetEnvironment == null || targetEnvironment.isBlank()) throw new IllegalArgumentException("Target environment must not be blank");
        if (targetKey == null || targetKey.isBlank()) throw new IllegalArgumentException("Target key must not be blank");
        this.aliasName = aliasName;
        this.targetEnvironment = targetEnvironment;
        this.targetKey = targetKey;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public String getAliasName() { return aliasName; }
    public String getTargetEnvironment() { return targetEnvironment; }
    public String getTargetKey() { return targetKey; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigAlias)) return false;
        ConfigAlias that = (ConfigAlias) o;
        return Objects.equals(aliasName, that.aliasName);
    }

    @Override
    public int hashCode() { return Objects.hash(aliasName); }

    @Override
    public String toString() {
        return "ConfigAlias{aliasName='" + aliasName + "', target='" + targetEnvironment + ":" + targetKey + "'}";
    }
}
