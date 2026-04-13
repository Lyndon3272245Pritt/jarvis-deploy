package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigImporterTest {

    private ConfigImporter importer;

    @BeforeEach
    void setUp() {
        importer = new ConfigImporter();
    }

    @Test
    void importFromString_propertiesFormat_parsesCorrectly() throws ImportException {
        String content = "db.host=localhost\ndb.port=5432\n";
        EnvironmentConfig config = importer.importFromString(content, "staging",
                ConfigImporter.ImportFormat.PROPERTIES);

        assertEquals("staging", config.getEnvironment());
        assertEquals("localhost", config.getProperties().get("db.host"));
        assertEquals("5432", config.getProperties().get("db.port"));
    }

    @Test
    void importFromString_envFormat_parsesCorrectly() throws ImportException {
        String content = "# comment\nAPP_ENV=production\nAPP_PORT=8080\n";
        EnvironmentConfig config = importer.importFromString(content, "prod",
                ConfigImporter.ImportFormat.ENV);

        assertEquals("prod", config.getEnvironment());
        assertEquals("production", config.getProperties().get("APP_ENV"));
        assertEquals("8080", config.getProperties().get("APP_PORT"));
    }

    @Test
    void importFromString_emptyContent_throwsImportException() {
        assertThrows(ImportException.class,
                () -> importer.importFromString("", "dev", ConfigImporter.ImportFormat.ENV));
    }

    @Test
    void importFromString_invalidEnvLine_throwsImportException() {
        String bad = "NOEQUALSSIGN\n";
        assertThrows(ImportException.class,
                () -> importer.importFromString(bad, "dev", ConfigImporter.ImportFormat.ENV));
    }

    @Test
    void importFromFile_propertiesFile_parsesCorrectly(@TempDir Path tmpDir) throws IOException, ImportException {
        Path file = tmpDir.resolve("app.properties");
        Files.writeString(file, "service.name=jarvis\nservice.version=1.0\n");

        EnvironmentConfig config = importer.importFromFile(file, "dev",
                ConfigImporter.ImportFormat.PROPERTIES);

        assertEquals("dev", config.getEnvironment());
        assertEquals("jarvis", config.getProperties().get("service.name"));
    }

    @Test
    void importFromFile_missingFile_throwsImportException() {
        Path missing = Path.of("/nonexistent/path/config.properties");
        assertThrows(ImportException.class,
                () -> importer.importFromFile(missing, "dev", ConfigImporter.ImportFormat.PROPERTIES));
    }
}
