package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publishes config change events to registered listeners.
 */
public class ConfigEventPublisher {

    public enum EventType { LOADED, UPDATED, DELETED, ROLLED_BACK, PROMOTED }

    public static class ConfigEvent {
        private final String environment;
        private final EventType type;
        private final String details;
        private final Instant occurredAt;

        public ConfigEvent(String environment, EventType type, String details) {
            this.environment = environment;
            this.type = type;
            this.details = details;
            this.occurredAt = Instant.now();
        }

        public String getEnvironment() { return environment; }
        public EventType getType() { return type; }
        public String getDetails() { return details; }
        public Instant getOccurredAt() { return occurredAt; }

        @Override
        public String toString() {
            return String.format("[%s] %s on '%s': %s", occurredAt, type, environment, details);
        }
    }

    public interface ConfigEventListener {
        void onEvent(ConfigEvent event);
    }

    private final List<ConfigEventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(ConfigEventListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener must not be null");
        listeners.add(listener);
    }

    public void unregister(ConfigEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(String environment, EventType type, String details) {
        ConfigEvent event = new ConfigEvent(environment, type, details);
        for (ConfigEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public int listenerCount() {
        return listeners.size();
    }
}
