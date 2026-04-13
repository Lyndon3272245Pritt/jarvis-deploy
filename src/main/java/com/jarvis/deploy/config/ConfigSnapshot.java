package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable snapshot of an environment configuration at a point in time.
 */
public class ConfigSnapshot {

    private final String environment;
    private final Map<String, String> properties;
    private final Instant capturedAt;
    private final String snapshotId;

    public ConfigSnapshot(String environment, Map<String, String> properties, Instant capturedAt, String snapshotId) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        Objects.requireNonNull(snapshotId, "snapshotId must not be null");
        this.environment = environment;
        this.properties = Collections.unmodifiableMap(properties);
        this.capturedAt = capturedAt;
        this.snapshotId = snapshotId;
    }

    public String getEnvironment() {
        return environment;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigSnapshot)) return false;
        ConfigSnapshot that = (ConfigSnapshot) o;
        return snapshotId.equals(that.snapshotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshotId);
    }

    @Override
    public String toString() {
        return "ConfigSnapshot{snapshotId='" + snapshotId + "', environment='" + environment +
                "', capturedAt=" + capturedAt + ", properties.size=" + properties.size() + "}";
    }
}
