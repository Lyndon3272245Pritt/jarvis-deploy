package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.*;

/**
 * Registry that tracks versioned configurations per environment.
 * Each time a config is registered, a new version entry is created.
 */
public class ConfigVersionRegistry {

    private final Map<String, List<VersionEntry>> registry = new LinkedHashMap<>();

    /**
     * Registers a new version of the given environment config.
     *
     * @param environment the environment name (e.g. "prod", "staging")
     * @param config      the EnvironmentConfig to version
     * @return the version number assigned (1-based)
     */
    public int register(String environment, EnvironmentConfig config) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment name must not be blank");
        }
        Objects.requireNonNull(config, "Config must not be null");

        registry.putIfAbsent(environment, new ArrayList<>());
        List<VersionEntry> versions = registry.get(environment);
        int version = versions.size() + 1;
        versions.add(new VersionEntry(version, config, Instant.now()));
        return version;
    }

    /**
     * Retrieves a specific version of a config for the given environment.
     *
     * @param environment the environment name
     * @param version     the 1-based version number
     * @return an Optional containing the VersionEntry if found
     */
    public Optional<VersionEntry> getVersion(String environment, int version) {
        List<VersionEntry> versions = registry.getOrDefault(environment, Collections.emptyList());
        return versions.stream().filter(e -> e.version() == version).findFirst();
    }

    /**
     * Returns the latest registered version entry for the given environment.
     */
    public Optional<VersionEntry> getLatest(String environment) {
        List<VersionEntry> versions = registry.getOrDefault(environment, Collections.emptyList());
        if (versions.isEmpty()) return Optional.empty();
        return Optional.of(versions.get(versions.size() - 1));
    }

    /**
     * Returns all version entries for the given environment.
     */
    public List<VersionEntry> getHistory(String environment) {
        return Collections.unmodifiableList(
                registry.getOrDefault(environment, Collections.emptyList()));
    }

    /**
     * Returns all environment names currently tracked.
     */
    public Set<String> getEnvironments() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /**
     * Immutable record representing a single versioned config entry.
     */
    public record VersionEntry(int version, EnvironmentConfig config, Instant registeredAt) {}
}
