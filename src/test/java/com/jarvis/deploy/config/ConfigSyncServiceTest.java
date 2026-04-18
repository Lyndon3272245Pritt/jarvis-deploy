package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigSyncServiceTest {

    private EnvironmentDiff environmentDiff;
    private ConfigAuditLog auditLog;
    private ConfigSyncService syncService;

    @BeforeEach
    void setUp() {
        environmentDiff = mock(EnvironmentDiff.class);
        auditLog = mock(ConfigAuditLog.class);
        syncService = new ConfigSyncService(environmentDiff, auditLog);
    }

    private EnvironmentConfig config(String env, Map<String, String> props) {
        EnvironmentConfig c = new EnvironmentConfig(env);
        c.setProperties(new LinkedHashMap<>(props));
        return c;
    }

    @Test
    void sync_addsNewKeysFromSource() {
        EnvironmentConfig source = config("prod", Map.of("db.url", "jdbc:prod", "db.pool", "10"));
        EnvironmentConfig target = config("staging", Map.of("db.url", "jdbc:staging"));

        SyncResult result = syncService.sync(source, target, "admin");

        assertTrue(result.getAddedKeys().contains("db.pool"));
        assertTrue(result.getUpdatedKeys().isEmpty());
        assertEquals("jdbc:prod", target.getProperties().get("db.pool"));
        verify(auditLog, times(1)).record(any(ConfigAuditEntry.class));
    }

    @Test
    void sync_updatesExistingDifferentKeys() {
        EnvironmentConfig source = config("prod", Map.of("db.url", "jdbc:prod"));
        EnvironmentConfig target = config("staging", Map.of("db.url", "jdbc:staging"));

        SyncResult result = syncService.sync(source, target, "admin");

        assertTrue(result.getUpdatedKeys().contains("db.url"));
        assertTrue(result.getAddedKeys().isEmpty());
        assertEquals("jdbc:prod", target.getProperties().get("db.url"));
    }

    @Test
    void sync_doesNotRemoveTargetOnlyKeys() {
        EnvironmentConfig source = config("prod", Map.of("app.name", "jarvis"));
        EnvironmentConfig target = config("staging", Map.of("app.name", "jarvis", "debug", "true"));

        syncService.sync(source, target, "admin");

        assertTrue(target.getProperties().containsKey("debug"));
    }

    @Test
    void preview_doesNotModifyTarget() {
        EnvironmentConfig source = config("prod", Map.of("key1", "val1", "key2", "val2"));
        EnvironmentConfig target = config("staging", Map.of("key1", "old"));

        SyncResult result = syncService.preview(source, target);

        assertEquals("old", target.getProperties().get("key1"));
        assertFalse(target.getProperties().containsKey("key2"));
        assertTrue(result.getAddedKeys().contains("key2"));
        assertTrue(result.getUpdatedKeys().contains("key1"));
        verifyNoInteractions(auditLog);
    }

    @Test
    void sync_noChangesWhenIdentical() {
        EnvironmentConfig source = config("prod", Map.of("x", "1"));
        EnvironmentConfig target = config("staging", Map.of("x", "1"));

        SyncResult result = syncService.sync(source, target, "admin");

        assertTrue(result.getAddedKeys().isEmpty());
        assertTrue(result.getUpdatedKeys().isEmpty());
        verifyNoInteractions(auditLog);
    }
}
