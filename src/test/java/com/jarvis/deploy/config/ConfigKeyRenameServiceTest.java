package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigKeyRenameServiceTest {

    private ConfigAuditLog auditLog;
    private ConfigKeyRenameService service;
    private EnvironmentConfig config;

    @BeforeEach
    void setUp() {
        auditLog = new ConfigAuditLog();
        service = new ConfigKeyRenameService(auditLog);

        Map<String, String> props = new LinkedHashMap<>();
        props.put("db.host", "localhost");
        props.put("db.port", "5432");
        props.put("app.name", "jarvis");
        config = new EnvironmentConfig("staging", props);
    }

    @Test
    void renameKey_success() {
        Map<String, String> result = service.renameKey(config, "db.host", "database.host", "alice");

        assertFalse(result.containsKey("db.host"));
        assertTrue(result.containsKey("database.host"));
        assertEquals("localhost", result.get("database.host"));
    }

    @Test
    void renameKey_preservesOtherKeys() {
        Map<String, String> result = service.renameKey(config, "db.port", "database.port", "alice");

        assertTrue(result.containsKey("db.host"));
        assertTrue(result.containsKey("app.name"));
        assertEquals(3, result.size());
    }

    @Test
    void renameKey_throwsWhenOldKeyMissing() {
        assertThrows(KeyRenameException.class,
                () -> service.renameKey(config, "nonexistent", "new.key", "alice"));
    }

    @Test
    void renameKey_throwsWhenNewKeyAlreadyExists() {
        assertThrows(KeyRenameException.class,
                () -> service.renameKey(config, "db.host", "db.port", "alice"));
    }

    @Test
    void renameKey_recordsAuditEntry() {
        service.renameKey(config, "db.host", "database.host", "bob");

        assertEquals(1, auditLog.getEntries().size());
        ConfigAuditEntry entry = auditLog.getEntries().get(0);
        assertEquals("bob", entry.getActor());
        assertEquals("staging", entry.getEnvironment());
        assertEquals("RENAME_KEY", entry.getAction());
        assertTrue(entry.getDetail().contains("db.host"));
        assertTrue(entry.getDetail().contains("database.host"));
    }

    @Test
    void batchRename_appliesAllRenames() {
        Map<String, String> renames = new LinkedHashMap<>();
        renames.put("db.host", "database.host");
        renames.put("db.port", "database.port");

        Map<String, String> result = service.batchRename(config, renames, "carol");

        assertTrue(result.containsKey("database.host"));
        assertTrue(result.containsKey("database.port"));
        assertFalse(result.containsKey("db.host"));
        assertFalse(result.containsKey("db.port"));
        assertEquals(2, auditLog.getEntries().size());
    }

    @Test
    void batchRename_abortsOnFirstConflict() {
        Map<String, String> renames = new LinkedHashMap<>();
        renames.put("db.host", "app.name"); // conflict
        renames.put("db.port", "database.port");

        assertThrows(KeyRenameException.class,
                () -> service.batchRename(config, renames, "carol"));
    }

    @Test
    void constructor_throwsOnNullAuditLog() {
        assertThrows(NullPointerException.class,
                () -> new ConfigKeyRenameService(null));
    }
}
