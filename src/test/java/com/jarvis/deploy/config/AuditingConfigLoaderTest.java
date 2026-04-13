package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditingConfigLoaderTest {

    private ConfigLoader mockLoader;
    private ConfigAuditLog auditLog;
    private AuditingConfigLoader auditingLoader;

    @BeforeEach
    void setUp() {
        mockLoader = mock(ConfigLoader.class);
        auditLog = new ConfigAuditLog();
        auditingLoader = new AuditingConfigLoader(mockLoader, auditLog);
    }

    @Test
    void successfulLoadRecordsAuditEntry() throws ConfigLoadException {
        EnvironmentConfig config = new EnvironmentConfig("dev", Map.of("key1", "val1", "key2", "val2"));
        when(mockLoader.load("dev")).thenReturn(config);

        EnvironmentConfig result = auditingLoader.load("dev");

        assertNotNull(result);
        assertEquals(1, auditLog.size());
        ConfigAuditEntry entry = auditLog.getAll().get(0);
        assertEquals("dev", entry.getEnvironment());
        assertEquals(ConfigAuditEntry.Action.LOADED, entry.getAction());
        assertTrue(entry.getDetails().contains("2"));
    }

    @Test
    void failedLoadRecordsFailureAndRethrows() throws ConfigLoadException {
        when(mockLoader.load("prod")).thenThrow(new ConfigLoadException("file not found"));

        assertThrows(ConfigLoadException.class, () -> auditingLoader.load("prod"));

        assertEquals(1, auditLog.size());
        ConfigAuditEntry entry = auditLog.getAll().get(0);
        assertEquals("prod", entry.getEnvironment());
        assertTrue(entry.getDetails().startsWith("FAILED:"));
    }

    @Test
    void multipleLoadsAccumulateEntries() throws ConfigLoadException {
        EnvironmentConfig devConfig = new EnvironmentConfig("dev", Map.of("a", "1"));
        EnvironmentConfig stagingConfig = new EnvironmentConfig("staging", Map.of("b", "2", "c", "3"));
        when(mockLoader.load("dev")).thenReturn(devConfig);
        when(mockLoader.load("staging")).thenReturn(stagingConfig);

        auditingLoader.load("dev");
        auditingLoader.load("staging");

        List<ConfigAuditEntry> all = auditLog.getAll();
        assertEquals(2, all.size());
        assertEquals("dev", all.get(0).getEnvironment());
        assertEquals("staging", all.get(1).getEnvironment());
    }

    @Test
    void constructorRejectsNullDelegate() {
        assertThrows(NullPointerException.class,
                () -> new AuditingConfigLoader(null, auditLog));
    }

    @Test
    void constructorRejectsNullAuditLog() {
        assertThrows(NullPointerException.class,
                () -> new AuditingConfigLoader(mockLoader, null));
    }
}
