package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigSearchServiceTest {

    private ConfigLoader configLoader;
    private ConfigTagManager tagManager;
    private ConfigSearchService searchService;

    @BeforeEach
    void setUp() {
        configLoader = mock(ConfigLoader.class);
        tagManager = mock(ConfigTagManager.class);
        searchService = new ConfigSearchService(configLoader, tagManager);
    }

    @Test
    void search_matchesByKeyPattern() throws ConfigLoadException {
        EnvironmentConfig config = new EnvironmentConfig("prod",
                Map.of("db.host", "localhost", "db.port", "5432", "app.name", "jarvis"));
        when(configLoader.load("prod")).thenReturn(config);
        when(tagManager.getTagsForKey(eq("prod"), anyString())).thenReturn(List.of());

        ConfigSearchQuery query = ConfigSearchQuery.builder()
                .environment("prod")
                .keyPattern("db\\.*")
                .build();

        ConfigSearchResult result = searchService.search(query);
        assertEquals(2, result.getTotalMatches());
        assertTrue(result.getMatchedEntries().containsKey("db.host"));
        assertTrue(result.getMatchedEntries().containsKey("db.port"));
    }

    @Test
    void search_matchesByValuePattern() throws ConfigLoadException {
        EnvironmentConfig config = new EnvironmentConfig("staging",
                Map.of("db.host", "staging-db", "cache.host", "staging-cache", "app.port", "8080"));
        when(configLoader.load("staging")).thenReturn(config);
        when(tagManager.getTagsForKey(eq("staging"), anyString())).thenReturn(List.of());

        ConfigSearchQuery query = ConfigSearchQuery.builder()
                .environment("staging")
                .valuePattern("staging-.*")
                .build();

        ConfigSearchResult result = searchService.search(query);
        assertEquals(2, result.getTotalMatches());
    }

    @Test
    void search_filtersByRequiredTag() throws ConfigLoadException {
        EnvironmentConfig config = new EnvironmentConfig("prod",
                Map.of("db.host", "localhost", "app.secret", "s3cr3t"));
        when(configLoader.load("prod")).thenReturn(config);
        when(tagManager.getTagsForKey("prod", "db.host")).thenReturn(List.of("infra"));
        when(tagManager.getTagsForKey("prod", "app.secret")).thenReturn(List.of("secret", "infra"));

        ConfigSearchQuery query = ConfigSearchQuery.builder()
                .environment("prod")
                .requiredTag("secret")
                .build();

        ConfigSearchResult result = searchService.search(query);
        assertEquals(1, result.getTotalMatches());
        assertTrue(result.getMatchedEntries().containsKey("app.secret"));
    }

    @Test
    void search_throwsWhenEnvironmentMissing() {
        ConfigSearchQuery query = ConfigSearchQuery.builder().keyPattern("db.*").build();
        assertThrows(IllegalArgumentException.class, () -> searchService.search(query));
    }

    @Test
    void search_throwsOnInvalidPattern() throws ConfigLoadException {
        EnvironmentConfig config = new EnvironmentConfig("dev", Map.of("key", "val"));
        when(configLoader.load("dev")).thenReturn(config);

        ConfigSearchQuery query = ConfigSearchQuery.builder()
                .environment("dev")
                .keyPattern("[invalid")
                .build();

        assertThrows(IllegalArgumentException.class, () -> searchService.search(query));
    }

    @Test
    void search_returnsEmptyWhenNoMatch() throws ConfigLoadException {
        EnvironmentConfig config = new EnvironmentConfig("dev", Map.of("app.name", "jarvis"));
        when(configLoader.load("dev")).thenReturn(config);
        when(tagManager.getTagsForKey(eq("dev"), anyString())).thenReturn(List.of());

        ConfigSearchQuery query = ConfigSearchQuery.builder()
                .environment("dev")
                .keyPattern("nonexistent.*")
                .build();

        ConfigSearchResult result = searchService.search(query);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalMatches());
    }
}
