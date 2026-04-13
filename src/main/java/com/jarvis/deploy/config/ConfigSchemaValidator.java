package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates an EnvironmentConfig against a defined schema (required keys and allowed keys).
 */
public class ConfigSchemaValidator {

    private final Set<String> requiredKeys;
    private final Set<String> allowedKeys;

    public ConfigSchemaValidator(Set<String> requiredKeys, Set<String> allowedKeys) {
        if (requiredKeys == null) throw new IllegalArgumentException("requiredKeys must not be null");
        if (allowedKeys == null) throw new IllegalArgumentException("allowedKeys must not be null");
        this.requiredKeys = Set.copyOf(requiredKeys);
        this.allowedKeys = Set.copyOf(allowedKeys);
    }

    /**
     * Validates the given config against the schema.
     *
     * @param config the environment config to validate
     * @return a SchemaValidationResult containing any violations found
     */
    public SchemaValidationResult validate(EnvironmentConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");

        List<String> errors = new ArrayList<>();
        Map<String, String> properties = config.getProperties();

        for (String required : requiredKeys) {
            if (!properties.containsKey(required) || properties.get(required) == null || properties.get(required).isBlank()) {
                errors.add("Missing required key: '" + required + "'");
            }
        }

        if (!allowedKeys.isEmpty()) {
            for (String key : properties.keySet()) {
                if (!allowedKeys.contains(key)) {
                    errors.add("Unknown key not in schema: '" + key + "'");
                }
            }
        }

        return new SchemaValidationResult(errors);
    }

    public Set<String> getRequiredKeys() {
        return requiredKeys;
    }

    public Set<String> getAllowedKeys() {
        return allowedKeys;
    }
}
