package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for capturing, storing, and retrieving configuration snapshots.
 */
public class ConfigSnapshotService {

    private final Map<String, ConfigSnapshot> snapshotStore = new ConcurrentHashMap<>();

    /**
     * Captures a snapshot of the given environment configuration.
     *
     * @param config the environment configuration to snapshot
     * @return the created ConfigSnapshot
     */
    public ConfigSnapshot capture(EnvironmentConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        String snapshotId = UUID.randomUUID().toString();
        ConfigSnapshot snapshot = new ConfigSnapshot(
                config.getEnvironment(),
                config.getProperties(),
                Instant.now(),
                snapshotId
        );
        snapshotStore.put(snapshotId, snapshot);
        return snapshot;
    }

    /**
     * Retrieves a snapshot by its ID.
     *
     * @param snapshotId the snapshot ID
     * @return an Optional containing the snapshot if found
     */
    public Optional<ConfigSnapshot> findById(String snapshotId) {
        return Optional.ofNullable(snapshotStore.get(snapshotId));
    }

    /**
     * Lists all snapshots for a given environment, ordered by capture time descending.
     *
     * @param environment the environment name
     * @return list of snapshots
     */
    public List<ConfigSnapshot> listByEnvironment(String environment) {
        return snapshotStore.values().stream()
                .filter(s -> s.getEnvironment().equals(environment))
                .sorted((a, b) -> b.getCapturedAt().compareTo(a.getCapturedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all stored snapshots.
     */
    public List<ConfigSnapshot> listAll() {
        List<ConfigSnapshot> all = new ArrayList<>(snapshotStore.values());
        all.sort((a, b) -> b.getCapturedAt().compareTo(a.getCapturedAt()));
        return Collections.unmodifiableList(all);
    }

    /**
     * Removes a snapshot by ID.
     *
     * @param snapshotId the snapshot ID
     * @return true if removed, false if not found
     */
    public boolean delete(String snapshotId) {
        return snapshotStore.remove(snapshotId) != null;
    }
}
