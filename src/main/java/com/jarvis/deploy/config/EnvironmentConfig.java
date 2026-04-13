package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a deployment configuration for a single environment (e.g., dev, staging, prod).
 */
public class EnvironmentConfig {

    private final String name;
    private final String region;
    private final String namespace;
    private final Map<String, String> variables;

    public EnvironmentConfig(String name, String region, String namespace, Map<String, String> variables) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Environment name must not be null or blank");
        }
        this.name = name;
        this.region = region;
        this.namespace = namespace;
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getNamespace() {
        return namespace;
    }

    public Map<String, String> getVariables() {
        return new HashMap<>(variables);
    }

    public String getVariable(String key) {
        return variables.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnvironmentConfig)) return false;
        EnvironmentConfig that = (EnvironmentConfig) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return String.format("EnvironmentConfig{name='%s', region='%s', namespace='%s', variables=%s}",
                name, region, namespace, variables);
    }
}
