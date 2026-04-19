package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigEnvironmentClonerTest {

    private ConfigLoader mockLoader;
    private ConfigEnvironmentCloner cloner;

    @BeforeEach
    void setUp() {
        mockLoader = mock(ConfigLoader.class);
        cloner = new ConfigEnvironmentCloner(mockLoader);
    }

    @Test
    void clone_copiesAllProperties() {
        EnvironmentConfig source = new EnvironmentConfig("staging",
                Map.of("db.host", "staging-db", "db.port", "5432"));
        EnvironmentConfig result = cloner.clone(source, "staging-copy", null);
        assertEquals("staging-copy", result.getName());
        assertEquals("staging-db", result.getProperties().get("db.host"));
        assertEquals("5432", result.getProperties().get("db.port"));
    }

    @Test
    void clone_appliesOverrides() {
        EnvironmentConfig source = new EnvironmentConfig("staging",
                Map.of("db.host", "staging-db", "db.port", "5432"));
        EnvironmentConfig result = cloner.clone(source, "prod", Map.of("db.host", "prod-db"));
        assertEquals("prod-db", result.getProperties().get("db.host"));
        assertEquals("5432", result.getProperties().get("db.port"));
    }

    @Test
    void clone_throwsOnNullSource() {
        assertThrows(IllegalArgumentException.class,
                () -> cloner.clone(null, "target", null));
    }

    @Test
    void clone_throwsOnBlankTargetName() {
        EnvironmentConfig source = new EnvironmentConfig("staging", Map.of());
        assertThrows(IllegalArgumentException.class,
                () -> cloner.clone(source, "  ", null));
    }

    @Test
    void cloneByName_loadsSourceAndClones() throws ConfigLoadException {
        EnvironmentConfig source = new EnvironmentConfig("staging",
                Map.of("key", "value"));
        when(mockLoader.load("staging")).thenReturn(source);
        EnvironmentConfig result = cloner.cloneByName("staging", "staging-v2", Map.of("key", "new-value"));
        assertEquals("staging-v2", result.getName());
        assertEquals("new-value", result.getProperties().get("key"));
        verify(mockLoader).load("staging");
    }

    @Test
    void cloneByName_propagatesLoadException() throws ConfigLoadException {
        when(mockLoader.load("missing")).thenThrow(new ConfigLoadException("not found"));
        assertThrows(ConfigLoadException.class,
                () -> cloner.cloneByName("missing", "copy", null));
    }

    @Test
    void constructor_throwsOnNullLoader() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConfigEnvironmentCloner(null));
    }
}
