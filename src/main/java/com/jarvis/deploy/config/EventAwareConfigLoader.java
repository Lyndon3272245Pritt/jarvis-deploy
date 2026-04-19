package com.jarvis.deploy.config;

import java.util.Map;

/**
 * Wraps a ConfigLoader and publishes events on load operations.
 */
public class EventAwareConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigEventPublisher publisher;

    public EventAwareConfigLoader(ConfigLoader delegate, ConfigEventPublisher publisher) {
        if (delegate == null) throw new IllegalArgumentException("Delegate must not be null");
        if (publisher == null) throw new IllegalArgumentException("Publisher must not be null");
        this.delegate = delegate;
        this.publisher = publisher;
    }

    public EnvironmentConfig load(String environment) throws ConfigLoadException {
        EnvironmentConfig config = delegate.load(environment);
        publisher.publish(environment, ConfigEventPublisher.EventType.LOADED,
                "Loaded " + config.getProperties().size() + " properties");
        return config;
    }

    public EnvironmentConfig loadWithOverrides(String environment, Map<String, String> overrides)
            throws ConfigLoadException {
        EnvironmentConfig config = delegate.load(environment);
        Map<String, String> merged = new java.util.LinkedHashMap<>(config.getProperties());
        merged.putAll(overrides);
        EnvironmentConfig result = new EnvironmentConfig(environment, merged);
        publisher.publish(environment, ConfigEventPublisher.EventType.UPDATED,
                "Applied " + overrides.size() + " override(s)");
        return result;
    }
}
