package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigPatchManagerTest {

    private ConfigPatchManager patchManager;

    @BeforeEach
    void setUp() {
        patchManager = new ConfigPatchManager();
    }

    @Test
    void stagePatch_andApply_setsValue() {
        patchManager.stagePatch("prod", "db.url", "jdbc:new");
        Map<String, String> original = new HashMap<>();
        original.put("db.url", "jdbc:old");
        original.put("app.name", "jarvis");

        Map<String, String> result = patchManager.applyPatches("prod", original);

        assertEquals("jdbc:new", result.get("db.url"));
        assertEquals("jarvis", result.get("app.name"));
    }

    @Test
    void stageDeletion_andApply_removesKey() {
        patchManager.stageDeletion("staging", "feature.flag");
        Map<String, String> original = new HashMap<>();
        original.put("feature.flag", "true");
        original.put("timeout", "30");

        Map<String, String> result = patchManager.applyPatches("staging", original);

        assertFalse(result.containsKey("feature.flag"));
        assertEquals("30", result.get("timeout"));
    }

    @Test
    void applyPatches_noPatches_returnsOriginal() {
        Map<String, String> original = Map.of("key", "value");
        Map<String, String> result = patchManager.applyPatches("dev", original);
        assertEquals(original, result);
    }

    @Test
    void clearPatches_removesAllStagedPatches() {
        patchManager.stagePatch("prod", "key", "val");
        assertTrue(patchManager.hasPendingPatches("prod"));
        patchManager.clearPatches("prod");
        assertFalse(patchManager.hasPendingPatches("prod"));
    }

    @Test
    void getPendingPatches_returnsCorrectEntries() {
        patchManager.stagePatch("dev", "log.level", "DEBUG");
        patchManager.stageDeletion("dev", "unused.key");

        Map<String, ConfigPatchManager.PatchEntry> patches = patchManager.getPendingPatches("dev");

        assertEquals(2, patches.size());
        assertEquals(ConfigPatchManager.PatchOperation.SET, patches.get("log.level").operation());
        assertEquals(ConfigPatchManager.PatchOperation.DELETE, patches.get("unused.key").operation());
    }

    @Test
    void stagePatch_overwritesPreviousPatchForSameKey() {
        patchManager.stagePatch("prod", "db.pool", "5");
        patchManager.stagePatch("prod", "db.pool", "10");
        Map<String, String> result = patchManager.applyPatches("prod", new HashMap<>());
        assertEquals("10", result.get("db.pool"));
    }

    @Test
    void patchAwareConfigLoader_appliesPatchesToLoadedConfig() throws ConfigLoadException {
        ConfigLoader mockLoader = mock(ConfigLoader.class);
        Map<String, String> baseProps = new HashMap<>();
        baseProps.put("server.port", "8080");
        baseProps.put("debug", "false");
        EnvironmentConfig baseConfig = new EnvironmentConfig("prod", baseProps);
        when(mockLoader.load("prod")).thenReturn(baseConfig);

        patchManager.stagePatch("prod", "debug", "true");
        patchManager.stageDeletion("prod", "server.port");

        PatchAwareConfigLoader loader = new PatchAwareConfigLoader(mockLoader, patchManager);
        EnvironmentConfig result = loader.load("prod");

        assertEquals("true", result.getProperties().get("debug"));
        assertFalse(result.getProperties().containsKey("server.port"));
    }
}
