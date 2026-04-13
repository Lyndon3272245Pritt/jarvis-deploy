package com.jarvis.deploy.config;

import java.util.Collections;
import java.util.List;

/**
 * Holds the result of a schema validation, including any error messages.
 */
public class SchemaValidationResult {

    private final List<String> errors;

    public SchemaValidationResult(List<String> errors) {
        this.errors = errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "SchemaValidationResult{valid=true}";
        }
        return "SchemaValidationResult{valid=false, errors=" + errors + "}";
    }
}
