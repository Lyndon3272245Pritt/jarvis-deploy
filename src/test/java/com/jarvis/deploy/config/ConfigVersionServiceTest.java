package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigVersionServiceTest {

    private ConfigVersionRegistry registry;
    private EnvironmentDiff environmentDiff;
    private ConfigVersionService service;

    private EnvironmentConfig configV1;
    private EnvironmentConfig configV2;

    @BeforeEach
    void setUp() {
        registry = new ConfigVersionRegistry();
        environmentDiff = new EnvironmentDiff();
        service = new ConfigVersionService(registry, environmentDiff);

        configV1 = new EnvironmentConfig("staging", Map.of(
                "db.host", "db-old.internal",
                "app.port", "8080"
        ));
        configV2 = new EnvironmentConfig("staging", Map.of(
                "db.host", "db-new.internal",
                "app.port", "8080",
                "feature.flag", "true"
        ));
    }

    @Test
    void saveVersion_assignsIncrementalVersionNumbers() {
        int v1 = service.saveVersion("staging", configV1);
        int v2 = service.saveVersion("staging", configV2);

        assertEquals(1, v1);
        assertEquals(2, v2);
    }

    @Test
    void rollback_returnsCorrectConfig() {
        service.saveVersion("staging", configV1);
        service.saveVersion("staging", configV2);

        EnvironmentConfig rolled = service.rollback("staging", 1);
        assertEquals("db-old.internal", rolled.getProperties().get("db.host"));
    }

    @Test
    void rollback_throwsWhenVersionMissing() {
        service.saveVersion("staging", configV1);

        assertThrows(ConfigVersionException.class, () -> service.rollback("staging", 99));
    }

    @Test
    void diffVersions_returnsChangedKeys() {
        service.saveVersion("staging", configV1);
        service.saveVersion("staging", configV2);

        Map<String, String[]> diff = service.diffVersions("staging", 1, 2);

        assertTrue(diff.containsKey("db.host"));
        assertFalse(diff.containsKey("app.port"));
    }

    @Test
    void getHistory_returnsAllVersionsInOrder() {
        service.saveVersion("staging", configV1);
        service.saveVersion("staging", configV2);

        List<ConfigVersionRegistry.VersionEntry> history = service.getHistory("staging");

        assertEquals(2, history.size());
        assertEquals(1, history.get(0).version());
        assertEquals(2, history.get(1).version());
    }

    @Test
    void getLatest_returnsNewestEntry() {
        service.saveVersion("staging", configV1);
        service.saveVersion("staging", configV2);

        Optional<ConfigVersionRegistry.VersionEntry> latest = service.getLatest("staging");

        assertTrue(latest.isPresent());
        assertEquals(2, latest.get().version());
        assertEquals("db-new.internal", latest.get().config().getProperties().get("db.host"));
    }

    @Test
    void getLatest_emptyWhenNoVersionsRegistered() {
        Optional<ConfigVersionRegistry.VersionEntry> latest = service.getLatest("prod");
        assertTrue(latest.isEmpty());
    }
}
