package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a notification event triggered by a configuration change.
 */
public class ConfigNotificationEvent {

    public enum EventType {
        CREATED, UPDATED, DELETED, ROLLED_BACK, IMPORTED
    }

    private final String environment;
    private final EventType eventType;
    private final String triggeredBy;
    private final Instant timestamp;
    private final String details;

    public ConfigNotificationEvent(String environment, EventType eventType,
                                   String triggeredBy, String details) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.triggeredBy = Objects.requireNonNull(triggeredBy, "triggeredBy must not be null");
        this.details = details;
        this.timestamp = Instant.now();
    }

    public String getEnvironment() { return environment; }
    public EventType getEventType() { return eventType; }
    public String getTriggeredBy() { return triggeredBy; }
    public Instant getTimestamp() { return timestamp; }
    public String getDetails() { return details; }

    @Override
    public String toString() {
        return String.format("ConfigNotificationEvent{env='%s', type=%s, by='%s', at=%s, details='%s'}",
                environment, eventType, triggeredBy, timestamp, details);
    }
}
