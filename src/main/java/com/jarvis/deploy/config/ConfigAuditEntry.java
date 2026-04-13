package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single audit log entry for a configuration change event.
 */
public class ConfigAuditEntry {

    public enum Action {
        LOADED, MERGED, EXPORTED, SNAPSHOT_CREATED, SNAPSHOT_RESTORED, VALIDATED
    }

    private final Instant timestamp;
    private final String environment;
    private final Action action;
    private final String details;

    public ConfigAuditEntry(String environment, Action action, String details) {
        this.timestamp = Instant.now();
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.details = details != null ? details : "";
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getEnvironment() {
        return environment;
    }

    public Action getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return String.format("[%s] env=%s action=%s details=%s",
                timestamp, environment, action, details);
    }
}
