package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigPromotionServiceTest {

    private ConfigLoader configLoader;
    private ConfigAuditLog auditLog;
    private ConfigPromotionService service;

    @BeforeEach
    void setUp() {
        configLoader = mock(ConfigLoader.class);
        auditLog = mock(ConfigAuditLog.class);
        service = new ConfigPromotionService(configLoader, auditLog);
    }

    @Test
    void promote_shouldMergeSourcePropertiesIntoTarget() {
        EnvironmentConfig staging = new EnvironmentConfig("staging",
                Map.of("db.host", "staging-db", "feature.x", "true"));
        EnvironmentConfig prod = new EnvironmentConfig("production",
                Map.of("db.host", "prod-db"));

        when(configLoader.load("staging")).thenReturn(staging);
        when(configLoader.load("production")).thenReturn(prod);

        PromotionResult result = service.promote("staging", "production", "alice");

        assertNotNull(result);
        assertEquals("staging", result.getSourceEnv());
        assertEquals("production", result.getTargetEnv());
        assertEquals(1, result.getAddedCount());
        assertEquals(1, result.getUpdatedCount());

        Map<String, String> promotedProps = result.getPromotedConfig().getProperties();
        assertEquals("staging-db", promotedProps.get("db.host"));
        assertEquals("true", promotedProps.get("feature.x"));
    }

    @Test
    void promote_shouldRecordAuditEntry() {
        EnvironmentConfig staging = new EnvironmentConfig("staging", Map.of("key", "val"));
        EnvironmentConfig prod = new EnvironmentConfig("production", Map.of());

        when(configLoader.load("staging")).thenReturn(staging);
        when(configLoader.load("production")).thenReturn(prod);

        service.promote("staging", "production", "bob");

        verify(auditLog, times(1)).record(argThat(entry ->
                entry.getEnvironment().equals("production") &&
                entry.getUser().equals("bob") &&
                entry.getAction().equals("PROMOTE")
        ));
    }

    @Test
    void promote_shouldThrowWhenSourceEnvIsNull() {
        assertThrows(NullPointerException.class,
                () -> service.promote(null, "production", "alice"));
    }

    @Test
    void promote_shouldThrowWhenPromotedByIsNull() {
        assertThrows(NullPointerException.class,
                () -> service.promote("staging", "production", null));
    }

    @Test
    void promote_shouldCountZeroChangesWhenConfigsIdentical() {
        Map<String, String> props = Map.of("db.url", "jdbc:postgres://host/db");
        EnvironmentConfig staging = new EnvironmentConfig("staging", props);
        EnvironmentConfig prod = new EnvironmentConfig("production", props);

        when(configLoader.load("staging")).thenReturn(staging);
        when(configLoader.load("production")).thenReturn(prod);

        PromotionResult result = service.promote("staging", "production", "carol");

        assertEquals(0, result.getAddedCount());
        assertEquals(0, result.getUpdatedCount());
    }
}
