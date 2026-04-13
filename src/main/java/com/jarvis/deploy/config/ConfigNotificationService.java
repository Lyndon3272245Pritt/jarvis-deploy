package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing and dispatching configuration change notifications
 * to registered listeners, optionally filtered by environment.
 */
public class ConfigNotificationService {

    private final List<ConfigNotificationListener> globalListeners = new CopyOnWriteArrayList<>();
    private final Map<String, List<ConfigNotificationListener>> envListeners = new ConcurrentHashMap<>();

    /**
     * Registers a listener that receives all events regardless of environment.
     */
    public void registerGlobalListener(ConfigNotificationListener listener) {
        if (listener == null) throw new IllegalArgumentException("listener must not be null");
        globalListeners.add(listener);
    }

    /**
     * Registers a listener scoped to a specific environment.
     */
    public void registerEnvListener(String environment, ConfigNotificationListener listener) {
        if (environment == null) throw new IllegalArgumentException("environment must not be null");
        if (listener == null) throw new IllegalArgumentException("listener must not be null");
        envListeners.computeIfAbsent(environment, e -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Publishes a notification event to all relevant listeners.
     */
    public void publish(ConfigNotificationEvent event) {
        if (event == null) throw new IllegalArgumentException("event must not be null");
        for (ConfigNotificationListener listener : globalListeners) {
            listener.onEvent(event);
        }
        List<ConfigNotificationListener> scoped = envListeners.getOrDefault(
                event.getEnvironment(), Collections.emptyList());
        for (ConfigNotificationListener listener : scoped) {
            listener.onEvent(event);
        }
    }

    /**
     * Removes all listeners (global and scoped).
     */
    public void clearAll() {
        globalListeners.clear();
        envListeners.clear();
    }

    public List<ConfigNotificationListener> getGlobalListeners() {
        return Collections.unmodifiableList(new ArrayList<>(globalListeners));
    }

    public List<ConfigNotificationListener> getEnvListeners(String environment) {
        return Collections.unmodifiableList(
                new ArrayList<>(envListeners.getOrDefault(environment, Collections.emptyList())));
    }
}
