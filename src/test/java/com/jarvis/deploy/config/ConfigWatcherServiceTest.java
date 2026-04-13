package com.jarvis.deploy.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigWatcherServiceTest {

    @TempDir
    Path tempDir;

    private ConfigLoader mockLoader;
    private ConfigWatcherService service;
    private Path configFile;

    @BeforeEach
    void setUp() throws IOException {
        mockLoader = mock(ConfigLoader.class);
        service = new ConfigWatcherService(mockLoader);
        configFile = tempDir.resolve("env.yaml");
        Files.writeString(configFile, "version: 1");
    }

    @AfterEach
    void tearDown() throws IOException {
        service.close();
    }

    @Test
    void shouldLoadInitialConfigOnWatch() throws IOException {
        EnvironmentConfig initial = mock(EnvironmentConfig.class);
        when(mockLoader.load(configFile)).thenReturn(initial);

        service.watch("staging", configFile);

        assertSame(initial, service.getCurrent("staging"));
    }

    @Test
    void shouldNotifyListenerOnFileChange() throws Exception {
        EnvironmentConfig initial = mock(EnvironmentConfig.class);
        EnvironmentConfig updated = mock(EnvironmentConfig.class);
        when(mockLoader.load(configFile))
                .thenReturn(initial)
                .thenReturn(updated);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<EnvironmentConfig> captured = new AtomicReference<>();

        service.addChangeListener((env, prev, next) -> {
            captured.set(next);
            latch.countDown();
        });
        service.watch("prod", configFile);

        // Trigger file change
        Files.writeString(configFile, "version: 2");

        boolean notified = latch.await(3, TimeUnit.SECONDS);
        assertTrue(notified, "Listener should have been notified within 3 seconds");
        assertSame(updated, captured.get());
    }

    @Test
    void shouldReturnNullForUnwatchedEnvironment() {
        assertNull(service.getCurrent("unknown"));
    }

    @Test
    void shouldKeepPreviousConfigOnReloadFailure() throws Exception {
        EnvironmentConfig initial = mock(EnvironmentConfig.class);
        when(mockLoader.load(configFile))
                .thenReturn(initial)
                .thenThrow(new ConfigLoadException("parse error"));

        service.watch("dev", configFile);
        Files.writeString(configFile, "version: broken");

        Thread.sleep(500);
        assertSame(initial, service.getCurrent("dev"));
    }
}
