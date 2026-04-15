package com.jarvis.deploy.config;

import java.util.Map;
import java.util.Objects;

/**
 * A config loader decorator that applies staged patches from a
 * {@link ConfigPatchManager} on top of configs loaded by a delegate loader.
 */
public class PatchAwareConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigPatchManager patchManager;

    public PatchAwareConfigLoader(ConfigLoader delegate, ConfigPatchManager patchManager) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.patchManager = Objects.requireNonNull(patchManager, "patchManager must not be null");
    }

    /**
     * Loads the config for the given environment and applies any staged patches.
     *
     * @param environment the target environment name
     * @return patched EnvironmentConfig
     * @throws ConfigLoadException if the underlying load fails
     */
    public EnvironmentConfig load(String environment) throws ConfigLoadException {
        Objects.requireNonNull(environment, "environment must not be null");
        EnvironmentConfig base = delegate.load(environment);
        if (!patchManager.hasPendingPatches(environment)) {
            return base;
        }
        Map<String, String> patched = patchManager.applyPatches(environment, base.getProperties());
        return new EnvironmentConfig(base.getEnvironmentName(), patched);
    }
}
