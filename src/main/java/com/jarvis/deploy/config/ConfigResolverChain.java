package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves config values by walking a prioritized chain of EnvironmentConfig sources.
 * Earlier entries in the chain take precedence over later ones.
 */
public class ConfigResolverChain {

    private final List<EnvironmentConfig> chain = new ArrayList<>();

    public ConfigResolverChain addSource(EnvironmentConfig config) {
        if (config == null) throw new IllegalArgumentException("Config source must not be null");
        chain.add(config);
        return this;
    }

    public Optional<String> resolve(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key must not be blank");
        for (EnvironmentConfig config : chain) {
            Map<String, String> props = config.getProperties();
            if (props != null && props.containsKey(key)) {
                return Optional.ofNullable(props.get(key));
            }
        }
        return Optional.empty();
    }

    public String resolveOrDefault(String key, String defaultValue) {
        return resolve(key).orElse(defaultValue);
    }

    public Map<String, String> resolveAll() {
        java.util.LinkedHashMap<String, String> merged = new java.util.LinkedHashMap<>();
        // Iterate in reverse so higher-priority sources overwrite
        for (int i = chain.size() - 1; i >= 0; i--) {
            Map<String, String> props = chain.get(i).getProperties();
            if (props != null) merged.putAll(props);
        }
        return java.util.Collections.unmodifiableMap(merged);
    }

    public int size() {
        return chain.size();
    }

    public void clear() {
        chain.clear();
    }
}
