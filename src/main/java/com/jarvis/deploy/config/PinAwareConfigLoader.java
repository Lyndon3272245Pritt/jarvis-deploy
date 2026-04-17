package com.jarvis.deploy.config;

import java.util.Map;

/**
 * A config loader decorator that applies pinned keys after loading,
 * ensuring pinned values always take precedence.
 */
public class PinAwareConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigPinManager pinManager;

    public PinAwareConfigLoader(ConfigLoader delegate, ConfigPinManager pinManager) {
        if (delegate == null || pinManager == null) {
            throw new IllegalArgumentException("delegate and pinManager must not be null");
        }
        this.delegate = delegate;
        this.pinManager = pinManager;
    }

    public EnvironmentConfig load(String environment) throws ConfigLoadException {
        EnvironmentConfig base = delegate.load(environment);
        Map<String, String> patched = pinManager.applyPins(environment, base.getProperties());
        return new EnvironmentConfig(base.getEnvironment(), patched);
    }
}
