package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigExportServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void delegatesLoadAndExport() throws IOException {
        ConfigLoader loader = mock(ConfigLoader.class);
        ConfigExporter exporter = new ConfigExporter();
        EnvironmentConfig cfg = new EnvironmentConfig("staging", Map.of("key", "value"));
        when(loader.load("staging")).thenReturn(cfg);

        ConfigExportService service = new ConfigExportService(loader, exporter);
        Path out = tempDir.resolve("staging.properties");
        service.exportEnvironment("staging", out, ConfigExporter.Format.PROPERTIES);

        assertTrue(Files.exists(out));
        String content = Files.readString(out);
        assertTrue(content.contains("key=value"));
        verify(loader).load("staging");
    }

    @Test
    void wrapsConfigLoadExceptionAsExportException() {
        ConfigLoader loader = mock(ConfigLoader.class);
        when(loader.load("missing")).thenThrow(new ConfigLoadException("not found"));

        ConfigExportService service = new ConfigExportService(loader, new ConfigExporter());
        Path out = tempDir.resolve("missing.properties");

        ExportException ex = assertThrows(ExportException.class,
                () -> service.exportEnvironment("missing", out, ConfigExporter.Format.PROPERTIES));
        assertTrue(ex.getMessage().contains("missing"));
    }

    @Test
    void wrapsIOExceptionAsExportException() throws IOException {
        ConfigLoader loader = mock(ConfigLoader.class);
        ConfigExporter exporter = mock(ConfigExporter.class);
        EnvironmentConfig cfg = new EnvironmentConfig("prod", Map.of());
        when(loader.load("prod")).thenReturn(cfg);
        Path out = tempDir.resolve("prod.json");
        doThrow(new IOException("disk full")).when(exporter).export(any(), any(), any());

        ConfigExportService service = new ConfigExportService(loader, exporter);
        ExportException ex = assertThrows(ExportException.class,
                () -> service.exportEnvironment("prod", out, ConfigExporter.Format.JSON));
        assertTrue(ex.getMessage().contains("prod"));
    }
}
