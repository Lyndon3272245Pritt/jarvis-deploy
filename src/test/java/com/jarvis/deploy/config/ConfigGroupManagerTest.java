package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigGroupManagerTest {

    private ConfigGroupManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConfigGroupManager();
    }

    @Test
    void createGroup_storesEmptyGroup() {
        manager.createGroup("database");
        assertTrue(manager.groupExists("database"));
        assertTrue(manager.getKeysInGroup("database").isEmpty());
    }

    @Test
    void createGroup_blankName_throws() {
        assertThrows(IllegalArgumentException.class, () -> manager.createGroup("  "));
    }

    @Test
    void addKeyToGroup_associatesKey() {
        manager.createGroup("network");
        manager.addKeyToGroup("network", "host");
        manager.addKeyToGroup("network", "port");

        Set<String> keys = manager.getKeysInGroup("network");
        assertTrue(keys.contains("host"));
        assertTrue(keys.contains("port"));
        assertEquals(Optional.of("network"), manager.getGroupForKey("host"));
    }

    @Test
    void addKeyToGroup_unknownGroup_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.addKeyToGroup("ghost", "key"));
    }

    @Test
    void removeKeyFromGroup_removesAssociation() {
        manager.createGroup("cache");
        manager.addKeyToGroup("cache", "ttl");
        manager.removeKeyFromGroup("cache", "ttl");

        assertFalse(manager.getKeysInGroup("cache").contains("ttl"));
        assertTrue(manager.getGroupForKey("ttl").isEmpty());
    }

    @Test
    void deleteGroup_removesGroupAndKeys() {
        manager.createGroup("temp");
        manager.addKeyToGroup("temp", "x");
        manager.deleteGroup("temp");

        assertFalse(manager.groupExists("temp"));
        assertTrue(manager.getGroupForKey("x").isEmpty());
    }

    @Test
    void filterByGroup_returnsOnlyGroupKeys() {
        manager.createGroup("db");
        manager.addKeyToGroup("db", "db.url");
        manager.addKeyToGroup("db", "db.user");

        Map<String, String> config = Map.of(
                "db.url", "jdbc:h2:mem",
                "db.user", "admin",
                "app.name", "jarvis"
        );
        Map<String, String> filtered = manager.filterByGroup("db", config);

        assertEquals(2, filtered.size());
        assertTrue(filtered.containsKey("db.url"));
        assertFalse(filtered.containsKey("app.name"));
    }

    @Test
    void snapshot_isImmutable() {
        manager.createGroup("g1");
        Map<String, ?> snap = manager.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snap.put("g2", Set.of()));
    }
}