package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NormalizingConfigLoaderTest {

    private ConfigLoader mockDelegate;
    private ConfigKeyNormalizer normalizer;

    @BeforeEach
    void setUp() {
        mockDelegate = mock(ConfigLoader.class);
        normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.LOWERCASE);
    }

    @Test
    void loadsAndNormalizesKeys() throws ConfigLoadException {
        EnvironmentConfig raw = new EnvironmentConfig("prod", Map.of("DB_HOST", "db.prod.local", "APP_PORT", "443"));
        when(mockDelegate.load("prod")).thenReturn(raw);

        NormalizingConfigLoader loader = new NormalizingConfigLoader(mockDelegate, normalizer);
        EnvironmentConfig result = loader.load("prod");

        assertEquals("prod", result.getEnvironment());
        assertTrue(result.getProperties().containsKey("db_host"));
        assertTrue(result.getProperties().containsKey("app_port"));
        assertEquals("db.prod.local", result.getProperties().get("db_host"));
        assertFalse(result.getProperties().containsKey("DB_HOST"));
    }

    @Test
    void propagatesConfigLoadException() throws ConfigLoadException {
        when(mockDelegate.load("staging")).thenThrow(new ConfigLoadException("not found"));
        NormalizingConfigLoader loader = new NormalizingConfigLoader(mockDelegate, normalizer);
        assertThrows(ConfigLoadException.class, () -> loader.load("staging"));
    }

    @Test
    void nullDelegateThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new NormalizingConfigLoader(null, normalizer));
    }

    @Test
    void nullNormalizerThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new NormalizingConfigLoader(mockDelegate, null));
    }

    @Test
    void getNormalizerReturnsConfiguredNormalizer() {
        NormalizingConfigLoader loader = new NormalizingConfigLoader(mockDelegate, normalizer);
        assertSame(normalizer, loader.getNormalizer());
    }
}
