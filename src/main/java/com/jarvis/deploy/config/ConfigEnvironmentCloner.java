package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Clones an existing environment config into a new environment,
 * optionally overriding specific keys.
 */
public class ConfigEnvironmentCloner {

    private final ConfigLoader configLoader;

    public ConfigEnvironmentCloner(ConfigLoader configLoader) {
        if (configLoader == null) throw new IllegalArgumentException("configLoader must not be null");
        this.configLoader = configLoader;
    }

    /**
     * Clone sourceEnv into targetEnvName, applying overrides on top.
     *
     * @param sourceEnv   the environment to clone from
     * @param targetEnvName the name for the cloned environment
     * @param overrides   key/value pairs to override in the clone
     * @return a new EnvironmentConfig representing the cloned environment
     */
    public EnvironmentConfig clone(EnvironmentConfig sourceEnv, String targetEnvName,
                                   Map<String, String> overrides) {
        if (sourceEnv == null) throw new IllegalArgumentException("sourceEnv must not be null");
        if (targetEnvName == null || targetEnvName.isBlank())
            throw new IllegalArgumentException("targetEnvName must not be blank");

        Map<String, String> clonedProperties = new HashMap<>(sourceEnv.getProperties());
        if (overrides != null) {
            clonedProperties.putAll(overrides);
        }
        return new EnvironmentConfig(targetEnvName, clonedProperties);
    }

    /**
     * Clone by loading source from the loader by name.
     */
    public EnvironmentConfig cloneByName(String sourceEnvName, String targetEnvName,
                                          Map<String, String> overrides) throws ConfigLoadException {
        EnvironmentConfig source = configLoader.load(sourceEnvName);
        return clone(source, targetEnvName, overrides);
    }
}
