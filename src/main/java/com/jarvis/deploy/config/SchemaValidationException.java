package com.jarvis.deploy.config;

import java.util.List;

/**
 * Thrown when a config fails schema validation and strict mode is enabled.
 */
public class SchemaValidationException extends RuntimeException {

    private final List<String> errors;

    public SchemaValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
