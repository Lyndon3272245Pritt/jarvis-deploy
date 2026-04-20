package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMetadataStoreTest {

    private ConfigMetadataStore store;

    @BeforeEach
    void setUp() {
        store = new ConfigMetadataStore();
    }

    @Test
    void putAndGetMetadata_returnsCorrectEntry() {
        store.put("production", "owner", "team-platform", "alice");
        Optional<ConfigMetadataStore.MetadataEntry> entry = store.get("production", "owner");
        assertTrue(entry.isPresent());
        assertEquals("owner", entry.get().getKey());
        assertEquals("team-platform", entry.get().getValue());
        assertEquals("alice", entry.get().getAuthor());
        assertNotNull(entry.get().getTimestamp());
    }

    @Test
    void get_missingEnvironment_returnsEmpty() {
        assertTrue(store.get("staging", "owner").isEmpty());
    }

    @Test
    void get_missingKey_returnsEmpty() {
        store.put("staging", "tier", "gold", "bob");
        assertTrue(store.get("staging", "nonexistent").isEmpty());
    }

    @Test
    void getAllForEnvironment_returnsAllEntries() {
        store.put("dev", "owner", "team-dev", "carol");
        store.put("dev", "tier", "bronze", "carol");
        Map<String, ConfigMetadataStore.MetadataEntry> all = store.getAllForEnvironment("dev");
        assertEquals(2, all.size());
        assertTrue(all.containsKey("owner"));
        assertTrue(all.containsKey("tier"));
    }

    @Test
    void getAllForEnvironment_unknownEnv_returnsEmptyMap() {
        assertTrue(store.getAllForEnvironment("unknown").isEmpty());
    }

    @Test
    void remove_existingKey_returnsTrueAndRemoves() {
        store.put("prod", "region", "us-east-1", "dave");
        assertTrue(store.remove("prod", "region"));
        assertTrue(store.get("prod", "region").isEmpty());
    }

    @Test
    void remove_missingKey_returnsFalse() {
        assertFalse(store.remove("prod", "nonexistent"));
    }

    @Test
    void clearEnvironment_removesAllEntries() {
        store.put("qa", "owner", "team-qa", "eve");
        store.put("qa", "tier", "silver", "eve");
        store.clearEnvironment("qa");
        assertEquals(0, store.size("qa"));
        assertTrue(store.getAllForEnvironment("qa").isEmpty());
    }

    @Test
    void hasMetadata_existingKey_returnsTrue() {
        store.put("prod", "sla", "99.9", "frank");
        assertTrue(store.hasMetadata("prod", "sla"));
    }

    @Test
    void hasMetadata_missingKey_returnsFalse() {
        assertFalse(store.hasMetadata("prod", "sla"));
    }

    @Test
    void put_blankEnvironment_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> store.put(" ", "key", "value", "author"));
    }

    @Test
    void put_blankKey_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> store.put("prod", "", "value", "author"));
    }

    @Test
    void put_overwritesExistingKey() {
        store.put("prod", "owner", "team-a", "alice");
        store.put("prod", "owner", "team-b", "bob");
        Optional<ConfigMetadataStore.MetadataEntry> entry = store.get("prod", "owner");
        assertTrue(entry.isPresent());
        assertEquals("team-b", entry.get().getValue());
        assertEquals("bob", entry.get().getAuthor());
    }
}
