package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigExporterTest {

    @TempDir
    Path tempDir;

    private final ConfigExporter exporter = new ConfigExporter();

    private EnvironmentConfig config() {
        Map<String, String> props = new HashMap<>();
        props.put("db.host", "localhost");
        props.put("db.port", "5432");
        return new EnvironmentConfig("test", props);
    }

    @Test
    void exportAsProperties() throws IOException {
        Path out = tempDir.resolve("config.properties");
        exporter.export(config(), out, ConfigExporter.Format.PROPERTIES);

        String content = Files.readString(out);
        assertTrue(content.contains("db.host=localhost"));
        assertTrue(content.contains("db.port=5432"));
        assertTrue(content.startsWith("# Environment: test"));
    }

    @Test
    void exportAsEnv() throws IOException {
        Path out = tempDir.resolve("config.env");
        exporter.export(config(), out, ConfigExporter.Format.ENV);

        String content = Files.readString(out);
        assertTrue(content.contains("export DB_HOST=\"localhost\""));
        assertTrue(content.contains("export DB_PORT=\"5432\""));
    }

    @Test
    void exportAsJson() throws IOException {
        Path out = tempDir.resolve("config.json");
        exporter.export(config(), out, ConfigExporter.Format.JSON);

        String content = Files.readString(out);
        assertTrue(content.contains("\"environment\": \"test\""));
        assertTrue(content.contains("\"db.host\": \"localhost\""));
        assertTrue(content.contains("\"db.port\": \"5432\""));
    }

    @Test
    void exportCreatesFile() throws IOException {
        Path out = tempDir.resolve("output.properties");
        assertFalse(Files.exists(out));
        exporter.export(config(), out, ConfigExporter.Format.PROPERTIES);
        assertTrue(Files.exists(out));
    }
}
