package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages named change sets that group multiple config key-value updates
 * into a single atomic logical unit before applying them to an environment.
 */
public class ConfigChangeSetManager {

    public static class ChangeSet {
        private final String id;
        private final String environment;
        private final String author;
        private final Instant createdAt;
        private final Map<String, String> changes;
        private boolean applied;

        public ChangeSet(String id, String environment, String author) {
            this.id = id;
            this.environment = environment;
            this.author = author;
            this.createdAt = Instant.now();
            this.changes = new LinkedHashMap<>();
            this.applied = false;
        }

        public String getId() { return id; }
        public String getEnvironment() { return environment; }
        public String getAuthor() { return author; }
        public Instant getCreatedAt() { return createdAt; }
        public Map<String, String> getChanges() { return Collections.unmodifiableMap(changes); }
        public boolean isApplied() { return applied; }

        void addChange(String key, String value) {
            if (applied) throw new IllegalStateException("ChangeSet already applied: " + id);
            changes.put(key, value);
        }

        void markApplied() { this.applied = true; }
    }

    private final Map<String, ChangeSet> changeSets = new ConcurrentHashMap<>();

    public ChangeSet createChangeSet(String id, String environment, String author) {
        if (changeSets.containsKey(id)) {
            throw new IllegalArgumentException("ChangeSet already exists with id: " + id);
        }
        ChangeSet cs = new ChangeSet(id, environment, author);
        changeSets.put(id, cs);
        return cs;
    }

    public void addChange(String changeSetId, String key, String value) {
        ChangeSet cs = getOrThrow(changeSetId);
        cs.addChange(key, value);
    }

    public EnvironmentConfig applyChangeSet(String changeSetId, EnvironmentConfig base) {
        ChangeSet cs = getOrThrow(changeSetId);
        if (cs.isApplied()) {
            throw new IllegalStateException("ChangeSet already applied: " + changeSetId);
        }
        Map<String, String> merged = new LinkedHashMap<>(base.getProperties());
        merged.putAll(cs.getChanges());
        cs.markApplied();
        return new EnvironmentConfig(base.getEnvironmentName(), merged);
    }

    public Optional<ChangeSet> findChangeSet(String id) {
        return Optional.ofNullable(changeSets.get(id));
    }

    public List<ChangeSet> listChangeSets(String environment) {
        List<ChangeSet> result = new ArrayList<>();
        for (ChangeSet cs : changeSets.values()) {
            if (cs.getEnvironment().equals(environment)) {
                result.add(cs);
            }
        }
        result.sort(Comparator.comparing(ChangeSet::getCreatedAt));
        return Collections.unmodifiableList(result);
    }

    private ChangeSet getOrThrow(String id) {
        ChangeSet cs = changeSets.get(id);
        if (cs == null) throw new NoSuchElementException("ChangeSet not found: " + id);
        return cs;
    }
}
