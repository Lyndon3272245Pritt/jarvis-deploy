package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Computes and represents the diff between two {@link EnvironmentConfig} instances.
 * Useful for reviewing changes before promoting configs across environments.
 */
public class EnvironmentDiff {

    public enum ChangeType { ADDED, REMOVED, MODIFIED }

    public static class Change {
        private final String key;
        private final ChangeType type;
        private final String oldValue;
        private final String newValue;

        public Change(String key, ChangeType type, String oldValue, String newValue) {
            this.key = key;
            this.type = type;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getKey() { return key; }
        public ChangeType getType() { return type; }
        public String getOldValue() { return oldValue; }
        public String getNewValue() { return newValue; }

        @Override
        public String toString() {
            return switch (type) {
                case ADDED    -> String.format("[ADDED]    %s = %s", key, newValue);
                case REMOVED  -> String.format("[REMOVED]  %s (was: %s)", key, oldValue);
                case MODIFIED -> String.format("[MODIFIED] %s: %s -> %s", key, oldValue, newValue);
            };
        }
    }

    private final List<Change> changes;

    private EnvironmentDiff(List<Change> changes) {
        this.changes = Collections.unmodifiableList(changes);
    }

    /**
     * Computes the diff between {@code base} and {@code target} configs.
     *
     * @param base   the source environment (e.g. staging)
     * @param target the destination environment (e.g. production)
     * @return an {@code EnvironmentDiff} describing what changed
     */
    public static EnvironmentDiff compute(EnvironmentConfig base, EnvironmentConfig target) {
        Map<String, String> baseProps   = base.getProperties();
        Map<String, String> targetProps = target.getProperties();

        List<Change> changes = new ArrayList<>();
        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(baseProps.keySet());
        allKeys.addAll(targetProps.keySet());

        for (String key : allKeys) {
            boolean inBase   = baseProps.containsKey(key);
            boolean inTarget = targetProps.containsKey(key);

            if (inBase && !inTarget) {
                changes.add(new Change(key, ChangeType.REMOVED, baseProps.get(key), null));
            } else if (!inBase && inTarget) {
                changes.add(new Change(key, ChangeType.ADDED, null, targetProps.get(key)));
            } else if (!baseProps.get(key).equals(targetProps.get(key))) {
                changes.add(new Change(key, ChangeType.MODIFIED, baseProps.get(key), targetProps.get(key)));
            }
        }
        return new EnvironmentDiff(changes);
    }

    public List<Change> getChanges() { return changes; }

    public boolean isEmpty() { return changes.isEmpty(); }

    public void print() {
        if (isEmpty()) {
            System.out.println("No differences found.");
        } else {
            changes.forEach(c -> System.out.println(c.toString()));
        }
    }
}
