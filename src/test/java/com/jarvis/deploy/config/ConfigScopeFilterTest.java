package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigScopeFilterTest {

    private ConfigScopeFilter filter;

    @BeforeEach
    void setUp() {
        filter = ConfigScopeFilterBuilder.newBuilder()
                .withKeyPrefix("db.")
                .allowEnvironment("staging")
                .allowEnvironment("production")
                .requireTag("backend")
                .build();
    }

    @Test
    void filterByPrefix_returnsOnlyMatchingKeys() {
        Map<String, String> props = Map.of(
                "db.host", "localhost",
                "db.port", "5432",
                "app.name", "jarvis"
        );
        Map<String, String> result = filter.filterByPrefix(props);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("db.host"));
        assertTrue(result.containsKey("db.port"));
        assertFalse(result.containsKey("app.name"));
    }

    @Test
    void filterByPrefix_emptyPrefix_returnsAll() {
        ConfigScopeFilter noPrefix = ConfigScopeFilterBuilder.newBuilder().build();
        Map<String, String> props = Map.of("a", "1", "b", "2");
        assertEquals(2, noPrefix.filterByPrefix(props).size());
    }

    @Test
    void isEnvironmentInScope_allowedEnvironment_returnsTrue() {
        assertTrue(filter.isEnvironmentInScope("staging"));
        assertTrue(filter.isEnvironmentInScope("production"));
    }

    @Test
    void isEnvironmentInScope_disallowedEnvironment_returnsFalse() {
        assertFalse(filter.isEnvironmentInScope("dev"));
    }

    @Test
    void isEnvironmentInScope_emptyAllowedSet_allowsAll() {
        ConfigScopeFilter openFilter = ConfigScopeFilterBuilder.newBuilder().build();
        assertTrue(openFilter.isEnvironmentInScope("anything"));
    }

    @Test
    void matchesTags_withAllRequiredTags_returnsTrue() {
        assertTrue(filter.matchesTags(Set.of("backend", "critical")));
    }

    @Test
    void matchesTags_missingRequiredTag_returnsFalse() {
        assertFalse(filter.matchesTags(Set.of("frontend")));
    }

    @Test
    void matchesTags_nullTags_returnsFalse() {
        assertFalse(filter.matchesTags(null));
    }

    @Test
    void scopeAwareLoader_throwsForOutOfScopeEnvironment() throws ConfigLoadException {
        ConfigLoader mockLoader = mock(ConfigLoader.class);
        ScopeAwareConfigLoader scopeLoader = new ScopeAwareConfigLoader(mockLoader, filter);
        assertThrows(ConfigLoadException.class, () -> scopeLoader.load("dev"));
        verifyNoInteractions(mockLoader);
    }

    @Test
    void scopeAwareLoader_filtersPropertiesForAllowedEnvironment() throws ConfigLoadException {
        ConfigLoader mockLoader = mock(ConfigLoader.class);
        EnvironmentConfig raw = new EnvironmentConfig("staging",
                Map.of("db.host", "db.staging.internal", "app.debug", "true"));
        when(mockLoader.load("staging")).thenReturn(raw);

        ScopeAwareConfigLoader scopeLoader = new ScopeAwareConfigLoader(mockLoader, filter);
        EnvironmentConfig result = scopeLoader.load("staging");

        assertEquals("staging", result.getName());
        assertEquals(1, result.getProperties().size());
        assertEquals("db.staging.internal", result.getProperties().get("db.host"));
    }
}
