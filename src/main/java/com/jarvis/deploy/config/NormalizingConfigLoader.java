package com.jarvis.deploy.config;

import java.util.Map;

/**
 * A config loader decorator that normalizes all keys before returning
 * the loaded environment configuration.
 */
public class NormalizingConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigKeyNormalizer normalizer;

    public NormalizingConfigLoader(ConfigLoader delegate, ConfigKeyNormalizer normalizer) {
        if (delegate == null) throw new IllegalArgumentException("Delegate loader must not be null");
        if (normalizer == null) throw new IllegalArgumentException("Normalizer must not be null");
        this.delegate = delegate;
        this.normalizer = normalizer;
    }

    public EnvironmentConfig load(String environment) throws ConfigLoadException {
        EnvironmentConfig original = delegate.load(environment);
        Map<String, String> normalizedProperties = normalizer.normalizeKeys(original.getProperties());
        return new EnvironmentConfig(original.getEnvironment(), normalizedProperties);
    }

    public ConfigKeyNormalizer getNormalizer() {
        return normalizer;
    }
}
