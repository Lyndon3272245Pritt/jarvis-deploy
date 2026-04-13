package com.jarvis.deploy.config;

import java.nio.file.Path;
import java.util.Objects;

/**
 * High-level service that imports a configuration file and immediately
 * registers it with the ConfigVersionRegistry so the imported config
 * participates in versioning and rollback workflows.
 */
public class ConfigImportService {

    private final ConfigImporter importer;
    private final ConfigVersionRegistry versionRegistry;

    public ConfigImportService(ConfigImporter importer, ConfigVersionRegistry versionRegistry) {
        this.importer = Objects.requireNonNull(importer, "importer must not be null");
        this.versionRegistry = Objects.requireNonNull(versionRegistry, "versionRegistry must not be null");
    }

    /**
     * Imports a file and registers the resulting config as a new version.
     *
     * @param filePath   path to the source file
     * @param environment target environment label
     * @param format     file format
     * @param author     who is performing the import
     * @return the imported EnvironmentConfig
     * @throws ImportException if import or registration fails
     */
    public EnvironmentConfig importAndRegister(
            Path filePath,
            String environment,
            ConfigImporter.ImportFormat format,
            String author) throws ImportException {

        EnvironmentConfig config = importer.importFromFile(filePath, environment, format);
        try {
            versionRegistry.register(environment, config, author);
        } catch (Exception e) {
            throw new ImportException(
                    "Config imported but version registration failed for env '" + environment + "'", e);
        }
        return config;
    }

    /**
     * Imports from a raw string and registers the resulting config.
     */
    public EnvironmentConfig importStringAndRegister(
            String content,
            String environment,
            ConfigImporter.ImportFormat format,
            String author) throws ImportException {

        EnvironmentConfig config = importer.importFromString(content, environment, format);
        try {
            versionRegistry.register(environment, config, author);
        } catch (Exception e) {
            throw new ImportException(
                    "Config imported but version registration failed for env '" + environment + "'", e);
        }
        return config;
    }
}
