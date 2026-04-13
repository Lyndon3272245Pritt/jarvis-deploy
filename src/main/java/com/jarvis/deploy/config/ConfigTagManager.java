package com.jarvis.deploy.config;

import java.util.*;

/**
 * Manages tags associated with environment configurations,
 * allowing grouping and filtering by arbitrary labels.
 */
public class ConfigTagManager {

    private final Map<String, Set<String>> envTags = new HashMap<>();

    /**
     * Adds a tag to the given environment.
     */
    public void addTag(String environment, String tag) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Tag must not be blank");
        }
        envTags.computeIfAbsent(environment, k -> new LinkedHashSet<>()).add(tag);
    }

    /**
     * Removes a tag from the given environment. Returns true if the tag was present.
     */
    public boolean removeTag(String environment, String tag) {
        Set<String> tags = envTags.get(environment);
        if (tags == null) return false;
        return tags.remove(tag);
    }

    /**
     * Returns all tags for a given environment.
     */
    public Set<String> getTags(String environment) {
        return Collections.unmodifiableSet(envTags.getOrDefault(environment, Collections.emptySet()));
    }

    /**
     * Returns all environments that have the specified tag.
     */
    public List<String> getEnvironmentsByTag(String tag) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : envTags.entrySet()) {
            if (entry.getValue().contains(tag)) {
                result.add(entry.getKey());
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns true if the environment has the given tag.
     */
    public boolean hasTag(String environment, String tag) {
        return envTags.getOrDefault(environment, Collections.emptySet()).contains(tag);
    }

    /**
     * Clears all tags for the given environment.
     */
    public void clearTags(String environment) {
        envTags.remove(environment);
    }

    /**
     * Returns all known environments that have at least one tag.
     */
    public Set<String> getTaggedEnvironments() {
        return Collections.unmodifiableSet(envTags.keySet());
    }
}
