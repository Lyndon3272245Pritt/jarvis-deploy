package com.jarvis.deploy.config;

import java.util.*;

/**
 * Manages inline comments/annotations attached to config keys per environment.
 */
public class ConfigCommentManager {

    // environmentName -> key -> comment
    private final Map<String, Map<String, String>> comments = new HashMap<>();

    public void addComment(String environment, String key, String comment) {
        if (environment == null || key == null || comment == null) {
            throw new IllegalArgumentException("Environment, key, and comment must not be null");
        }
        comments
            .computeIfAbsent(environment, e -> new LinkedHashMap<>())
            .put(key, comment);
    }

    public Optional<String> getComment(String environment, String key) {
        if (environment == null || key == null) return Optional.empty();
        Map<String, String> envComments = comments.get(environment);
        if (envComments == null) return Optional.empty();
        return Optional.ofNullable(envComments.get(key));
    }

    public Map<String, String> getCommentsForEnvironment(String environment) {
        return Collections.unmodifiableMap(
            comments.getOrDefault(environment, Collections.emptyMap())
        );
    }

    public boolean removeComment(String environment, String key) {
        Map<String, String> envComments = comments.get(environment);
        if (envComments == null) return false;
        return envComments.remove(key) != null;
    }

    public void clearEnvironmentComments(String environment) {
        comments.remove(environment);
    }

    public Set<String> getCommentedKeys(String environment) {
        return Collections.unmodifiableSet(
            new LinkedHashSet<>(comments.getOrDefault(environment, Collections.emptyMap()).keySet())
        );
    }

    public boolean hasComment(String environment, String key) {
        return getComment(environment, key).isPresent();
    }
}
