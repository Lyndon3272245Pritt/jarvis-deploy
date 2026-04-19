package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigRetentionServiceTest {

    private ConfigVersionRegistry registry;
    private ConfigTagManager tagManager;
    private ConfigRetentionService service;

    @BeforeEach
    void setUp() {
        registry = mock(ConfigVersionRegistry.class);
        tagManager = mock(ConfigTagManager.class);
        service = new ConfigRetentionService(registry, tagManager);
    }

    @Test
    void applyPolicy_prunesOldVersions() {
        ConfigRetentionPolicy policy = new ConfigRetentionPolicy("staging", 10, Duration.ofDays(30), false);
        service.registerPolicy(policy);

        String oldVersion = "v1";
        String newVersion = "v2";
        when(registry.listVersions("staging")).thenReturn(Arrays.asList(oldVersion, newVersion));
        when(registry.getVersionTimestamp("staging", oldVersion)).thenReturn(Instant.now().minus(Duration.ofDays(60)));
        when(registry.getVersionTimestamp("staging", newVersion)).thenReturn(Instant.now());
        when(tagManager.getTaggedVersions("staging")).thenReturn(Collections.emptyList());

        List<String> pruned = service.applyPolicy("staging");

        assertTrue(pruned.contains(oldVersion));
        assertFalse(pruned.contains(newVersion));
        verify(registry).deleteVersion("staging", oldVersion);
        verify(registry, never()).deleteVersion("staging", newVersion);
    }

    @Test
    void applyPolicy_keepsTaggedVersions() {
        ConfigRetentionPolicy policy = new ConfigRetentionPolicy("prod", 10, Duration.ofDays(1), true);
        service.registerPolicy(policy);

        String taggedOld = "v1";
        when(registry.listVersions("prod")).thenReturn(Collections.singletonList(taggedOld));
        when(registry.getVersionTimestamp("prod", taggedOld)).thenReturn(Instant.now().minus(Duration.ofDays(90)));
        when(tagManager.getTaggedVersions("prod")).thenReturn(Collections.singletonList(taggedOld));

        List<String> pruned = service.applyPolicy("prod");

        assertFalse(pruned.contains(taggedOld));
        verify(registry, never()).deleteVersion(eq("prod"), any());
    }

    @Test
    void applyPolicy_returnsEmpty_whenNoPolicyRegistered() {
        List<String> pruned = service.applyPolicy("dev");
        assertTrue(pruned.isEmpty());
    }

    @Test
    void getPolicy_returnsRegisteredPolicy() {
        ConfigRetentionPolicy policy = new ConfigRetentionPolicy("dev", 5, Duration.ofDays(7), false);
        service.registerPolicy(policy);
        assertTrue(service.getPolicy("dev").isPresent());
        assertEquals(5, service.getPolicy("dev").get().getMaxVersions());
    }

    @Test
    void constructor_rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new ConfigRetentionService(null, tagManager));
        assertThrows(NullPointerException.class, () -> new ConfigRetentionService(registry, null));
    }
}
