package com.jarvis.deploy.config;

import java.util.Map;

/**
 * A config loader decorator that applies a {@link ConfigScopeFilter} before returning
 * properties, ensuring only in-scope keys are exposed.
 */
public class ScopeAwareConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigScopeFilter scopeFilter;

    public ScopeAwareConfigLoader(ConfigLoader delegate, ConfigScopeFilter scopeFilter) {
        if (delegate == null) throw new IllegalArgumentException("delegate must not be null");
        if (scopeFilter == null) throw new IllegalArgumentException("scopeFilter must not be null");
        this.delegate = delegate;
        this.scopeFilter = scopeFilter;
    }

    /**
     * Loads the environment config for the given environment name and filters properties
     * through the scope filter. Throws {@link ConfigLoadException} if the environment is
     * outside the allowed scope.
     */
    public EnvironmentConfig load(String environment) throws ConfigLoadException {
        if (!scopeFilter.isEnvironmentInScope(environment)) {
            throw new ConfigLoadException(
                    "Environment '" + environment + "' is outside the allowed scope.");
        }
        EnvironmentConfig raw = delegate.load(environment);
        Map<String, String> filtered = scopeFilter.filterByPrefix(raw.getProperties());
        return new EnvironmentConfig(raw.getName(), filtered);
    }

    public ConfigScopeFilter getScopeFilter() {
        return scopeFilter;
    }
}
