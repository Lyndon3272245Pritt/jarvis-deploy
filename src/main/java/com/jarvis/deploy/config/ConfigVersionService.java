package com.jarvis.deploy.config;

import java.util.List;
import java.util.Optional;

/**
 * High-level service for managing config versioning.
 * Wraps ConfigVersionRegistry and integrates with EnvironmentDiff
 * to provide rollback and diff-between-versions capabilities.
 */
public class ConfigVersionService {

    private final ConfigVersionRegistry registry;
    private final EnvironmentDiff environmentDiff;

    public ConfigVersionService(ConfigVersionRegistry registry, EnvironmentDiff environmentDiff) {
        this.registry = registry;
        this.environmentDiff = environmentDiff;
    }

    /**
     * Saves a new version of the config for the specified environment.
     *
     * @return the assigned version number
     */
    public int saveVersion(String environment, EnvironmentConfig config) {
        return registry.register(environment, config);
    }

    /**
     * Rolls back to a specific version for the given environment.
     *
     * @param environment the target environment
     * @param version     the version to roll back to
     * @return the EnvironmentConfig at that version
     * @throws ConfigVersionException if the version does not exist
     */
    public EnvironmentConfig rollback(String environment, int version) {
        return registry.getVersion(environment, version)
                .map(ConfigVersionRegistry.VersionEntry::config)
                .orElseThrow(() -> new ConfigVersionException(
                        "Version " + version + " not found for environment: " + environment));
    }

    /**
     * Returns a diff between two versions of the same environment config.
     *
     * @param environment the environment name
     * @param fromVersion the base version
     * @param toVersion   the target version
     * @return a map of differing keys to their [fromValue, toValue] pairs
     */
    public java.util.Map<String, String[]> diffVersions(String environment, int fromVersion, int toVersion) {
        EnvironmentConfig from = rollback(environment, fromVersion);
        EnvironmentConfig to = rollback(environment, toVersion);
        return environmentDiff.diff(from, to);
    }

    /**
     * Returns the full version history for an environment.
     */
    public List<ConfigVersionRegistry.VersionEntry> getHistory(String environment) {
        return registry.getHistory(environment);
    }

    /**
     * Returns the latest version entry for an environment, if any.
     */
    public Optional<ConfigVersionRegistry.VersionEntry> getLatest(String environment) {
        return registry.getLatest(environment);
    }
}
