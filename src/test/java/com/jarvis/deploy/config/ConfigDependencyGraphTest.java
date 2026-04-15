package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigDependencyGraphTest {

    private ConfigDependencyGraph graph;

    @BeforeEach
    void setUp() {
        graph = new ConfigDependencyGraph();
        graph.addEnvironment("base");
        graph.addEnvironment("staging");
        graph.addEnvironment("production");
    }

    @Test
    void testGetEnvironments() {
        assertTrue(graph.getEnvironments().contains("base"));
        assertTrue(graph.getEnvironments().contains("staging"));
        assertTrue(graph.getEnvironments().contains("production"));
    }

    @Test
    void testAddAndGetDependencies() {
        graph.addDependency("staging", "base");
        assertTrue(graph.getDependencies("staging").contains("base"));
        assertTrue(graph.getDependencies("base").isEmpty());
    }

    @Test
    void testAddDependencyUnknownEnvironmentThrows() {
        assertThrows(IllegalArgumentException.class, () -> graph.addDependency("staging", "unknown"));
        assertThrows(IllegalArgumentException.class, () -> graph.addDependency("unknown", "base"));
    }

    @Test
    void testTopologicalOrderNoDeps() {
        List<String> order = graph.topologicalOrder();
        assertEquals(3, order.size());
        assertTrue(order.containsAll(List.of("base", "staging", "production")));
    }

    @Test
    void testTopologicalOrderWithDeps() {
        graph.addDependency("staging", "base");
        graph.addDependency("production", "staging");
        List<String> order = graph.topologicalOrder();
        assertTrue(order.indexOf("base") < order.indexOf("staging"));
        assertTrue(order.indexOf("staging") < order.indexOf("production"));
    }

    @Test
    void testCycleDetection() {
        graph.addDependency("staging", "base");
        graph.addDependency("base", "staging");
        assertTrue(graph.hasCycle());
        assertThrows(ConfigDependencyCycleException.class, () -> graph.topologicalOrder());
    }

    @Test
    void testNoCycleReturnsFalse() {
        graph.addDependency("staging", "base");
        assertFalse(graph.hasCycle());
    }
}
