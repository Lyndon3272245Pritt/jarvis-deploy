package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates an {@link EnvironmentConfig} against required fields and constraints.
 */
public class EnvironmentValidator {

    private static final List<String> REQUIRED_KEYS = List.of("app.name", "deploy.host", "deploy.port");
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    /**
     * Validates the given config and returns a list of validation errors.
     * An empty list means the config is valid.
     *
     * @param config the environment config to validate
     * @return list of human-readable error messages
     */
    public List<String> validate(EnvironmentConfig config) {
        List<String> errors = new ArrayList<>();

        if (config == null) {
            errors.add("EnvironmentConfig must not be null");
            return errors;
        }

        if (config.getEnvironmentName() == null || config.getEnvironmentName().isBlank()) {
            errors.add("Environment name must not be blank");
        }

        Map<String, String> props = config.getProperties();
        if (props == null || props.isEmpty()) {
            errors.add("Properties map must not be null or empty");
            return errors;
        }

        for (String key : REQUIRED_KEYS) {
            if (!props.containsKey(key) || props.get(key).isBlank()) {
                errors.add("Missing required property: " + key);
            }
        }

        String portValue = props.get("deploy.port");
        if (portValue != null && !portValue.isBlank()) {
            try {
                int port = Integer.parseInt(portValue);
                if (port < MIN_PORT || port > MAX_PORT) {
                    errors.add("deploy.port must be between " + MIN_PORT + " and " + MAX_PORT + ", got: " + port);
                }
            } catch (NumberFormatException e) {
                errors.add("deploy.port must be a valid integer, got: " + portValue);
            }
        }

        return errors;
    }

    /**
     * Validates the config and throws {@link ConfigLoadException} if any errors are found.
     *
     * @param config the environment config to validate
     * @throws ConfigLoadException if validation fails
     */
    public void validateOrThrow(EnvironmentConfig config) throws ConfigLoadException {
        List<String> errors = validate(config);
        if (!errors.isEmpty()) {
            throw new ConfigLoadException("Validation failed for environment config: " + errors);
        }
    }
}
