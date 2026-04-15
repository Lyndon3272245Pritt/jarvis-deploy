package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.*;

/**
 * Tracks the lineage (origin and transformation history) of configuration keys
 * across environments, merges, promotions, and patches.
 */
public class ConfigLineageTracker {

    public enum OperationType {
        LOADED, MERGED, PROMOTED, PATCHED, IMPORTED, ROLLED_BACK
    }

    public static class LineageEntry {
        private final String key;
        private final String sourceEnvironment;
        private final String targetEnvironment;
        private final OperationType operation;
        private final String actor;
        private final Instant timestamp;

        public LineageEntry(String key, String sourceEnvironment, String targetEnvironment,
                            OperationType operation, String actor, Instant timestamp) {
            this.key = Objects.requireNonNull(key, "key must not be null");
            this.sourceEnvironment = sourceEnvironment;
            this.targetEnvironment = Objects.requireNonNull(targetEnvironment, "targetEnvironment must not be null");
            this.operation = Objects.requireNonNull(operation, "operation must not be null");
            this.actor = actor != null ? actor : "system";
            this.timestamp = timestamp != null ? timestamp : Instant.now();
        }

        public String getKey() { return key; }
        public String getSourceEnvironment() { return sourceEnvironment; }
        public String getTargetEnvironment() { return targetEnvironment; }
        public OperationType getOperation() { return operation; }
        public String getActor() { return actor; }
        public Instant getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] key='%s' %s -> %s via %s by %s",
                    timestamp, key, sourceEnvironment, targetEnvironment, operation, actor);
        }
    }

    private final List<LineageEntry> entries = new ArrayList<>();

    public void record(String key, String sourceEnv, String targetEnv,
                       OperationType operation, String actor) {
        entries.add(new LineageEntry(key, sourceEnv, targetEnv, operation, actor, Instant.now()));
    }

    public List<LineageEntry> getLineageForKey(String key) {
        Objects.requireNonNull(key, "key must not be null");
        List<LineageEntry> result = new ArrayList<>();
        for (LineageEntry e : entries) {
            if (e.getKey().equals(key)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<LineageEntry> getLineageForEnvironment(String environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        List<LineageEntry> result = new ArrayList<>();
        for (LineageEntry e : entries) {
            if (environment.equals(e.getTargetEnvironment()) || environment.equals(e.getSourceEnvironment())) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<LineageEntry> getAllEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }
}
