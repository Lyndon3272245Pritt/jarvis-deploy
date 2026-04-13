package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Merges two EnvironmentConfig instances, with the override config
 * taking precedence over the base config for conflicting keys.
 */
public class ConfigMerger {

    /**
     * Merges base and override configs into a new EnvironmentConfig.
     * Keys present in override will replace those in base.
     * Keys only in base are kept as-is.
     *
     * @param base     the base environment config
     * @param override the overriding environment config
     * @return a new EnvironmentConfig representing the merged result
     * @throws IllegalArgumentException if either argument is null
     */
    public EnvironmentConfig merge(EnvironmentConfig base, EnvironmentConfig override) {
        Objects.requireNonNull(base, "Base config must not be null");
        Objects.requireNonNull(override, "Override config must not be null");

        Map<String, String> mergedProperties = new HashMap<>(base.getProperties());
        mergedProperties.putAll(override.getProperties());

        String mergedName = override.getName() != null && !override.getName().isBlank()
                ? override.getName()
                : base.getName();

        String mergedRegion = override.getRegion() != null && !override.getRegion().isBlank()
                ? override.getRegion()
                : base.getRegion();

        return new EnvironmentConfig(mergedName, mergedRegion, mergedProperties);
    }

    /**
     * Merges a list of configs in order, each successive config overriding the previous.
     *
     * @param configs ordered list of configs to merge
     * @return the fully merged EnvironmentConfig
     * @throws IllegalArgumentException if configs is null or empty
     */
    public EnvironmentConfig mergeAll(java.util.List<EnvironmentConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            throw new IllegalArgumentException("Config list must not be null or empty");
        }
        EnvironmentConfig result = configs.get(0);
        for (int i = 1; i < configs.size(); i++) {
            result = merge(result, configs.get(i));
        }
        return result;
    }
}
