package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigHealthCheckerTest {

    private ConfigHealthChecker checker;

    @BeforeEach
    void setUp() {
        checker = new ConfigHealthChecker(Arrays.asList("db.url", "db.password", "app.port"));
    }

    private EnvironmentConfig buildConfig(Map<String, String> props) {
        EnvironmentConfig config = new EnvironmentConfig("test");
        props.forEach(config::setProperty);
        return config;
    }

    @Test
    void healthy_whenAllRequiredKeysPresent() {
        Map<String, String> props = new HashMap<>();
        props.put("db.url", "jdbc:postgresql://localhost/mydb");
        props.put("db.password", "secret");
        props.put("app.port", "8080");

        ConfigHealthChecker.HealthCheckResult result = checker.check(buildConfig(props));

        assertTrue(result.isHealthy());
        assertTrue(result.getIssues().isEmpty());
    }

    @Test
    void unhealthy_whenRequiredKeyMissing() {
        Map<String, String> props = new HashMap<>();
        props.put("db.url", "jdbc:postgresql://localhost/mydb");
        props.put("app.port", "8080");
        // db.password is missing

        ConfigHealthChecker.HealthCheckResult result = checker.check(buildConfig(props));

        assertFalse(result.isHealthy());
        assertEquals(1, result.getIssues().size());
        assertTrue(result.getIssues().get(0).contains("db.password"));
    }

    @Test
    void unhealthy_whenRequiredValueIsEmpty() {
        Map<String, String> props = new HashMap<>();
        props.put("db.url", "jdbc:postgresql://localhost/mydb");
        props.put("db.password", "   ");
        props.put("app.port", "8080");

        ConfigHealthChecker.HealthCheckResult result = checker.check(buildConfig(props));

        assertFalse(result.isHealthy());
        assertTrue(result.getIssues().stream().anyMatch(i -> i.contains("db.password")));
    }

    @Test
    void unhealthy_whenUnresolvedPlaceholderPresent() {
        Map<String, String> props = new HashMap<>();
        props.put("db.url", "jdbc:postgresql://${db.host}/mydb");
        props.put("db.password", "secret");
        props.put("app.port", "8080");

        ConfigHealthChecker.HealthCheckResult result = checker.check(buildConfig(props));

        assertFalse(result.isHealthy());
        assertTrue(result.getIssues().stream().anyMatch(i -> i.contains("db.url")));
    }

    @Test
    void healthy_withNoRequiredKeys() {
        ConfigHealthChecker noRequirements = new ConfigHealthChecker(Collections.emptyList());
        ConfigHealthChecker.HealthCheckResult result = noRequirements.check(buildConfig(Collections.emptyMap()));

        assertTrue(result.isHealthy());
    }

    @Test
    void constructor_throwsOnNullRequiredKeys() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigHealthChecker(null));
    }

    @Test
    void check_throwsOnNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> checker.check(null));
    }

    @Test
    void toStringReflectsHealthStatus() {
        Map<String, String> props = new HashMap<>();
        props.put("db.url", "jdbc:postgresql://localhost/mydb");
        props.put("db.password", "secret");
        props.put("app.port", "8080");

        ConfigHealthChecker.HealthCheckResult result = checker.check(buildConfig(props));
        assertTrue(result.toString().contains("healthy"));
    }
}
