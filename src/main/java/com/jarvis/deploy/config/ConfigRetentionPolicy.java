package com.jarvis.deploy.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines retention rules for config snapshots and versions.
 */
public class ConfigRetentionPolicy {

    private final String environment;
    private final int maxVersions;
    private final Duration maxAge;
    private final boolean keepTagged;

    public ConfigRetentionPolicy(String environment, int maxVersions, Duration maxAge, boolean keepTagged) {
        if (environment == null || environment.isBlank()) throw new IllegalArgumentException("Environment must not be blank");
        if (maxVersions < 1) throw new IllegalArgumentException("maxVersions must be >= 1");
        Objects.requireNonNull(maxAge, "maxAge must not be null");
        this.environment = environment;
        this.maxVersions = maxVersions;
        this.maxAge = maxAge;
        this.keepTagged = keepTagged;
    }

    public String getEnvironment() { return environment; }
    public int getMaxVersions() { return maxVersions; }
    public Duration getMaxAge() { return maxAge; }
    public boolean isKeepTagged() { return keepTagged; }

    @Override
    public String toString() {
        return "ConfigRetentionPolicy{env=" + environment +
               ", maxVersions=" + maxVersions +
               ", maxAge=" + maxAge +
               ", keepTagged=" + keepTagged + "}";
    }
}
