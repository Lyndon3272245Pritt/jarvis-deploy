package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentConfigTest {

    @Test
    void shouldCreateConfigWithValidFields() {
        Map<String, String> vars = Map.of("DB_HOST", "localhost", "PORT", "5432");
        EnvironmentConfig config = new EnvironmentConfig("dev", "us-east-1", "dev-ns", vars);

        assertEquals("dev", config.getName());
        assertEquals("us-east-1", config.getRegion());
        assertEquals("dev-ns", config.getNamespace());
        assertEquals("localhost", config.getVariable("DB_HOST"));
        assertEquals("5432", config.getVariable("PORT"));
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new EnvironmentConfig(null, "us-east-1", "ns", null));
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new EnvironmentConfig("   ", "us-east-1", "ns", null));
    }

    @Test
    void shouldHandleNullVariablesGracefully() {
        EnvironmentConfig config = new EnvironmentConfig("staging", "eu-west-1", "staging-ns", null);
        assertNotNull(config.getVariables());
        assertTrue(config.getVariables().isEmpty());
    }

    @Test
    void shouldReturnDefensiveCopyOfVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.put("KEY", "value");
        EnvironmentConfig config = new EnvironmentConfig("prod", "us-west-2", "prod-ns", vars);

        Map<String, String> returned = config.getVariables();
        returned.put("INJECTED", "bad");

        assertNull(config.getVariable("INJECTED"));
    }

    @Test
    void shouldBeEqualByName() {
        EnvironmentConfig c1 = new EnvironmentConfig("dev", "us-east-1", "ns1", null);
        EnvironmentConfig c2 = new EnvironmentConfig("dev", "eu-west-1", "ns2", null);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
