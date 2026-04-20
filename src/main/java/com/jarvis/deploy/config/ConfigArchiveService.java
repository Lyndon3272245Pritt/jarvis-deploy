package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages archiving of environment configurations. Archived entries are
 * immutable historical records distinct from rolling snapshots or versions.
 */
public class ConfigArchiveService {

    private final Map<String, ConfigArchiveEntry> archiveStore = new ConcurrentHashMap<>();
    private final ConfigArchiveIdGenerator idGenerator;

    public ConfigArchiveService(ConfigArchiveIdGenerator idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator must not be null");
    }

    /**
     * Archives the current state of the given environment config.
     *
     * @return the created archive entry
     */
    public ConfigArchiveEntry archive(EnvironmentConfig config, String archivedBy, String reason) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(archivedBy, "archivedBy must not be null");

        String archiveId = idGenerator.generate(config.getEnvironment());
        ConfigArchiveEntry entry = new ConfigArchiveEntry(
                archiveId,
                config.getEnvironment(),
                new HashMap<>(config.getProperties()),
                Instant.now(),
                archivedBy,
                reason
        );
        archiveStore.put(archiveId, entry);
        return entry;
    }

    /**
     * Retrieves an archive entry by its unique ID.
     */
    public Optional<ConfigArchiveEntry> findById(String archiveId) {
        return Optional.ofNullable(archiveStore.get(archiveId));
    }

    /**
     * Lists all archive entries for a given environment, sorted newest first.
     */
    public List<ConfigArchiveEntry> listByEnvironment(String environment) {
        return archiveStore.values().stream()
                .filter(e -> e.getEnvironment().equals(environment))
                .sorted(Comparator.comparing(ConfigArchiveEntry::getArchivedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Deletes an archive entry by ID. Returns true if removed, false if not found.
     */
    public boolean delete(String archiveId) {
        return archiveStore.remove(archiveId) != null;
    }

    /**
     * Returns the total number of archived entries.
     */
    public int size() {
        return archiveStore.size();
    }
}
