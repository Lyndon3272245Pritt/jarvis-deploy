package com.jarvis.deploy.config;

import java.util.Objects;

/**
 * Computes the diff between two configuration snapshots using EnvironmentDiff.
 */
public class SnapshotDiffService {

    private final EnvironmentDiff environmentDiff;

    public SnapshotDiffService(EnvironmentDiff environmentDiff) {
        Objects.requireNonNull(environmentDiff, "environmentDiff must not be null");
        this.environmentDiff = environmentDiff;
    }

    /**
     * Computes the diff between two snapshots.
     *
     * @param baseline the baseline snapshot
     * @param target   the target snapshot to compare against
     * @return a DiffResult describing the changes
     * @throws IllegalArgumentException if either snapshot is null
     */
    public EnvironmentDiff.DiffResult diff(ConfigSnapshot baseline, ConfigSnapshot target) {
        if (baseline == null) {
            throw new IllegalArgumentException("baseline snapshot must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("target snapshot must not be null");
        }
        EnvironmentConfig baseConfig = new EnvironmentConfig(baseline.getEnvironment(), baseline.getProperties());
        EnvironmentConfig targetConfig = new EnvironmentConfig(target.getEnvironment(), target.getProperties());
        return environmentDiff.compare(baseConfig, targetConfig);
    }
}
