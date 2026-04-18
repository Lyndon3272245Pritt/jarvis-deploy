package com.jarvis.deploy.config;

import java.util.*;

/**
 * Represents a directed dependency graph between environment configs.
 * Allows detection of dependency order and circular dependencies.
 */
public class ConfigDependencyGraph {

    private final Map<String, Set<String>> adjacency = new LinkedHashMap<>();

    public void addEnvironment(String env) {
        adjacency.putIfAbsent(env, new LinkedHashSet<>());
    }

    public void addDependency(String from, String to) {
        if (!adjacency.containsKey(from)) {
            throw new IllegalArgumentException("Unknown environment: " + from);
        }
        if (!adjacency.containsKey(to)) {
            throw new IllegalArgumentException("Unknown environment: " + to);
        }
        adjacency.get(from).add(to);
    }

    public Set<String> getDependencies(String env) {
        return Collections.unmodifiableSet(adjacency.getOrDefault(env, Collections.emptySet()));
    }

    public Set<String> getEnvironments() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /**
     * Returns all environments that directly or transitively depend on the given environment.
     * Useful for determining what needs to be redeployed when a config changes.
     *
     * @param env the environment whose dependents should be found
     * @return set of environment names that depend on the given environment
     */
    public Set<String> getTransitiveDependents(String env) {
        if (!adjacency.containsKey(env)) {
            throw new IllegalArgumentException("Unknown environment: " + env);
        }
        Set<String> dependents = new LinkedHashSet<>();
        for (String candidate : adjacency.keySet()) {
            if (!candidate.equals(env)) {
                collectDependents(candidate, env, dependents, new HashSet<>());
            }
        }
        return Collections.unmodifiableSet(dependents);
    }

    private boolean collectDependents(String current, String target, Set<String> dependents, Set<String> visited) {
        if (!visited.add(current)) {
            return dependents.contains(current);
        }
        for (String dep : adjacency.getOrDefault(current, Collections.emptySet())) {
            if (dep.equals(target) || collectDependents(dep, target, dependents, visited)) {
                dependents.add(current);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns environments in topological order (dependencies first).
     * Throws ConfigDependencyCycleException if a cycle is detected.
     */
    public List<String> topologicalOrder() {
        List<String> result = new ArrayList<>();
        Map<String, Integer> state = new HashMap<>(); // 0=unvisited,1=visiting,2=done
        for (String env : adjacency.keySet()) {
            state.put(env, 0);
        }
        for (String env : adjacency.keySet()) {
            if (state.get(env) == 0) {
                dfs(env, state, result);
            }
        }
        Collections.reverse(result);
        return result;
    }

    private void dfs(String env, Map<String, Integer> state, List<String> result) {
        state.put(env, 1);
        for (String dep : adjacency.getOrDefault(env, Collections.emptySet())) {
            int s = state.getOrDefault(dep, 0);
            if (s == 1) {
                throw new ConfigDependencyCycleException("Cycle detected involving environment: " + dep);
            }
            if (s == 0) {
                dfs(dep, state, result);
            }
        }
        state.put(env, 2);
        result.add(env);
    }

    public boolean hasCycle() {
        try {
            topologicalOrder();
            return false;
        } catch (ConfigDependencyCycleException e) {
            return true;
        }
    }
}
