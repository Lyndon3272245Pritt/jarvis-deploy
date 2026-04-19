package com.jarvis.deploy.config;

import java.util.List;

/**
 * Builds a ConfigResolverChain from a profile-ordered list of environment names,
 * loading each via a provided ConfigLoader.
 */
public class ConfigResolverChainBuilder {

    private final ConfigLoader loader;

    public ConfigResolverChainBuilder(ConfigLoader loader) {
        if (loader == null) throw new IllegalArgumentException("ConfigLoader must not be null");
        this.loader = loader;
    }

    /**
     * Builds a chain from the given environment names in priority order (index 0 = highest priority).
     */
    public ConfigResolverChain build(List<String> environmentNames) throws ConfigLoadException {
        if (environmentNames == null || environmentNames.isEmpty()) {
            throw new IllegalArgumentException("At least one environment name is required");
        }
        ConfigResolverChain chain = new ConfigResolverChain();
        for (String name : environmentNames) {
            EnvironmentConfig config = loader.load(name);
            chain.addSource(config);
        }
        return chain;
    }

    /**
     * Builds a chain with a base environment overridden by an overlay environment.
     */
    public ConfigResolverChain buildWithOverlay(String baseEnv, String overlayEnv) throws ConfigLoadException {
        return build(List.of(overlayEnv, baseEnv));
    }
}
