package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigCommentExporterTest {

    private ConfigCommentManager manager;
    private ConfigCommentExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        manager = new ConfigCommentManager();
        exporter = new ConfigCommentExporter(manager);
    }

    @Test
    void exportToString_includesNonBlankComments() {
        manager.addComment("prod", "db.url", "Primary DB");
        manager.addComment("prod", "db.password", "");
        String output = exporter.exportToString("prod");
        assertTrue(output.contains("db.url=Primary DB"));
        assertFalse(output.contains("db.password"));
        assertTrue(output.contains("# Comments for environment: prod"));
    }

    @Test
    void exportToString_emptyEnvironment_onlyHeader() {
        String output = exporter.exportToString("empty-env");
        assertTrue(output.startsWith("# Comments for environment: empty-env"));
        long lines = output.lines().filter(l -> !l.startsWith("#") && !l.isBlank()).count();
        assertEquals(0, lines);
    }

    @Test
    void export_writesFileCorrectly() throws Exception {
        manager.addComment("staging", "app.name", "Jarvis Staging");
        manager.addComment("staging", "app.port", "8080 port");
        Path outputFile = tempDir.resolve("staging.comments");
        exporter.export("staging", outputFile);
        List<String> lines = Files.readAllLines(outputFile);
        assertTrue(lines.stream().anyMatch(l -> l.equals("app.name=Jarvis Staging")));
        assertTrue(lines.stream().anyMatch(l -> l.equals("app.port=8080 port")));
    }

    @Test
    void export_invalidPath_throwsExportException() {
        Path badPath = tempDir.resolve("nonexistent_dir/comments.txt");
        assertThrows(ExportException.class, () -> exporter.export("prod", badPath));
    }

    @Test
    void constructor_nullCommentManager_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigCommentExporter(null));
    }
}
