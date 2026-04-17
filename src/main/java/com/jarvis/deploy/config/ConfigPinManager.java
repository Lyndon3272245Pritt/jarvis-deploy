package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pinned config keys — keys that are locked to a specific value
 * and cannot be overridden by merges, promotions, or patches.
 */
public class ConfigPinManager {

    private final Map<String, Map<String, PinnedEntry>> pins = new ConcurrentHashMap<>();

    public void pin(String environment, String key, String value, String pinnedBy) {
        if (environment == null || key == null || value == null) {
            throw new IllegalArgumentException("environment, key, and value must not be null");
        }
        pins.computeIfAbsent(environment, e -> new ConcurrentHashMap<>())
            .put(key, new PinnedEntry(key, value, pinnedBy, Instant.now()));
    }

    public void unpin(String environment, String key) {
        Map<String, PinnedEntry> envPins = pins.get(environment);
        if (envPins != null) {
            envPins.remove(key);
        }
    }

    public boolean isPinned(String environment, String key) {
        Map<String, PinnedEntry> envPins = pins.get(environment);
        return envPins != null && envPins.containsKey(key);
    }

    public Optional<PinnedEntry> getPin(String environment, String key) {
        Map<String, PinnedEntry> envPins = pins.get(environment);
        if (envPins == null) return Optional.empty();
        return Optional.ofNullable(envPins.get(key));
    }

    public Map<String, PinnedEntry> getPinsForEnvironment(String environment) {
        return Collections.unmodifiableMap(pins.getOrDefault(environment, Collections.emptyMap()));
    }

    /**
     * Apply pins to a config map — pinned keys override whatever is present.
     */
    public Map<String, String> applyPins(String environment, Map<String, String> config) {
        Map<String, String> result = new LinkedHashMap<>(config);
        Map<String, PinnedEntry> envPins = pins.getOrDefault(environment, Collections.emptyMap());
        for (Map.Entry<String, PinnedEntry> entry : envPins.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getValue());
        }
        return result;
    }

    public void clearAll(String environment) {
        pins.remove(environment);
    }

    public static class PinnedEntry {
        private final String key;
        private final String value;
        private final String pinnedBy;
        private final Instant pinnedAt;

        public PinnedEntry(String key, String value, String pinnedBy, Instant pinnedAt) {
            this.key = key;
            this.value = value;
            this.pinnedBy = pinnedBy;
            this.pinnedAt = pinnedAt;
        }

        public String getKey() { return key; }
        public String getValue() { return value; }
        public String getPinnedBy() { return pinnedBy; }
        public Instant getPinnedAt() { return pinnedAt; }

        @Override
        public String toString() {
            return "PinnedEntry{key='" + key + "', value='" + value + "', pinnedBy='" + pinnedBy + "', pinnedAt=" + pinnedAt + "}";
        }
    }
}
