package com.jarvis.deploy.config;

import java.nio.file.Path;

/**
 * A ConfigLoader decorator that validates loaded configs against a schema.
 * In strict mode, a SchemaValidationException is thrown on violations.
 * In non-strict mode, violations are logged to stderr and loading continues.
 */
public class SchemaValidatingConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigSchemaValidator schemaValidator;
    private final boolean strict;

    public SchemaValidatingConfigLoader(ConfigLoader delegate,
                                        ConfigSchemaValidator schemaValidator,
                                        boolean strict) {
        if (delegate == null) throw new IllegalArgumentException("delegate must not be null");
        if (schemaValidator == null) throw new IllegalArgumentException("schemaValidator must not be null");
        this.delegate = delegate;
        this.schemaValidator = schemaValidator;
        this.strict = strict;
    }

    /**
     * Loads an EnvironmentConfig from the given path and validates it against the schema.
     *
     * @param path path to the config file
     * @return the loaded and validated EnvironmentConfig
     * @throws ConfigLoadException        if loading fails
     * @throws SchemaValidationException  if strict mode is enabled and validation fails
     */
    public EnvironmentConfig load(Path path) throws ConfigLoadException {
        EnvironmentConfig config = delegate.load(path);
        SchemaValidationResult result = schemaValidator.validate(config);

        if (!result.isValid()) {
            String summary = "Schema validation failed for '" + path + "': " + result.getErrors();
            if (strict) {
                throw new SchemaValidationException(summary, result.getErrors());
            } else {
                System.err.println("[WARN] " + summary);
            }
        }

        return config;
    }

    public boolean isStrict() {
        return strict;
    }
}
