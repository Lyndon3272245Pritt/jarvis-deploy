package com.jarvis.deploy.config;

import java.util.Map;

/**
 * Enforces quota policies on EnvironmentConfig instances.
 */
public class ConfigQuotaEnforcer {

    private final ConfigQuotaPolicy policy;

    public ConfigQuotaEnforcer(ConfigQuotaPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy must not be null");
        }
        this.policy = policy;
    }

    /**
     * Validates that the given config does not exceed the quota for its environment.
     *
     * @throws ConfigQuotaEnforcementException if the quota is exceeded
     */
    public void enforce(EnvironmentConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        String env = config.getEnvironment();
        int limit = policy.getLimit(env);
        Map<String, String> props = config.getProperties();
        if (props.size() > limit) {
            throw new ConfigQuotaEnforcementException(
                String.format("Environment '%s' exceeds quota: %d keys present, limit is %d",
                    env, props.size(), limit));
        }
    }

    /**
     * Returns true if the config is within quota.
     */
    public boolean isWithinQuota(EnvironmentConfig config) {
        try {
            enforce(config);
            return true;
        } catch (ConfigQuotaEnforcementException e) {
            return false;
        }
    }

    public ConfigQuotaPolicy getPolicy() {
        return policy;
    }
}
