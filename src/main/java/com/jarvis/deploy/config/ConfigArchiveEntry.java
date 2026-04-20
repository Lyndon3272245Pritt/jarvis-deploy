package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single archived snapshot of an environment's configuration,
 * captured at a specific point in time for long-term storage.
 */
public class ConfigArchiveEntry {

    private final String archiveId;
    private final String environment;
    private final Map<String, String> properties;
    private final Instant archivedAt;
    private final String archivedBy;
    private final String reason;

    public ConfigArchiveEntry(String archiveId, String environment,
                              Map<String, String> properties,
                              Instant archivedAt, String archivedBy, String reason) {
        this.archiveId = Objects.requireNonNull(archiveId, "archiveId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.properties = Collections.unmodifiableMap(
                Objects.requireNonNull(properties, "properties must not be null"));
        this.archivedAt = Objects.requireNonNull(archivedAt, "archivedAt must not be null");
        this.archivedBy = Objects.requireNonNull(archivedBy, "archivedBy must not be null");
        this.reason = reason != null ? reason : "";
    }

    public String getArchiveId() { return archiveId; }
    public String getEnvironment() { return environment; }
    public Map<String, String> getProperties() { return properties; }
    public Instant getArchivedAt() { return archivedAt; }
    public String getArchivedBy() { return archivedBy; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return "ConfigArchiveEntry{archiveId='" + archiveId + "', environment='" + environment +
                "', archivedAt=" + archivedAt + ", archivedBy='" + archivedBy + "'}";
    }
}
