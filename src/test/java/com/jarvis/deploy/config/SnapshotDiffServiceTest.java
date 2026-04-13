package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotDiffServiceTest {

    private SnapshotDiffService snapshotDiffService;

    @BeforeEach
    void setUp() {
        snapshotDiffService = new SnapshotDiffService(new EnvironmentDiff());
    }

    private ConfigSnapshot makeSnapshot(String env, Map<String, String> props) {
        return new ConfigSnapshot(env, props, Instant.now(), java.util.UUID.randomUUID().toString());
    }

    @Test
    void diff_shouldDetectAddedKey() {
        Map<String, String> baseProps = new HashMap<>();
        baseProps.put("db.url", "jdbc:old");

        Map<String, String> targetProps = new HashMap<>();
        targetProps.put("db.url", "jdbc:old");
        targetProps.put("cache.enabled", "true");

        ConfigSnapshot base = makeSnapshot("prod", baseProps);
        ConfigSnapshot target = makeSnapshot("prod", targetProps);

        EnvironmentDiff.DiffResult result = snapshotDiffService.diff(base, target);

        assertTrue(result.getAdded().containsKey("cache.enabled"));
        assertTrue(result.getRemoved().isEmpty());
        assertTrue(result.getModified().isEmpty());
    }

    @Test
    void diff_shouldDetectModifiedKey() {
        Map<String, String> baseProps = new HashMap<>();
        baseProps.put("db.url", "jdbc:old");

        Map<String, String> targetProps = new HashMap<>();
        targetProps.put("db.url", "jdbc:new");

        ConfigSnapshot base = makeSnapshot("prod", baseProps);
        ConfigSnapshot target = makeSnapshot("prod", targetProps);

        EnvironmentDiff.DiffResult result = snapshotDiffService.diff(base, target);

        assertTrue(result.getModified().containsKey("db.url"));
    }

    @Test
    void diff_shouldDetectRemovedKey() {
        Map<String, String> baseProps = new HashMap<>();
        baseProps.put("db.url", "jdbc:old");
        baseProps.put("legacy.flag", "true");

        Map<String, String> targetProps = new HashMap<>();
        targetProps.put("db.url", "jdbc:old");

        ConfigSnapshot base = makeSnapshot("prod", baseProps);
        ConfigSnapshot target = makeSnapshot("prod", targetProps);

        EnvironmentDiff.DiffResult result = snapshotDiffService.diff(base, target);

        assertTrue(result.getRemoved().containsKey("legacy.flag"));
    }

    @Test
    void diff_shouldThrowWhenBaselineIsNull() {
        Map<String, String> props = new HashMap<>();
        ConfigSnapshot target = makeSnapshot("prod", props);
        assertThrows(IllegalArgumentException.class, () -> snapshotDiffService.diff(null, target));
    }

    @Test
    void diff_shouldThrowWhenTargetIsNull() {
        Map<String, String> props = new HashMap<>();
        ConfigSnapshot base = makeSnapshot("prod", props);
        assertThrows(IllegalArgumentException.class, () -> snapshotDiffService.diff(base, null));
    }
}
