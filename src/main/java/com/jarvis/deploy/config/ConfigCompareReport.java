package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Represents a structured comparison report between two environments.
 */
public class ConfigCompareReport {

    public enum DiffType { ADDED, REMOVED, MODIFIED, UNCHANGED }

    public static class Entry {
        private final String key;
        private final DiffType diffType;
        private final String baseValue;
        private final String targetValue;

        public Entry(String key, DiffType diffType, String baseValue, String targetValue) {
            this.key = key;
            this.diffType = diffType;
            this.baseValue = baseValue;
            this.targetValue = targetValue;
        }

        public String getKey() { return key; }
        public DiffType getDiffType() { return diffType; }
        public String getBaseValue() { return baseValue; }
        public String getTargetValue() { return targetValue; }
    }

    private final String baseEnvironment;
    private final String targetEnvironment;
    private final Instant generatedAt;
    private final List<Entry> entries;

    public ConfigCompareReport(String baseEnvironment, String targetEnvironment, List<Entry> entries) {
        this.baseEnvironment = baseEnvironment;
        this.targetEnvironment = targetEnvironment;
        this.generatedAt = Instant.now();
        this.entries = Collections.unmodifiableList(entries);
    }

    public String getBaseEnvironment() { return baseEnvironment; }
    public String getTargetEnvironment() { return targetEnvironment; }
    public Instant getGeneratedAt() { return generatedAt; }
    public List<Entry> getEntries() { return entries; }

    public long countByType(DiffType type) {
        return entries.stream().filter(e -> e.getDiffType() == type).count();
    }

    public boolean hasChanges() {
        return entries.stream().anyMatch(e -> e.getDiffType() != DiffType.UNCHANGED);
    }
}
