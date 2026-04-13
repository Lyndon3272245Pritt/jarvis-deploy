package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigProfileManagerTest {

    private ConfigProfileManager manager;
    private EnvironmentConfig devConfig;
    private EnvironmentConfig prodConfig;

    @BeforeEach
    void setUp() {
        manager = new ConfigProfileManager();
        devConfig = new EnvironmentConfig("dev", Map.of("db.url", "jdbc:h2:mem:dev", "log.level", "DEBUG"));
        prodConfig = new EnvironmentConfig("prod", Map.of("db.url", "jdbc:postgresql://prod/db", "log.level", "WARN"));
    }

    @Test
    void registerAndRetrieveProfile() {
        manager.registerProfile("dev", devConfig);
        Optional<EnvironmentConfig> result = manager.getProfile("dev");
        assertTrue(result.isPresent());
        assertEquals(devConfig, result.get());
    }

    @Test
    void registerProfileWithNullNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.registerProfile(null, devConfig));
    }

    @Test
    void registerProfileWithBlankNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.registerProfile("  ", devConfig));
    }

    @Test
    void registerProfileWithNullConfigThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.registerProfile("dev", null));
    }

    @Test
    void activateKnownProfileSetsActiveConfig() {
        manager.registerProfile("prod", prodConfig);
        manager.activateProfile("prod");
        assertTrue(manager.getActiveConfig().isPresent());
        assertEquals(prodConfig, manager.getActiveConfig().get());
        assertEquals("prod", manager.getActiveProfileName().orElse(null));
    }

    @Test
    void activateUnknownProfileThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.activateProfile("unknown"));
    }

    @Test
    void getActiveConfigReturnsEmptyWhenNoProfileActivated() {
        manager.registerProfile("dev", devConfig);
        assertTrue(manager.getActiveConfig().isEmpty());
        assertTrue(manager.getActiveProfileName().isEmpty());
    }

    @Test
    void getRegisteredProfilesReturnsAllKeys() {
        manager.registerProfile("dev", devConfig);
        manager.registerProfile("prod", prodConfig);
        assertTrue(manager.getRegisteredProfiles().contains("dev"));
        assertTrue(manager.getRegisteredProfiles().contains("prod"));
        assertEquals(2, manager.getRegisteredProfiles().size());
    }

    @Test
    void removeProfileClearsActiveIfMatching() {
        manager.registerProfile("dev", devConfig);
        manager.activateProfile("dev");
        manager.removeProfile("dev");
        assertTrue(manager.getActiveConfig().isEmpty());
        assertTrue(manager.getActiveProfileName().isEmpty());
        assertFalse(manager.getRegisteredProfiles().contains("dev"));
    }

    @Test
    void removeNonActiveProfileLeavesActiveIntact() {
        manager.registerProfile("dev", devConfig);
        manager.registerProfile("prod", prodConfig);
        manager.activateProfile("dev");
        manager.removeProfile("prod");
        assertEquals("dev", manager.getActiveProfileName().orElse(null));
        assertTrue(manager.getActiveConfig().isPresent());
    }
}
