package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigCompareServiceTest {

    private ConfigCompareService service;

    @BeforeEach
    void setUp()CompareService();
    }

    private EnvironmentConfig env(String name, Map<String, String> props) {
        EnvironmentConfig cfg = new EnvironmentConfig(name);
        props.forEach(cfg::setProperty);
        return cfg;
    }

    @Test
    void detectsAddedKey() {
        EnvironmentConfig base = env("dev", Map.of("a", "1"));
        EnvironmentConfig target = env("prod", Map.of("a", "1", "b", "2"));
        ConfigCompareReport report = service.compare(base, target);
        assertEquals(1, report.countByType(ConfigCompareReport.DiffType.ADDED));
        assertTrue(report.hasChanges());
    }

    @Test
    void detectsRemovedKey() {
        EnvironmentConfig base = env("dev", Map.of("a", "1", "b", "2"));
        EnvironmentConfig target = env("prod", Map.of("a", "1"));
        ConfigCompareReport report = service.compare(base, target);
        assertEquals(1, report.countByType(ConfigCompareReport.DiffType.REMOVED));
    }

    @Test
    void detectsModifiedKey() {
        EnvironmentConfig base = env("dev", Map.of("a", "1"));
        EnvironmentConfig target = env("prod", Map.of("a", "2"));
        ConfigCompareReport report = service.compare(base, target);
        assertEquals(1, report.countByType(ConfigCompareReport.DiffType.MODIFIED));
    }

    @Test
    void detectsUnchangedKey() {
        EnvironmentConfig base = env("dev", Map.of("a", "1"));
        EnvironmentConfig target = env("prod", Map.of("a", "1"));
        ConfigCompareReport report = service.compare(base, target);
        assertEquals(1, report.countByType(ConfigCompareReport.DiffType.UNCHANGED));
        assertFalse(report.hasChanges());
    }

    @Test
    void throwsOnNullBase() {
        EnvironmentConfig target = env("prod", Map.of());
        assertThrows(IllegalArgumentException.class, () -> service.compare(null, target));
    }

    @Test
    void throwsOnNullTarget() {
        EnvironmentConfig base = env("dev", Map.of());
        assertThrows(IllegalArgumentException.class, () -> service.compare(base, null));
    }

    @Test
    void reportContainsEnvironmentNames() {
        EnvironmentConfig base = env("dev", Map.of());
        EnvironmentConfig target = env("prod", Map.of());
        ConfigCompareReport report = service.compare(base, target);
        assertEquals("dev", report.getBaseEnvironment());
        assertEquals("prod", report.getTargetEnvironment());
        assertNotNull(report.getGeneratedAt());
    }
}
