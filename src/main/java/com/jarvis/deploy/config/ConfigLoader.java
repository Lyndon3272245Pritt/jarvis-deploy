package com.jarvis.deploy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Loads {@link EnvironmentConfig} from YAML files on disk or classpath.
 */
public class ConfigLoader {

    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final String DEFAULT_CONFIG_DIR = "configs";

    private final ObjectMapper mapper;
    private final String configDir;

    public ConfigLoader() {
        this(DEFAULT_CONFIG_DIR);
    }

    public ConfigLoader(String configDir) {
        this.configDir = configDir;
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.mapper.findAndRegisterModules();
    }

    /**
     * Loads the config for the given environment name.
     * Looks for a file named "<env>.yaml" in the configured directory.
     *
     * @param environment the environment name (e.g. "dev", "prod")
     * @return an Optional containing the loaded config, or empty if not found
     */
    public Optional<EnvironmentConfig> load(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment name must not be null or blank");
        }

        Path filePath = Paths.get(configDir, environment + ".yaml");

        if (Files.exists(filePath)) {
            return loadFromFile(filePath.toFile());
        }

        // Fallback to classpath
        String classpathResource = configDir + "/" + environment + ".yaml";
        InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathResource);
        if (stream != null) {
            return loadFromStream(stream, classpathResource);
        }

        LOGGER.warning("No config found for environment: " + environment);
        return Optional.empty();
    }

    private Optional<EnvironmentConfig> loadFromFile(File file) {
        try {
            EnvironmentConfig config = mapper.readValue(file, EnvironmentConfig.class);
            LOGGER.info("Loaded config from file: " + file.getPath());
            return Optional.of(config);
        } catch (IOException e) {
            LOGGER.severe("Failed to parse config file " + file.getPath() + ": " + e.getMessage());
            throw new ConfigLoadException("Failed to load config from " + file.getPath(), e);
        }
    }

    private Optional<EnvironmentConfig> loadFromStream(InputStream stream, String source) {
        try {
            EnvironmentConfig config = mapper.readValue(stream, EnvironmentConfig.class);
            LOGGER.info("Loaded config from classpath: " + source);
            return Optional.of(config);
        } catch (IOException e) {
            LOGGER.severe("Failed to parse classpath config " + source + ": " + e.getMessage());
            throw new ConfigLoadException("Failed to load config from classpath: " + source, e);
        }
    }
}
