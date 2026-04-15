package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigAliasManagerTest {

    private ConfigLoader configLoader;
    private ConfigAliasManager manager;

    @BeforeEach
    void setUp() {
        configLoader = mock(ConfigLoader.class);
        manager = new ConfigAliasManager(configLoader);
    }

    @Test
    void registerAndResolveAlias() throws ConfigLoadException {
        EnvironmentConfig config = new EnvironmentConfig("prod", Map.of("db.url", "jdbc:prod"));
        when(configLoader.load("prod")).thenReturn(config);

        manager.registerAlias("prod-db", "prod", "db.url", "admin");

        Optional<String> value = manager.resolve("prod-db");
        assertTrue(value.isPresent());
        assertEquals("jdbc:prod", value.get());
    }

    @Test
    void resolveReturnsEmptyForUnknownAlias() {
        Optional<String> value = manager.resolve("nonexistent");
        assertFalse(value.isPresent());
    }

    @Test
    void resolveReturnsEmptyWhenKeyMissing() throws ConfigLoadException {
        EnvironmentConfig config = new EnvironmentConfig("staging", Map.of());
        when(configLoader.load("staging")).thenReturn(config);

        manager.registerAlias("stg-key", "staging", "missing.key", "dev");
        Optional<String> value = manager.resolve("stg-key");
        assertFalse(value.isPresent());
    }

    @Test
    void registerDuplicateAliasThrows() {
        manager.registerAlias("my-alias", "dev", "some.key", "user");
        assertThrows(IllegalStateException.class, () ->
            manager.registerAlias("my-alias", "prod", "other.key", "user")
        );
    }

    @Test
    void removeAliasReturnsTrueIfExisted() {
        manager.registerAlias("temp", "dev", "key", "user");
        assertTrue(manager.removeAlias("temp"));
        assertFalse(manager.removeAlias("temp"));
    }

    @Test
    void listAliasesForEnvironmentFiltersCorrectly() {
        manager.registerAlias("alias-a", "prod", "key.a", "user");
        manager.registerAlias("alias-b", "dev", "key.b", "user");
        manager.registerAlias("alias-c", "prod", "key.c", "user");

        List<ConfigAlias> prodAliases = manager.listAliasesForEnvironment("prod");
        assertEquals(2, prodAliases.size());
        assertTrue(prodAliases.stream().allMatch(a -> a.getTargetEnvironment().equals("prod")));
    }

    @Test
    void getAliasReturnsMetadata() {
        manager.registerAlias("meta-alias", "qa", "app.port", "tester");
        Optional<ConfigAlias> alias = manager.getAlias("meta-alias");
        assertTrue(alias.isPresent());
        assertEquals("qa", alias.get().getTargetEnvironment());
        assertEquals("app.port", alias.get().getTargetKey());
        assertEquals("tester", alias.get().getCreatedBy());
    }

    @Test
    void resolveReturnsEmptyOnConfigLoadException() throws ConfigLoadException {
        when(configLoader.load("broken")).thenThrow(new ConfigLoadException("fail"));
        manager.registerAlias("broken-alias", "broken", "key", "user");
        assertFalse(manager.resolve("broken-alias").isPresent());
    }
}
