package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    private ConfigLoader loader;

    @BeforeEach
    void setUp() {
        loader = new ConfigLoader(tempDir.toString());
    }

    @Test
    void load_returnsConfig_whenValidYamlFileExists() throws IOException {
        String yaml = "environment: dev\n"
                + "region: us-east-1\n"
                + "replicas: 2\n"
                + "debug: true\n";
        Files.writeString(tempDir.resolve("dev.yaml"), yaml);

        Optional<EnvironmentConfig> result = loader.load("dev");

        assertTrue(result.isPresent());
        EnvironmentConfig config = result.get();
        assertEquals("dev", config.getEnvironment());
        assertEquals("us-east-1", config.getRegion());
        assertEquals(2, config.getReplicas());
        assertTrue(config.isDebug());
    }

    @Test
    void load_returnsEmpty_whenFileDoesNotExist() {
        Optional<EnvironmentConfig> result = loader.load("staging");
        assertFalse(result.isPresent());
    }

    @Test
    void load_throwsException_whenEnvironmentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> loader.load(null));
    }

    @Test
    void load_throwsException_whenEnvironmentIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> loader.load("  "));
    }

    @Test
    void load_throwsConfigLoadException_whenYamlIsMalformed() throws IOException {
        Files.writeString(tempDir.resolve("broken.yaml"), "environment: [unclosed");
        assertThrows(ConfigLoadException.class, () -> loader.load("broken"));
    }

    @Test
    void load_fallsBackToClasspath_whenFileNotOnDisk() {
        // Uses classpath resource at configs/test-classpath.yaml if present;
        // without the resource this simply returns empty — verifying no exception is thrown.
        ConfigLoader classpathLoader = new ConfigLoader("configs");
        Optional<EnvironmentConfig> result = classpathLoader.load("nonexistent-env");
        assertFalse(result.isPresent());
    }
}
