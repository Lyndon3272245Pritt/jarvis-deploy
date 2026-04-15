package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLineageTrackerTest {

    private ConfigLineageTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new ConfigLineageTracker();
    }

    @Test
    void recordAndRetrieveLineageForKey() {
        tracker.record("db.url", "base", "staging", ConfigLineageTracker.OperationType.PROMOTED, "alice");
        tracker.record("db.url", "staging", "prod", ConfigLineageTracker.OperationType.PROMOTED, "bob");
        tracker.record("app.port", "base", "staging", ConfigLineageTracker.OperationType.LOADED, "system");

        List<ConfigLineageTracker.LineageEntry> lineage = tracker.getLineageForKey("db.url");
        assertEquals(2, lineage.size());
        assertEquals("alice", lineage.get(0).getActor());
        assertEquals("bob", lineage.get(1).getActor());
    }

    @Test
    void getLineageForEnvironmentIncludesSourceAndTarget() {
        tracker.record("db.url", "base", "staging", ConfigLineageTracker.OperationType.MERGED, "alice");
        tracker.record("app.name", "staging", "prod", ConfigLineageTracker.OperationType.PROMOTED, "bob");
        tracker.record("feature.flag", "base", "dev", ConfigLineageTracker.OperationType.LOADED, "system");

        List<ConfigLineageTracker.LineageEntry> stagingEntries = tracker.getLineageForEnvironment("staging");
        assertEquals(2, stagingEntries.size());
    }

    @Test
    void recordUsesDefaultActorWhenNull() {
        tracker.record("key", null, "dev", ConfigLineageTracker.OperationType.LOADED, null);
        List<ConfigLineageTracker.LineageEntry> entries = tracker.getAllEntries();
        assertEquals(1, entries.size());
        assertEquals("system", entries.get(0).getActor());
    }

    @Test
    void recordSetsTimestampAutomatically() {
        tracker.record("key", "src", "dest", ConfigLineageTracker.OperationType.PATCHED, "user");
        assertNotNull(tracker.getAllEntries().get(0).getTimestamp());
    }

    @Test
    void getLineageForKeyReturnsEmptyWhenNoMatch() {
        tracker.record("other.key", "base", "dev", ConfigLineageTracker.OperationType.LOADED, "system");
        List<ConfigLineageTracker.LineageEntry> result = tracker.getLineageForKey("db.url");
        assertTrue(result.isEmpty());
    }

    @Test
    void clearRemovesAllEntries() {
        tracker.record("db.url", "base", "staging", ConfigLineageTracker.OperationType.PROMOTED, "alice");
        tracker.record("app.port", "base", "dev", ConfigLineageTracker.OperationType.LOADED, "system");
        tracker.clear();
        assertTrue(tracker.getAllEntries().isEmpty());
    }

    @Test
    void getAllEntriesIsUnmodifiable() {
        tracker.record("key", "src", "dest", ConfigLineageTracker.OperationType.ROLLED_BACK, "admin");
        List<ConfigLineageTracker.LineageEntry> entries = tracker.getAllEntries();
        assertThrows(UnsupportedOperationException.class, () -> entries.remove(0));
    }

    @Test
    void recordNullKeyThrowsException() {
        assertThrows(NullPointerException.class, () ->
                tracker.record(null, "src", "dest", ConfigLineageTracker.OperationType.LOADED, "user"));
    }

    @Test
    void toStringContainsRelevantInfo() {
        tracker.record("db.host", "base", "prod", ConfigLineageTracker.OperationType.IMPORTED, "ci-bot");
        String str = tracker.getAllEntries().get(0).toString();
        assertTrue(str.contains("db.host"));
        assertTrue(str.contains("IMPORTED"));
        assertTrue(str.contains("ci-bot"));
    }
}
