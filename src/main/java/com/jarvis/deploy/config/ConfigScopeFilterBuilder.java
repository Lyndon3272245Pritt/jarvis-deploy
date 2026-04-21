package com.jarvis.deploy.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Fluent builder for constructing {@link ConfigScopeFilter} instances.
 */
public class ConfigScopeFilterBuilder {

    private String keyPrefix;
    private final Set<String> allowedEnvironments = new HashSet<>();
    private final Set<String> requiredTags = new HashSet<>();

    public static ConfigScopeFilterBuilder newBuilder() {
        return new ConfigScopeFilterBuilder();
    }

    public ConfigScopeFilterBuilder withKeyPrefix(String prefix) {
        this.keyPrefix = prefix;
        return this;
    }

    public ConfigScopeFilterBuilder allowEnvironment(String environment) {
        if (environment != null && !environment.isBlank()) {
            this.allowedEnvironments.add(environment);
        }
        return this;
    }

    public ConfigScopeFilterBuilder allowEnvironments(Set<String> environments) {
        if (environments != null) {
            this.allowedEnvironments.addAll(environments);
        }
        return this;
    }

    public ConfigScopeFilterBuilder requireTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            this.requiredTags.add(tag);
        }
        return this;
    }

    public ConfigScopeFilterBuilder requireTags(Set<String> tags) {
        if (tags != null) {
            this.requiredTags.addAll(tags);
        }
        return this;
    }

    public ConfigScopeFilter build() {
        return new ConfigScopeFilter(keyPrefix, Set.copyOf(allowedEnvironments), Set.copyOf(requiredTags));
    }
}
