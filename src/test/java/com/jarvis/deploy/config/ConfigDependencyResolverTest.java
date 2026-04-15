package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigDependencyResolverTest {

    private ConfigDependencyGraph graph;
    private ConfigMerger merger;
    private ConfigDependencyResolver resolver;

    @BeforeEach
    void setUp() {
        graph = new ConfigDependencyGraph();
        graph.addEnvironment("base");
        graph.addEnvironment("staging");
        graph.addEnvironment("production");
        graph.addDependency("staging", "base");
        graph.addDependency("production", "staging");

        merger = new ConfigMerger();
        resolver = new ConfigDependencyResolver(graph, merger);
    }

    private EnvironmentConfig makeConfig(String name, Map<String, String> props) {
        return new EnvironmentConfig(name, props);
    }

    @Test
    void testResolveBaseHasNoInheritance() {
        Map<String, EnvironmentConfig> configs = new HashMap<>();
        configs.put("base", makeConfig("base", Map.of("db.url", "base-db")));
        configs.put("staging", makeConfig("staging", Map.of("app.port", "8080")));
        configs.put("production", makeConfig("production", Map.of("app.port", "443")));

        EnvironmentConfig resolved = resolver.resolve("base", configs);
        assertEquals("base-db", resolved.getProperties().get("db.url"));
        assertFalse(resolved.getProperties().containsKey("app.port"));
    }

    @Test
    void testResolveStagingInheritsFromBase() {
        Map<String, EnvironmentConfig> configs = new HashMap<>();
        configs.put("base", makeConfig("base", Map.of("db.url", "base-db", "log.level", "INFO")));
        configs.put("staging", makeConfig("staging", Map.of("log.level", "DEBUG", "app.port", "8080")));
        configs.put("production", makeConfig("production", Map.of("app.port", "443")));

        EnvironmentConfig resolved = resolver.resolve("staging", configs);
        assertEquals("base-db", resolved.getProperties().get("db.url"));
        assertEquals("DEBUG", resolved.getProperties().get("log.level"));
        assertEquals("8080", resolved.getProperties().get("app.port"));
    }

    @Test
    void testResolveProductionInheritsTransitively() {
        Map<String, EnvironmentConfig> configs = new HashMap<>();
        configs.put("base", makeConfig("base", Map.of("db.url", "base-db", "log.level", "INFO")));
        configs.put("staging", makeConfig("staging", Map.of("log.level", "DEBUG")));
        configs.put("production", makeConfig("production", Map.of("app.port", "443")));

        EnvironmentConfig resolved = resolver.resolve("production", configs);
        assertEquals("base-db", resolved.getProperties().get("db.url"));
        assertEquals("DEBUG", resolved.getProperties().get("log.level"));
        assertEquals("443", resolved.getProperties().get("app.port"));
    }

    @Test
    void testResolveUnknownEnvironmentThrows() {
        Map<String, EnvironmentConfig> configs = new HashMap<>();
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("unknown", configs));
    }

    @Test
    void testResolveNullGraphThrows() {
        assertThrows(NullPointerException.class, () -> new ConfigDependencyResolver(null, merger));
    }

    @Test
    void testResolveNullMergerThrows() {
        assertThrows(NullPointerException.class, () -> new ConfigDependencyResolver(graph, null));
    }
}
