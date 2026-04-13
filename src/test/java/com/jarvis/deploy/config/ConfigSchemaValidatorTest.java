package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSchemaValidatorTest {

    private static final Set<String> REQUIRED = Set.of("app.name", "app.port");
    private static final Set<String> ALLOWED  = Set.of("app.name", "app.port", "app.debug");

    private ConfigSchemaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConfigSchemaValidator(REQUIRED, ALLOWED);
    }

    private EnvironmentConfig configWith(Map<String, String> props) {
        return new EnvironmentConfig("test", props);
    }

    @Test
    void validConfig_passesValidation() {
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "jarvis");
        props.put("app.port", "8080");

        SchemaValidationResult result = validator.validate(configWith(props));

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void missingRequiredKey_producesError() {
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "jarvis");
        // app.port missing

        SchemaValidationResult result = validator.validate(configWith(props));

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("app.port"));
    }

    @Test
    void blankRequiredValue_producesError() {
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "");
        props.put("app.port", "8080");

        SchemaValidationResult result = validator.validate(configWith(props));

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).contains("app.name"));
    }

    @Test
    void unknownKey_producesError() {
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "jarvis");
        props.put("app.port", "8080");
        props.put("app.unknown", "value");

        SchemaValidationResult result = validator.validate(configWith(props));

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).contains("app.unknown"));
    }

    @Test
    void emptyAllowedKeys_skipsUnknownKeyCheck() {
        ConfigSchemaValidator lenient = new ConfigSchemaValidator(REQUIRED, Set.of());
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "jarvis");
        props.put("app.port", "9090");
        props.put("anything", "goes");

        SchemaValidationResult result = lenient.validate(props == null ? null : new EnvironmentConfig("test", props));

        assertTrue(result.isValid());
    }

    @Test
    void nullConfig_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
    }
}
