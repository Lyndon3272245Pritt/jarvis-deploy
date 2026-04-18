package com.jarvis.deploy.config;

import java.util.*;

/**
 * Synchronizes configuration between two environments by applying missing
 * or differing keys from a source environment to a target environment.
 */
public class ConfigSyncService {

    private final EnvironmentDiff environmentDiff;
    private final ConfigAuditLog auditLog;

    public ConfigSyncService(EnvironmentDiff environmentDiff, ConfigAuditLog auditLog) {
        this.environmentDiff = environmentDiff;
        this.auditLog = auditLog;
    }

    /**
     * Syncs keys from source into target. Only adds/updates keys that differ;
     * never removes keys from target.
     *
     * @return a SyncResult describing what changed
     */
    public SyncResult sync(EnvironmentConfig source, EnvironmentConfig target, String actor) {
        Map<String, String> sourceProps = source.getProperties();
        Map<String, String> targetProps = new LinkedHashMap<>(target.getProperties());

        List<String> added = new ArrayList<>();
        List<String> updated = new ArrayList<>();

        for (Map.Entry<String, String> entry : sourceProps.entrySet()) {
            String key = entry.getKey();
            String sourceVal = entry.getValue();
            if (!targetProps.containsKey(key)) {
                targetProps.put(key, sourceVal);
                added.add(key);
                auditLog.record(new ConfigAuditEntry(actor, "SYNC_ADD", key, null, sourceVal, target.getEnvironmentName()));
            } else if (!targetProps.get(key).equals(sourceVal)) {
                String oldVal = targetProps.get(key);
                targetProps.put(key, sourceVal);
                updated.add(key);
                auditLog.record(new ConfigAuditEntry(actor, "SYNC_UPDATE", key, oldVal, sourceVal, target.getEnvironmentName()));
            }
        }

        target.setProperties(targetProps);
        return new SyncResult(source.getEnvironmentName(), target.getEnvironmentName(), added, updated);
    }

    /**
     * Dry-run: returns what would change without modifying the target.
     */
    public SyncResult preview(EnvironmentConfig source, EnvironmentConfig target) {
        Map<String, String> sourceProps = source.getProperties();
        Map<String, String> targetProps = target.getProperties();

        List<String> added = new ArrayList<>();
        List<String> updated = new ArrayList<>();

        for (Map.Entry<String, String> entry : sourceProps.entrySet()) {
            String key = entry.getKey();
            String sourceVal = entry.getValue();
            if (!targetProps.containsKey(key)) {
                added.add(key);
            } else if (!targetProps.get(key).equals(sourceVal)) {
                updated.add(key);
            }
        }

        return new SyncResult(source.getEnvironmentName(), target.getEnvironmentName(), added, updated);
    }
}
