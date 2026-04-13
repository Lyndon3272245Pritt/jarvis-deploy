package com.jarvis.deploy.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.StringReader;

/**
 * Imports configuration from external sources (properties files, env-var style
 * flat files, or simple key=value text) into an EnvironmentConfig.
 */
public class ConfigImporter {

    public enum ImportFormat {
        PROPERTIES,
        ENV
    }

    /**
     * Imports a config file from the given path using the specified format.
     *
     * @param filePath path to the file to import
     * @param environment target environment name
     * @param format the file format to parse
     * @return populated EnvironmentConfig
     * @throws ImportException if the file cannot be read or parsed
     */
    public EnvironmentConfig importFromFile(Path filePath, String environment, ImportFormat format)
            throws ImportException {
        if (filePath == null || !Files.exists(filePath)) {
            throw new ImportException("Import file not found: " + filePath);
        }
        try {
            String content = Files.readString(filePath);
            return parse(content, environment, format);
        } catch (IOException e) {
            throw new ImportException("Failed to read import file: " + filePath, e);
        }
    }

    /**
     * Imports config from a raw string.
     */
    public EnvironmentConfig importFromString(String content, String environment, ImportFormat format)
            throws ImportException {
        if (content == null || content.isBlank()) {
            throw new ImportException("Import content must not be empty");
        }
        return parse(content, environment, format);
    }

    private EnvironmentConfig parse(String content, String environment, ImportFormat format)
            throws ImportException {
        Map<String, String> properties = new HashMap<>();
        try {
            if (format == ImportFormat.PROPERTIES) {
                Properties props = new Properties();
                props.load(new StringReader(content));
                props.forEach((k, v) -> properties.put(k.toString(), v.toString()));
            } else if (format == ImportFormat.ENV) {
                for (String line : content.lines().toList()) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                    int idx = trimmed.indexOf('=');
                    if (idx < 1) throw new ImportException("Invalid ENV line: " + trimmed);
                    String key = trimmed.substring(0, idx).trim();
                    String value = trimmed.substring(idx + 1).trim();
                    properties.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new ImportException("Failed to parse import content", e);
        }
        return new EnvironmentConfig(environment, properties);
    }
}
