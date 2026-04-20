package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores and retrieves arbitrary metadata associated with named configurations.
 * Metadata entries are keyed by environment name and a metadata key string.
 */
public class ConfigMetadataStore {

    // environment -> (metaKey -> MetadataEntry)
    private final ConcurrentHashMap<String, Map<String, MetadataEntry>> store = new ConcurrentHashMap<>();

    public void put(String environment, String key, String value, String author) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Metadata key must not be blank");
        }
        store.computeIfAbsent(environment, e -> new LinkedHashMap<>())
             .put(key, new MetadataEntry(key, value, author, Instant.now()));
    }

    public Optional<MetadataEntry> get(String environment, String key) {
        Map<String, MetadataEntry> envMeta = store.get(environment);
        if (envMeta == null) return Optional.empty();
        return Optional.ofNullable(envMeta.get(key));
    }

    public Map<String, MetadataEntry> getAllForEnvironment(String environment) {
        Map<String, MetadataEntry> envMeta = store.get(environment);
        if (envMeta == null) return Collections.emptyMap();
        return Collections.unmodifiableMap(envMeta);
    }

    public boolean remove(String environment, String key) {
        Map<String, MetadataEntry> envMeta = store.get(environment);
        if (envMeta == null) return false;
        return envMeta.remove(key) != null;
    }

    public void clearEnvironment(String environment) {
        store.remove(environment);
    }

    public boolean hasMetadata(String environment, String key) {
        return get(environment, key).isPresent();
    }

    public int size(String environment) {
        Map<String, MetadataEntry> envMeta = store.get(environment);
        return envMeta == null ? 0 : envMeta.size();
    }

    public static final class MetadataEntry {
        private final String key;
        private final String value;
        private final String author;
        private final Instant timestamp;

        public MetadataEntry(String key, String value, String author, Instant timestamp) {
            this.key = key;
            this.value = value;
            this.author = author;
            this.timestamp = timestamp;
        }

        public String getKey()       { return key; }
        public String getValue()     { return value; }
        public String getAuthor()    { return author; }
        public Instant getTimestamp(){ return timestamp; }

        @Override
        public String toString() {
            return "MetadataEntry{key='" + key + "', value='" + value +
                   "', author='" + author + "', timestamp=" + timestamp + "}";
        }
    }
}
