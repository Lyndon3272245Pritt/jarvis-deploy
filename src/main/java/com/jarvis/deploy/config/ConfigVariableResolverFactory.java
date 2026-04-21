package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for building ConfigVariableResolver instances from various sources,
 * including environment configs, system environment variables, and explicit overrides.
 */
public class ConfigVariableResolverFactory {

    public static ConfigVariableResolver fromEnvironmentConfig(EnvironmentConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("EnvironmentConfig must not be null");
        }
        Map<String, String> vars = new HashMap<>(config.getProperties());
        return new ConfigVariableResolver(vars);
    }

    public static ConfigVariableResolver fromSystemEnvironment() {
        return new ConfigVariableResolver(new HashMap<>(System.getenv()));
    }

    public static ConfigVariableResolver fromMerged(EnvironmentConfig config, Map<String, String> overrides) {
        if (config == null) {
            throw new IllegalArgumentException("EnvironmentConfig must not be null");
        }
        Map<String, String> vars = new HashMap<>(config.getProperties());
        if (overrides != null) {
            vars.putAll(overrides);
        }
        return new ConfigVariableResolver(vars);
    }

    public static ConfigVariableResolver fromMap(Map<String, String> variables) {
        return new ConfigVariableResolver(variables != null ? variables : new HashMap<>());
    }

    public static ConfigVariableResolver withSystemFallback(Map<String, String> primary) {
        Map<String, String> merged = new HashMap<>(System.getenv());
        if (primary != null) {
            merged.putAll(primary);
        }
        return new ConfigVariableResolver(merged);
    }
}
