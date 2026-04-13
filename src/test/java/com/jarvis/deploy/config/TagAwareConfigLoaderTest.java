package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TagAwareConfigLoaderTest {

    private ConfigLoader mockLoader;
    private ConfigTagManager tagManager;
    private TagAwareConfigLoader tagAwareLoader;

    @BeforeEach
    void setUp() {
        mockLoader = mock(ConfigLoader.class);
        tagManager = new ConfigTagManager();
        tagAwareLoader = new TagAwareConfigLoader(mockLoader, tagManager);
    }

    @Test
    void loadForTagSucceedsWhenTagPresent() throws ConfigLoadException {
        tagManager.addTag("prod", "live");
        EnvironmentConfig config = new EnvironmentConfig("prod", Map.of("key", "value"));
        when(mockLoader.load("prod")).thenReturn(config);

        EnvironmentConfig result = tagAwareLoader.loadForTag("prod", "live");
        assertEquals(config, result);
        verify(mockLoader).load("prod");
    }

    @Test
    void loadForTagThrowsWhenTagAbsent() {
        tagManager.addTag("prod", "internal");
        ConfigLoadException ex = assertThrows(ConfigLoadException.class,
                () -> tagAwareLoader.loadForTag("prod", "live"));
        assertTrue(ex.getMessage().contains("live"));
    }

    @Test
    void loadAllByTagReturnsMatchingEnvironments() throws ConfigLoadException {
        tagManager.addTag("prod", "live");
        tagManager.addTag("prod-eu", "live");
        tagManager.addTag("staging", "internal");

        EnvironmentConfig prodConfig = new EnvironmentConfig("prod", Map.of());
        EnvironmentConfig prodEuConfig = new EnvironmentConfig("prod-eu", Map.of());
        when(mockLoader.load("prod")).thenReturn(prodConfig);
        when(mockLoader.load("prod-eu")).thenReturn(prodEuConfig);

        List<EnvironmentConfig> results = tagAwareLoader.loadAllByTag("live");
        assertEquals(2, results.size());
        assertTrue(results.contains(prodConfig));
        assertTrue(results.contains(prodEuConfig));
    }

    @Test
    void loadAllByTagReturnsEmptyWhenNoEnvsHaveTag() {
        List<EnvironmentConfig> results = tagAwareLoader.loadAllByTag("nonexistent");
        assertTrue(results.isEmpty());
    }

    @Test
    void loadAllByTagWrapsConfigLoadException() throws ConfigLoadException {
        tagManager.addTag("broken", "live");
        when(mockLoader.load("broken")).thenThrow(new ConfigLoadException("disk error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tagAwareLoader.loadAllByTag("live"));
        assertTrue(ex.getMessage().contains("broken"));
    }
}
