package com.jarvis.deploy.config;

import java.io.IOException;
import java.nio.file.Path;

/**
 * High-level service that loads a config and exports it in one step.
 */
public class ConfigExportService {

    private final ConfigLoader loader;
    private final ConfigExporter exporter;

    public ConfigExportService(ConfigLoader loader, ConfigExporter exporter) {
        this.loader = loader;
        this.exporter = exporter;
    }

    /**
     * Loads the config for the given environment and exports it to {@code destination}.
     *
     * @param environment the environment name (e.g. "prod", "staging")
     * @param destination the file path to write the exported config to
     * @param format      the desired output format
     * @throws ExportException if loading or writing fails
     */
    public void exportEnvironment(String environment, Path destination, ConfigExporter.Format format) {
        EnvironmentConfig config;
        try {
            config = loader.load(environment);
        } catch (ConfigLoadException e) {
            throw new ExportException("Failed to load config for environment '" + environment + "'", e);
        }

        try {
            exporter.export(config, destination, format);
        } catch (IOException e) {
            throw new ExportException(
                    "Failed to write exported config to '" + destination + "'", e);
        }
    }
}
