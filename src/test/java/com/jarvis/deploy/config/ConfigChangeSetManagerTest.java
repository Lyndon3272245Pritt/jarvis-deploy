package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigChangeSetManagerTest {

    private ConfigChangeSetManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConfigChangeSetManager();
    }

    @Test
    void createChangeSet_shouldStoreAndReturnChangeSet() {
        ConfigChangeSetManager.ChangeSet cs = manager.createChangeSet("cs-1", "staging", "alice");
        assertNotNull(cs);
        assertEquals("cs-1", cs.getId());
        assertEquals("staging", cs.getEnvironment());
        assertEquals("alice", cs.getAuthor());
        assertFalse(cs.isApplied());
    }

    @Test
    void createChangeSet_duplicateId_shouldThrow() {
        manager.createChangeSet("cs-dup", "prod", "bob");
        assertThrows(IllegalArgumentException.class,
                () -> manager.createChangeSet("cs-dup", "prod", "bob"));
    }

    @Test
    void addChange_shouldAccumulateChanges() {
        manager.createChangeSet("cs-2", "dev", "carol");
        manager.addChange("cs-2", "db.host", "localhost");
        manager.addChange("cs-2", "db.port", "5432");

        Map<String, String> changes = manager.findChangeSet("cs-2").get().getChanges();
        assertEquals(2, changes.size());
        assertEquals("localhost", changes.get("db.host"));
        assertEquals("5432", changes.get("db.port"));
    }

    @Test
    void applyChangeSet_shouldMergeIntoBaseConfig() {
        Map<String, String> baseProps = Map.of("app.name", "jarvis", "db.host", "old-host");
        EnvironmentConfig base = new EnvironmentConfig("staging", baseProps);

        manager.createChangeSet("cs-3", "staging", "dave");
        manager.addChange("cs-3", "db.host", "new-host");
        manager.addChange("cs-3", "cache.ttl", "300");

        EnvironmentConfig result = manager.applyChangeSet("cs-3", base);

        assertEquals("new-host", result.getProperties().get("db.host"));
        assertEquals("jarvis", result.getProperties().get("app.name"));
        assertEquals("300", result.getProperties().get("cache.ttl"));
        assertTrue(manager.findChangeSet("cs-3").get().isApplied());
    }

    @Test
    void applyChangeSet_alreadyApplied_shouldThrow() {
        EnvironmentConfig base = new EnvironmentConfig("dev", Map.of());
        manager.createChangeSet("cs-4", "dev", "eve");
        manager.applyChangeSet("cs-4", base);

        assertThrows(IllegalStateException.class,
                () -> manager.applyChangeSet("cs-4", base));
    }

    @Test
    void addChange_afterApplied_shouldThrow() {
        EnvironmentConfig base = new EnvironmentConfig("dev", Map.of());
        manager.createChangeSet("cs-5", "dev", "frank");
        manager.applyChangeSet("cs-5", base);

        assertThrows(IllegalStateException.class,
                () -> manager.addChange("cs-5", "key", "value"));
    }

    @Test
    void listChangeSets_shouldReturnOnlyMatchingEnvironment() {
        manager.createChangeSet("cs-a", "prod", "alice");
        manager.createChangeSet("cs-b", "staging", "bob");
        manager.createChangeSet("cs-c", "prod", "carol");

        List<ConfigChangeSetManager.ChangeSet> prodSets = manager.listChangeSets("prod");
        assertEquals(2, prodSets.size());
        assertTrue(prodSets.stream().allMatch(cs -> cs.getEnvironment().equals("prod")));
    }

    @Test
    void findChangeSet_unknownId_shouldReturnEmpty() {
        Optional<ConfigChangeSetManager.ChangeSet> result = manager.findChangeSet("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void addChange_unknownChangeSetId_shouldThrow() {
        assertThrows(NoSuchElementException.class,
                () -> manager.addChange("ghost", "key", "val"));
    }
}
