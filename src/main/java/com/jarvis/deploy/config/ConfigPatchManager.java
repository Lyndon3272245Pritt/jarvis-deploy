package com.jarvis.deploy.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages partial patch operations on environment configs,
 * allowing targeted key updates without full config replacement.
 */
public class ConfigPatchManager {

    public enum PatchOperation {
        SET, DELETE
    }

    public record PatchEntry(PatchOperation operation, String key, String value) {
        public PatchEntry(PatchOperation operation, String key) {
            this(operation, key, null);
        }
    }

    private final Map<String, Map<String, PatchEntry>> pendingPatches = new LinkedHashMap<>();

    /**
     * Stages a SET patch for the given environment and key.
     */
    public void stagePatch(String environment, String key, String value) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        pendingPatches
            .computeIfAbsent(environment, e -> new LinkedHashMap<>())
            .put(key, new PatchEntry(PatchOperation.SET, key, value));
    }

    /**
     * Stages a DELETE patch for the given environment and key.
     */
    public void stageDeletion(String environment, String key) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(key, "key must not be null");
        pendingPatches
            .computeIfAbsent(environment, e -> new LinkedHashMap<>())
            .put(key, new PatchEntry(PatchOperation.DELETE, key));
    }

    /**
     * Applies all staged patches for the given environment to the provided config properties.
     * Returns a new map with patches applied.
     */
    public Map<String, String> applyPatches(String environment, Map<String, String> original) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(original, "original config must not be null");
        Map<String, String> result = new HashMap<>(original);
        Map<String, PatchEntry> patches = pendingPatches.getOrDefault(environment, Collections.emptyMap());
        for (PatchEntry entry : patches.values()) {
            if (entry.operation() == PatchOperation.SET) {
                result.put(entry.key(), entry.value());
            } else if (entry.operation() == PatchOperation.DELETE) {
                result.remove(entry.key());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Clears all staged patches for the given environment.
     */
    public void clearPatches(String environment) {
        pendingPatches.remove(environment);
    }

    /**
     * Returns staged patches for a given environment (unmodifiable view).
     */
    public Map<String, PatchEntry> getPendingPatches(String environment) {
        return Collections.unmodifiableMap(
            pendingPatches.getOrDefault(environment, Collections.emptyMap())
        );
    }

    /**
     * Returns true if there are any staged patches for the given environment.
     */
    public boolean hasPendingPatches(String environment) {
        Map<String, PatchEntry> patches = pendingPatches.get(environment);
        return patches != null && !patches.isEmpty();
    }
}
