package com.jarvis.deploy.config;

import java.util.*;

/**
 * Resolves and merges environment configs in dependency order.
 * Base configs are merged into dependent configs so that dependents
 * inherit properties from their dependencies (dependents override bases).
 */
public class ConfigDependencyResolver {

    private final ConfigDependencyGraph graph;
    private final ConfigMerger merger;

    public ConfigDependencyResolver(ConfigDependencyGraph graph, ConfigMerger merger) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.merger = Objects.requireNonNull(merger, "merger must not be null");
    }

    /**
     * Resolves the given environment config by merging all transitive dependency
     * configs into it, in topological order.
     *
     * @param target    the environment to resolve
     * @param configMap map of environment name to its raw EnvironmentConfig
     * @return merged EnvironmentConfig with inherited properties applied
     */
    public EnvironmentConfig resolve(String target, Map<String, EnvironmentConfig> configMap) {
        if (!graph.getEnvironments().contains(target)) {
            throw new IllegalArgumentException("Unknown environment: " + target);
        }
        List<String> order = graph.topologicalOrder();
        // Collect transitive dependencies for target
        List<String> chain = buildChain(target, order);

        EnvironmentConfig resolved = null;
        for (String env : chain) {
            EnvironmentConfig cfg = configMap.get(env);
            if (cfg == null) {
                throw new IllegalStateException("No config found for environment: " + env);
            }
            if (resolved == null) {
                resolved = cfg;
            } else {
                // dependent overrides base
                resolved = merger.merge(resolved, cfg);
            }
        }
        return resolved;
    }

    private List<String> buildChain(String target, List<String> topoOrder) {
        Set<String> visited = new LinkedHashSet<>();
        collectDeps(target, visited);
        // preserve topological order among collected nodes, target last
        List<String> chain = new ArrayList<>();
        for (String env : topoOrder) {
            if (visited.contains(env)) {
                chain.add(env);
            }
        }
        return chain;
    }

    private void collectDeps(String env, Set<String> visited) {
        for (String dep : graph.getDependencies(env)) {
            collectDeps(dep, visited);
        }
        visited.add(env);
    }
}
