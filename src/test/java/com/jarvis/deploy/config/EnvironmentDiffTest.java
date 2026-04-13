package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentDiffTest {

    private EnvironmentConfig staging;
    private EnvironmentConfig production;

    @BeforeEach
    void setUp() {
        staging = new EnvironmentConfig("staging", Map.of(
                "db.host", "staging-db.internal",
                "db.port", "5432",
                "feature.flag", "true"
        ));

        production = new EnvironmentConfig("production", Map.of(
                "db.host", "prod-db.internal",
                "db.port", "5432",
                "cache.enabled", "true"
        ));
    }

    @Test
    void diffShouldDetectModifiedKey() {
        EnvironmentDiff diff = EnvironmentDiff.compute(staging, production);
        List<EnvironmentDiff.Change> modified = diff.getChanges().stream()
                .filter(c -> c.getType() == EnvironmentDiff.ChangeType.MODIFIED)
                .toList();

        assertEquals(1, modified.size());
        EnvironmentDiff.Change change = modified.get(0);
        assertEquals("db.host", change.getKey());
        assertEquals("staging-db.internal", change.getOldValue());
        assertEquals("prod-db.internal", change.getNewValue());
    }

    @Test
    void diffShouldDetectRemovedKey() {
        EnvironmentDiff diff = EnvironmentDiff.compute(staging, production);
        List<EnvironmentDiff.Change> removed = diff.getChanges().stream()
                .filter(c -> c.getType() == EnvironmentDiff.ChangeType.REMOVED)
                .toList();

        assertEquals(1, removed.size());
        assertEquals("feature.flag", removed.get(0).getKey());
        assertEquals("true", removed.get(0).getOldValue());
        assertNull(removed.get(0).getNewValue());
    }

    @Test
    void diffShouldDetectAddedKey() {
        EnvironmentDiff diff = EnvironmentDiff.compute(staging, production);
        List<EnvironmentDiff.Change> added = diff.getChanges().stream()
                .filter(c -> c.getType() == EnvironmentDiff.ChangeType.ADDED)
                .toList();

        assertEquals(1, added.size());
        assertEquals("cache.enabled", added.get(0).getKey());
        assertNull(added.get(0).getOldValue());
        assertEquals("true", added.get(0).getNewValue());
    }

    @Test
    void diffShouldBeEmptyForIdenticalConfigs() {
        EnvironmentConfig copy = new EnvironmentConfig("staging-copy", Map.of(
                "db.host", "staging-db.internal",
                "db.port", "5432",
                "feature.flag", "true"
        ));
        EnvironmentDiff diff = EnvironmentDiff.compute(staging, copy);
        assertTrue(diff.isEmpty());
    }

    @Test
    void changesToStringShouldContainKey() {
        EnvironmentDiff diff = EnvironmentDiff.compute(staging, production);
        diff.getChanges().forEach(c -> assertTrue(c.toString().contains(c.getKey())));
    }
}
