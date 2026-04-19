package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigResolverChainTest {

    private ConfigResolverChain chain;
    private EnvironmentConfig primary;
    private EnvironmentConfig fallback;

    @BeforeEach
    void setUp() {
        chain = new ConfigResolverChain();
        primary = mock(EnvironmentConfig.class);
        fallback = mock(EnvironmentConfig.class);
    }

    @Test
    void resolve_returnsValueFromFirstMatchingSource() {
        when(primary.getProperties()).thenReturn(Map.of("db.url", "jdbc:primary"));
        when(fallback.getProperties()).thenReturn(Map.of("db.url", "jdbc:fallback", "db.user", "admin"));
        chain.addSource(primary).addSource(fallback);

        assertEquals(Optional.of("jdbc:primary"), chain.resolve("db.url"));
    }

    @Test
    void resolve_fallsBackToSecondSource() {
        when(primary.getProperties()).thenReturn(Map.of("app.name", "jarvis"));
        when(fallback.getProperties()).thenReturn(Map.of("db.user", "admin"));
        chain.addSource(primary).addSource(fallback);

        assertEquals(Optional.of("admin"), chain.resolve("db.user"));
    }

    @Test
    void resolve_returnsEmptyWhenKeyMissing() {
        when(primary.getProperties()).thenReturn(Map.of());
        chain.addSource(primary);

        assertTrue(chain.resolve("missing.key").isEmpty());
    }

    @Test
    void resolveOrDefault_returnsDefaultWhenMissing() {
        when(primary.getProperties()).thenReturn(Map.of());
        chain.addSource(primary);

        assertEquals("default-val", chain.resolveOrDefault("x", "default-val"));
    }

    @Test
    void resolveAll_mergesWithPriorityOrder() {
        when(primary.getProperties()).thenReturn(Map.of("key", "from-primary"));
        when(fallback.getProperties()).thenReturn(Map.of("key", "from-fallback", "extra", "value"));
        chain.addSource(primary).addSource(fallback);

        Map<String, String> all = chain.resolveAll();
        assertEquals("from-primary", all.get("key"));
        assertEquals("value", all.get("extra"));
    }

    @Test
    void addSource_throwsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> chain.addSource(null));
    }

    @Test
    void resolve_throwsOnBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> chain.resolve(""));
    }

    @Test
    void size_reflectsAddedSources() {
        when(primary.getProperties()).thenReturn(Map.of());
        chain.addSource(primary);
        assertEquals(1, chain.size());
        chain.clear();
        assertEquals(0, chain.size());
    }
}
