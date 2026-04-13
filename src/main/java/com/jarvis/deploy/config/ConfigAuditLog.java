package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory audit log that records configuration lifecycle events.
 */
public class ConfigAuditLog {

    private final List<ConfigAuditEntry> entries = new ArrayList<>();

    public synchronized void record(String environment, ConfigAuditEntry.Action action, String details) {
        entries.add(new ConfigAuditEntry(environment, action, details));
    }

    public synchronized List<ConfigAuditEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized List<ConfigAuditEntry> getByEnvironment(String environment) {
        if (environment == null) {
            return Collections.emptyList();
        }
        return entries.stream()
                .filter(e -> environment.equals(e.getEnvironment()))
                .collect(Collectors.toUnmodifiableList());
    }

    public synchronized List<ConfigAuditEntry> getByAction(ConfigAuditEntry.Action action) {
        if (action == null) {
            return Collections.emptyList();
        }
        return entries.stream()
                .filter(e -> action == e.getAction())
                .collect(Collectors.toUnmodifiableList());
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized int size() {
        return entries.size();
    }
}
