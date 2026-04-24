package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigGroupExportServiceTest {

    @Mock
    private ConfigExporter exporter;

    private ConfigGroupManager groupManager;
    private ConfigGroupExportService service;

    @BeforeEach
    void setUp() {
        groupManager = new ConfigGroupManager();
        service = new ConfigGroupExportService(groupManager, exporter);
    }

    @Test
    void exportGroup_delegatesFilteredConfig() throws ExportException {
        groupManager.createGroup("db");
        groupManager.addKeyToGroup("db", "db.url");

        EnvironmentConfig env = new EnvironmentConfig("prod",
                Map.of("db.url", "jdbc:pg:5432", "app.name", "jarvis"));

        when(exporter.export(any(EnvironmentConfig.class), eq("json")))
                .thenReturn("{\"db.url\":\"jdbc:pg:5432\"}");

        String result = service.exportGroup("db", env, "json");

        assertEquals("{\"db.url\":\"jdbc:pg:5432\"}", result);
        verify(exporter).export(argThat(e ->
                e.getName().contains("db") && e.getProperties().containsKey("db.url")
                        && !e.getProperties().containsKey("app.name")
        ), eq("json"));
    }

    @Test
    void exportGroup_unknownGroup_throws() {
        EnvironmentConfig env = new EnvironmentConfig("prod", Map.of());
        assertThrows(IllegalArgumentException.class,
                () -> service.exportGroup("missing", env, "json"));
    }

    @Test
    void exportAllGroups_exportsEachGroup() throws ExportException {
        groupManager.createGroup("g1");
        groupManager.createGroup("g2");
        groupManager.addKeyToGroup("g1", "k1");
        groupManager.addKeyToGroup("g2", "k2");

        EnvironmentConfig env = new EnvironmentConfig("staging",
                Map.of("k1", "v1", "k2", "v2"));

        when(exporter.export(any(), eq("properties")))
                .thenReturn("k=v");

        Map<String, String> all = service.exportAllGroups(env, "properties");

        assertEquals(2, all.size());
        assertTrue(all.containsKey("g1"));
        assertTrue(all.containsKey("g2"));
        verify(exporter, times(2)).export(any(), eq("properties"));
    }

    @Test
    void constructor_nullArgs_throws() {
        assertThrows(NullPointerException.class,
                () -> new ConfigGroupExportService(null, exporter));
        assertThrows(NullPointerException.class,
                () -> new ConfigGroupExportService(groupManager, null));
    }
}