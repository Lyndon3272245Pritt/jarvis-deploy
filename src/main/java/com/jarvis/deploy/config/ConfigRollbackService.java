package com.jarvis.deploy.config;

import java.util.List;
import java.util.Optional;

/**
 * Service for rolling back environment configurations to a previously
 * captured snapshot.
 */
public class ConfigRollbackService {

    private final ConfigSnapshotService snapshotService;
    private final ConfigVersionRegistry versionRegistry;

    public ConfigRollbackService(ConfigSnapshotService snapshotService,
                                 ConfigVersionRegistry versionRegistry) {
        if (snapshotService == null) throw new IllegalArgumentException("snapshotService must not be null");
        if (versionRegistry == null) throw new IllegalArgumentException("versionRegistry must not be null");
        this.snapshotService = snapshotService;
        this.versionRegistry = versionRegistry;
    }

    /**
     * Rolls back the given environment to the snapshot identified by snapshotId.
     *
     * @param environment the target environment name
     * @param snapshotId  the snapshot to restore
     * @return the restored EnvironmentConfig
     * @throws RollbackException if the snapshot cannot be found or rollback fails
     */
    public EnvironmentConfig rollback(String environment, String snapshotId) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("environment must not be blank");
        }
        if (snapshotId == null || snapshotId.isBlank()) {
            throw new IllegalArgumentException("snapshotId must not be blank");
        }

        List<ConfigSnapshot> snapshots = snapshotService.listSnapshots(environment);
        Optional<ConfigSnapshot> target = snapshots.stream()
                .filter(s -> snapshotId.equals(s.getSnapshotId()))
                .findFirst();

        if (target.isEmpty()) {
            throw new RollbackException(
                    "No snapshot found with id '" + snapshotId + "' for environment '" + environment + "'");
        }

        ConfigSnapshot snapshot = target.get();
        EnvironmentConfig restored = snapshot.getConfig();

        try {
            versionRegistry.register(environment, restored);
        } catch (Exception e) {
            throw new RollbackException("Failed to register rolled-back config as new version: " + e.getMessage(), e);
        }

        return restored;
    }

    /**
     * Rolls back to the most recent snapshot before the current version.
     *
     * @param environment the target environment name
     * @return the restored EnvironmentConfig
     * @throws RollbackException if no prior snapshot exists
     */
    public EnvironmentConfig rollbackToLatest(String environment) {
        List<ConfigSnapshot> snapshots = snapshotService.listSnapshots(environment);
        if (snapshots.isEmpty()) {
            throw new RollbackException("No snapshots available for environment '" + environment + "'");
        }
        ConfigSnapshot latest = snapshots.get(snapshots.size() - 1);
        return rollback(environment, latest.getSnapshotId());
    }
}
