package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigPinManagerTest {

    private ConfigPinManager pinManager;

    @BeforeEach
    void setUp() {
        pinManager = new ConfigPinManager();
    }

    @Test
    void testPinAndIsPinned() {
        pinManager.pin("prod", "db.host", "prod-db.internal", "admin");
        assertTrue(pinManager.isPinned("prod", "db.host"));
        assertFalse(pinManager.isPinned("prod", "db.port"));
        assertFalse(pinManager.isPinned("staging", "db.host"));
    }

    @Test
    void testGetPin() {
        pinManager.pin("prod", "db.host", "prod-db.internal", "admin");
        Optional<ConfigPinManager.PinnedEntry> entry = pinManager.getPin("prod", "db.host");
        assertTrue(entry.isPresent());
        assertEquals("prod-db.internal", entry.get().getValue());
        assertEquals("admin", entry.get().getPinnedBy());
        assertNotNull(entry.get().getPinnedAt());
    }

    @Test
    void testUnpin() {
        pinManager.pin("prod", "db.host", "prod-db.internal", "admin");
        pinManager.unpin("prod", "db.host");
        assertFalse(pinManager.isPinned("prod", "db.host"));
    }

    @Test
    void testApplyPinsOverridesValues() {
        pinManager.pin("prod", "db.host", "pinned-host", "ops");
        Map<String, String> config = new HashMap<>();
        config.put("db.host", "original-host");
        config.put("app.port", "8080");

        Map<String, String> result = pinManager.applyPins("prod", config);
        assertEquals("pinned-host", result.get("db.host"));
        assertEquals("8080", result.get("app.port"));
    }

    @Test
    void testApplyPinsAddsNewPinnedKeys() {
        pinManager.pin("prod", "feature.flag", "true", "ci");
        Map<String, String> config = new HashMap<>();
        config.put("app.port", "8080");

        Map<String, String> result = pinManager.applyPins("prod", config);
        assertEquals("true", result.get("feature.flag"));
        assertEquals("8080", result.get("app.port"));
    }

    @Test
    void testGetPinsForEnvironment() {
        pinManager.pin("prod", "key1", "val1", "user1");
        pinManager.pin("prod", "key2", "val2", "user2");
        pinManager.pin("staging", "key1", "stagingVal", "user3");

        Map<String, ConfigPinManager.PinnedEntry> prodPins = pinManager.getPinsForEnvironment("prod");
        assertEquals(2, prodPins.size());
        assertTrue(prodPins.containsKey("key1"));
        assertTrue(prodPins.containsKey("key2"));
    }

    @Test
    void testClearAll() {
        pinManager.pin("prod", "key1", "val1", "user");
        pinManager.clearAll("prod");
        assertFalse(pinManager.isPinned("prod", "key1"));
        assertTrue(pinManager.getPinsForEnvironment("prod").isEmpty());
    }

    @Test
    void testPinNullArgumentsThrows() {
        assertThrows(IllegalArgumentException.class, () -> pinManager.pin(null, "key", "val", "user"));
        assertThrows(IllegalArgumentException.class, () -> pinManager.pin("prod", null, "val", "user"));
        assertThrows(IllegalArgumentException.class, () -> pinManager.pin("prod", "key", null, "user"));
    }
}
