package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConfigImportServiceTest {

    private ConfigImporter importer;
    private ConfigVersionRegistry versionRegistry;
    private ConfigImportService service;

    @BeforeEach
    void setUp() {
        importer = new ConfigImporter();
        versionRegistry = mock(ConfigVersionRegistry.class);
        service = new ConfigImportService(importer, versionRegistry);
    }

    @Test
    void importStringAndRegister_registersVersion() throws Exception {
        String content = "KEY=value\n";
        EnvironmentConfig config = service.importStringAndRegister(
                content, "staging", ConfigImporter.ImportFormat.ENV, "alice");

        assertNotNull(config);
        assertEquals("staging", config.getEnvironment());
        assertEquals("value", config.getProperties().get("KEY"));
        verify(versionRegistry, times(1)).register(eq("staging"), eq(config), eq("alice"));
    }

    @Test
    void importAndRegister_fromFile_registersVersion(@TempDir Path tmpDir) throws IOException, ImportException {
        Path file = tmpDir.resolve("env.properties");
        Files.writeString(file, "timeout=30\n");

        EnvironmentConfig config = service.importAndRegister(
                file, "prod", ConfigImporter.ImportFormat.PROPERTIES, "bob");

        assertEquals("prod", config.getEnvironment());
        assertEquals("30", config.getProperties().get("timeout"));
        verify(versionRegistry, times(1)).register(eq("prod"), eq(config), eq("bob"));
    }

    @Test
    void importStringAndRegister_registrationFailure_throwsImportException() throws Exception {
        doThrow(new RuntimeException("registry error"))
                .when(versionRegistry).register(anyString(), any(), anyString());

        assertThrows(ImportException.class, () ->
                service.importStringAndRegister(
                        "K=V\n", "dev", ConfigImporter.ImportFormat.ENV, "carol"));
    }

    @Test
    void importStringAndRegister_emptyContent_returnsEmptyProperties() throws Exception {
        EnvironmentConfig config = service.importStringAndRegister(
                "", "staging", ConfigImporter.ImportFormat.ENV, "alice");

        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty(),
                "Expected no properties for empty input");
        verify(versionRegistry, times(1)).register(eq("staging"), eq(config), eq("alice"));
    }

    @Test
    void constructor_nullImporter_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new ConfigImportService(null, versionRegistry));
    }

    @Test
    void constructor_nullRegistry_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new ConfigImportService(importer, null));
    }
}
