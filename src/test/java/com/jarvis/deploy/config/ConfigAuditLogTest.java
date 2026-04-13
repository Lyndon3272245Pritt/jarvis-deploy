package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigAuditLogTest {

    private ConfigAuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new ConfigAuditLog();
    }

    @Test
    void recordAndRetrieveAll() {
        auditLog.record("dev", ConfigAuditEntry.Action.LOADED, "3 properties");
        auditLog.record("prod", ConfigAuditEntry.Action.EXPORTED, "exported to file");

        List<ConfigAuditEntry> all = auditLog.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void filterByEnvironment() {
        auditLog.record("dev", ConfigAuditEntry.Action.LOADED, "details");
        auditLog.record("prod", ConfigAuditEntry.Action.LOADED, "details");
        auditLog.record("dev", ConfigAuditEntry.Action.MERGED, "merged base");

        List<ConfigAuditEntry> devEntries = auditLog.getByEnvironment("dev");
        assertEquals(2, devEntries.size());
        devEntries.forEach(e -> assertEquals("dev", e.getEnvironment()));
    }

    @Test
    void filterByAction() {
        auditLog.record("dev", ConfigAuditEntry.Action.LOADED, "d");
        auditLog.record("staging", ConfigAuditEntry.Action.SNAPSHOT_CREATED, "s");
        auditLog.record("prod", ConfigAuditEntry.Action.LOADED, "p");

        List<ConfigAuditEntry> loaded = auditLog.getByAction(ConfigAuditEntry.Action.LOADED);
        assertEquals(2, loaded.size());
    }

    @Test
    void clearResetsLog() {
        auditLog.record("dev", ConfigAuditEntry.Action.LOADED, "x");
        auditLog.clear();
        assertEquals(0, auditLog.size());
    }

    @Test
    void nullEnvironmentReturnsEmpty() {
        auditLog.record("dev", ConfigAuditEntry.Action.LOADED, "x");
        assertTrue(auditLog.getByEnvironment(null).isEmpty());
    }

    @Test
    void getByNullActionReturnsEmpty() {
        auditLog.record("dev", ConfigAuditEntry.Action.LOADED, "x");
        assertTrue(auditLog.getByAction(null).isEmpty());
    }
}
