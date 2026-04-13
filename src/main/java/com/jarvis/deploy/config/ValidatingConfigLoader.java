package com.jarvis.deploy.config;

import java.nio.file.Path;

/**
 * A decorator around {@link ConfigLoader} that automatically validates
 * each loaded {@link EnvironmentConfig} using {@link EnvironmentValidator}.
 */
public class ValidatingConfigLoader {

    private final ConfigLoader configLoader;
    private final EnvironmentValidator validator;

    public ValidatingConfigLoader(ConfigLoader configLoader) {
        this(configLoader, new EnvironmentValidator());
    }

    public ValidatingConfigLoader(ConfigLoader configLoader, EnvironmentValidator validator) {
        if (configLoader == null) throw new IllegalArgumentException("configLoader must not be null");
        if (validator == null) throw new IllegalArgumentException("validator must not be null");
        this.configLoader = configLoader;
        this.validator = validator;
    }

    /**
     * Loads the environment config from the given path and validates it.
     *
     * @param configPath path to the config file
     * @param environment the target environment name (e.g. "production")
     * @return a validated {@link EnvironmentConfig}
     * @throws ConfigLoadException if loading or validation fails
     */
    public EnvironmentConfig loadAndValidate(Path configPath, String environment) throws ConfigLoadException {
        EnvironmentConfig config = configLoader.load(configPath, environment);
        validator.validateOrThrow(config);
        return config;
    }

    /**
     * Checks whether a config at the given path is valid without throwing.
     *
     * @param configPath  path to the config file
     * @param environment the target environment name
     * @return true if the config loads and passes validation, false otherwise
     */
    public boolean isValid(Path configPath, String environment) {
        try {
            loadAndValidate(configPath, environment);
            return true;
        } catch (ConfigLoadException e) {
            return false;
        }
    }
}
