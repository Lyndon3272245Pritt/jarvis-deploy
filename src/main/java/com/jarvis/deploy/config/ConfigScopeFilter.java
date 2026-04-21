package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Filters configuration entries based on a defined scope (e.g., key prefix, environment, tags).
 */
public class ConfigScopeFilter {

    private final String keyPrefix;
    private final Set<String> allowedEnvironments;
    private final Set<String> requiredTags;

    public ConfigScopeFilter(String keyPrefix, Set<String> allowedEnvironments, Set<String> requiredTags) {
        this.keyPrefix = keyPrefix;
        this.allowedEnvironments = allowedEnvironments;
        this.requiredTags = requiredTags;
    }

    /**
     * Filters properties map by key prefix.
     */
    public Map<String, String> filterByPrefix(Map<String, String> properties) {
        if (keyPrefix == null || keyPrefix.isBlank()) {
            return new HashMap<>(properties);
        }
        return properties.entrySet().stream()
                .filter(e -> e.getKey().startsWith(keyPrefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Returns true if the given environment name is within scope.
     */
    public boolean isEnvironmentInScope(String environment) {
        if (allowedEnvironments == null || allowedEnvironments.isEmpty()) {
            return true;
        }
        return allowedEnvironments.contains(environment);
    }

    /**
     * Returns true if the given set of tags satisfies the required tags for this scope.
     */
    public boolean matchesTags(Set<String> tags) {
        if (requiredTags == null || requiredTags.isEmpty()) {
            return true;
        }
        return tags != null && tags.containsAll(requiredTags);
    }

    /**
     * Applies a custom predicate filter on top of prefix filtering.
     */
    public Map<String, String> filterWithPredicate(Map<String, String> properties, Predicate<Map.Entry<String, String>> predicate) {
        return filterByPrefix(properties).entrySet().stream()
                .filter(predicate)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public String getKeyPrefix() { return keyPrefix; }
    public Set<String> getAllowedEnvironments() { return allowedEnvironments; }
    public Set<String> getRequiredTags() { return requiredTags; }
}
