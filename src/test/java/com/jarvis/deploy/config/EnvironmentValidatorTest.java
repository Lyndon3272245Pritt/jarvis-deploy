package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentValidatorTest {

    private EnvironmentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EnvironmentValidator();
    }

    private EnvironmentConfig validConfig() {
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "jarvis-app");
        props.put("deploy.host", "prod.example.com");
        props.put("deploy.port", "8080");
        return new EnvironmentConfig("production", props);
    }

    @Test
    void testValidConfigProducesNoErrors() {
        List<String> errors = validator.validate(validConfig());
        assertTrue(errors.isEmpty(), "Expected no validation errors for a valid config");
    }

    @Test
    void testNullConfigReturnsError() {
        List<String> errors = validator.validate(null);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("null"));
    }

    @Test
    void testMissingEnvironmentNameReturnsError() {
        EnvironmentConfig config = new EnvironmentConfig("", Map.of(
                "app.name", "jarvis", "deploy.host", "localhost", "deploy.port", "9090"));
        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Environment name")));
    }

    @Test
    void testMissingRequiredPropertyReturnsError() {
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "jarvis");
        props.put("deploy.host", "localhost");
        // deploy.port intentionally omitted
        EnvironmentConfig config = new EnvironmentConfig("staging", props);
        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("deploy.port")));
    }

    @Test
    void testInvalidPortFormatReturnsError() {
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "jarvis");
        props.put("deploy.host", "localhost");
        props.put("deploy.port", "not-a-number");
        EnvironmentConfig config = new EnvironmentConfig("dev", props);
        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("valid integer")));
    }

    @Test
    void testOutOfRangePortReturnsError() {
        Map<String, String> props = new HashMap<>();
        props.put("app.name", "jarvis");
        props.put("deploy.host", "localhost");
        props.put("deploy.port", "99999");
        EnvironmentConfig config = new EnvironmentConfig("dev", props);
        List<String> errors = validator.validate(config);
        assertTrue(errors.stream().anyMatch(e -> e.contains("65535")));
    }

    @Test
    void testValidateOrThrowThrowsOnInvalidConfig() {
        EnvironmentConfig config = new EnvironmentConfig("broken", Map.of());
        assertThrows(ConfigLoadException.class, () -> validator.validateOrThrow(config));
    }

    @Test
    void testValidateOrThrowDoesNotThrowOnValidConfig() {
        assertDoesNotThrow(() -> validator.validateOrThrow(validConfig()));
    }
}
