package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigRollbackServiceTest {

    private ConfigSnapshotService snapshotService;
    private ConfigVersionRegistry versionRegistry;
    private ConfigRollbackService rollbackService;

    private EnvironmentConfig sampleConfig;
    private ConfigSnapshot sampleSnapshot;

    @BeforeEach
    void setUp() {
        snapshotService = mock(ConfigSnapshotService.class);
        versionRegistry = mock(ConfigVersionRegistry.class);
        rollbackService = new ConfigRollbackService(snapshotService, versionRegistry);

        sampleConfig = new EnvironmentConfig("staging", Map.of("db.host", "localhost", "db.port", "5432"));
        sampleSnapshot = new ConfigSnapshot("snap-001", "staging", sampleConfig);
    }

    @Test
    void rollback_restoresConfigFromSnapshot() {
        when(snapshotService.listSnapshots("staging")).thenReturn(List.of(sampleSnapshot));

        EnvironmentConfig result = rollbackService.rollback("staging", "snap-001");

        assertNotNull(result);
        assertEquals("staging", result.getEnvironment());
        assertEquals("localhost", result.getProperties().get("db.host"));
        verify(versionRegistry, times(1)).register("staging", result);
    }

    @Test
    void rollback_throwsWhenSnapshotNotFound() {
        when(snapshotService.listSnapshots("staging")).thenReturn(List.of(sampleSnapshot));

        RollbackException ex = assertThrows(RollbackException.class,
                () -> rollbackService.rollback("staging", "snap-999"));
        assertTrue(ex.getMessage().contains("snap-999"));
    }

    @Test
    void rollback_throwsOnBlankEnvironment() {
        assertThrows(IllegalArgumentException.class,
                () -> rollbackService.rollback("", "snap-001"));
    }

    @Test
    void rollback_throwsOnBlankSnapshotId() {
        assertThrows(IllegalArgumentException.class,
                () -> rollbackService.rollback("staging", "  "));
    }

    @Test
    void rollbackToLatest_usesLastSnapshot() {
        ConfigSnapshot older = new ConfigSnapshot("snap-000", "staging",
                new EnvironmentConfig("staging", Map.of("db.host", "old-host")));
        when(snapshotService.listSnapshots("staging")).thenReturn(List.of(older, sampleSnapshot));

        EnvironmentConfig result = rollbackService.rollbackToLatest("staging");

        assertEquals("localhost", result.getProperties().get("db.host"));
        verify(versionRegistry).register("staging", result);
    }

    @Test
    void rollbackToLatest_throwsWhenNoSnapshots() {
        when(snapshotService.listSnapshots("prod")).thenReturn(List.of());

        RollbackException ex = assertThrows(RollbackException.class,
                () -> rollbackService.rollbackToLatest("prod"));
        assertTrue(ex.getMessage().contains("prod"));
    }

    @Test
    void rollback_doesNotRegisterWhenSnapshotNotFound() {
        when(snapshotService.listSnapshots("staging")).thenReturn(List.of(sampleSnapshot));

        assertThrows(RollbackException.class,
                () -> rollbackService.rollback("staging", "snap-999"));

        verify(versionRegistry, never()).register(anyString(), any());
    }
}
