package com.jarvis.deploy.config;

import java.nio.file.Path;

/**
 * A config loader that integrates with {@link ConfigProfileManager} to
 * automatically load and register environment configs by profile name.
 * After loading, the specified profile is activated on the manager.
 */
public class ProfileAwareConfigLoader {

    private final ConfigLoader configLoader;
    private final ConfigProfileManager profileManager;

    public ProfileAwareConfigLoader(ConfigLoader configLoader, ConfigProfileManager profileManager) {
        if (configLoader == null) throw new IllegalArgumentException("ConfigLoader must not be null");
        if (profileManager == null) throw new IllegalArgumentException("ConfigProfileManager must not be null");
        this.configLoader = configLoader;
        this.profileManager = profileManager;
    }

    /**
     * Loads a config file, registers it under the given profile name, and activates it.
     *
     * @param profileName the name to register the profile under
     * @param configPath  the path to the config file
     * @return the loaded {@link EnvironmentConfig}
     * @throws ConfigLoadException if the file cannot be loaded
     */
    public EnvironmentConfig loadAndActivate(String profileName, Path configPath) throws ConfigLoadException {
        EnvironmentConfig config = configLoader.load(configPath);
        profileManager.registerProfile(profileName, config);
        profileManager.activateProfile(profileName);
        return config;
    }

    /**
     * Loads a config file and registers it under the given profile name
     * without changing the currently active profile.
     *
     * @param profileName the name to register the profile under
     * @param configPath  the path to the config file
     * @return the loaded {@link EnvironmentConfig}
     * @throws ConfigLoadException if the file cannot be loaded
     */
    public EnvironmentConfig loadAndRegister(String profileName, Path configPath) throws ConfigLoadException {
        EnvironmentConfig config = configLoader.load(configPath);
        profileManager.registerProfile(profileName, config);
        return config;
    }

    /**
     * Returns the underlying profile manager for inspection or further manipulation.
     */
    public ConfigProfileManager getProfileManager() {
        return profileManager;
    }
}
