package com.jarvis.deploy.config;

import java.util.List;

/**
 * A config loader decorator that filters environments by tag before loading.
 * Only environments carrying the required tag are permitted to load.
 */
public class TagAwareConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigTagManager tagManager;

    public TagAwareConfigLoader(ConfigLoader delegate, ConfigTagManager tagManager) {
        this.delegate = delegate;
        this.tagManager = tagManager;
    }

    /**
     * Loads the config for the given environment only if it carries the specified tag.
     *
     * @throws ConfigLoadException if the environment does not have the required tag
     */
    public EnvironmentConfig loadForTag(String environment, String requiredTag) throws ConfigLoadException {
        if (!tagManager.hasTag(environment, requiredTag)) {
            throw new ConfigLoadException(
                "Environment '" + environment + "' does not have required tag '" + requiredTag + "'"
            );
        }
        return delegate.load(environment);
    }

    /**
     * Loads configs for all environments that carry the specified tag.
     */
    public List<EnvironmentConfig> loadAllByTag(String tag) {
        List<String> envs = tagManager.getEnvironmentsByTag(tag);
        return envs.stream()
                .map(env -> {
                    try {
                        return delegate.load(env);
                    } catch (ConfigLoadException e) {
                        throw new RuntimeException("Failed to load config for env '" + env + "': " + e.getMessage(), e);
                    }
                })
                .toList();
    }
}
