package com.jarvis.deploy.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages named configuration profiles, allowing environments to be
 * grouped and activated by profile (e.g., "dev", "staging", "prod").
 */
public class ConfigProfileManager {

    private final Map<String, EnvironmentConfig> profiles = new HashMap<>();
    private String activeProfile;

    /**
     * Registers an environment config under the given profile name.
     *
     * @param profileName the profile identifier
     * @param config      the environment configuration
     */
    public void registerProfile(String profileName, EnvironmentConfig config) {
        if (profileName == null || profileName.isBlank()) {
            throw new IllegalArgumentException("Profile name must not be null or blank");
        }
        if (config == null) {
            throw new IllegalArgumentException("EnvironmentConfig must not be null");
        }
        profiles.put(profileName, config);
    }

    /**
     * Sets the currently active profile.
     *
     * @param profileName the profile to activate
     * @throws IllegalArgumentException if the profile has not been registered
     */
    public void activateProfile(String profileName) {
        if (!profiles.containsKey(profileName)) {
            throw new IllegalArgumentException("Unknown profile: " + profileName);
        }
        this.activeProfile = profileName;
    }

    /**
     * Returns the active profile's environment config, if one is set.
     */
    public Optional<EnvironmentConfig> getActiveConfig() {
        if (activeProfile == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(profiles.get(activeProfile));
    }

    /**
     * Returns the name of the currently active profile, or empty if none.
     */
    public Optional<String> getActiveProfileName() {
        return Optional.ofNullable(activeProfile);
    }

    /**
     * Returns an unmodifiable view of all registered profile names.
     */
    public Set<String> getRegisteredProfiles() {
        return Collections.unmodifiableSet(profiles.keySet());
    }

    /**
     * Retrieves the config for a specific profile by name.
     */
    public Optional<EnvironmentConfig> getProfile(String profileName) {
        return Optional.ofNullable(profiles.get(profileName));
    }

    /**
     * Removes a profile from the registry. If it was the active profile,
     * the active profile is cleared.
     */
    public void removeProfile(String profileName) {
        profiles.remove(profileName);
        if (profileName != null && profileName.equals(activeProfile)) {
            activeProfile = null;
        }
    }
}
