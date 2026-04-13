package com.jarvis.deploy.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-level service that manages multiple {@link ConfigWatcher} instances
 * and reloads {@link EnvironmentConfig} when watched files change.
 */
public class ConfigWatcherService implements AutoCloseable {

    private final ConfigLoader configLoader;
    private final Map<String, EnvironmentConfig> liveConfigs = new ConcurrentHashMap<>();
    private final List<ConfigWatcher> watchers = new ArrayList<>();
    private final List<ConfigChangeListener> listeners = new ArrayList<>();

    public ConfigWatcherService(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    public void watch(String environment, Path configPath) throws IOException {
        EnvironmentConfig initial = configLoader.load(configPath);
        liveConfigs.put(environment, initial);

        ConfigWatcher watcher = new ConfigWatcher(configPath, changed -> {
            try {
                EnvironmentConfig updated = configLoader.load(changed);
                EnvironmentConfig previous = liveConfigs.put(environment, updated);
                notifyListeners(environment, previous, updated);
            } catch (ConfigLoadException e) {
                // log and keep previous config
                System.err.println("[ConfigWatcherService] Failed to reload config for "
                        + environment + ": " + e.getMessage());
            }
        });
        watcher.start();
        watchers.add(watcher);
    }

    public EnvironmentConfig getCurrent(String environment) {
        return liveConfigs.get(environment);
    }

    public void addChangeListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(String environment,
                                  EnvironmentConfig previous,
                                  EnvironmentConfig updated) {
        for (ConfigChangeListener listener : listeners) {
            listener.onConfigChanged(environment, previous, updated);
        }
    }

    @Override
    public void close() throws IOException {
        for (ConfigWatcher watcher : watchers) {
            watcher.close();
        }
        watchers.clear();
    }

    @FunctionalInterface
    public interface ConfigChangeListener {
        void onConfigChanged(String environment,
                             EnvironmentConfig previous,
                             EnvironmentConfig updated);
    }
}
