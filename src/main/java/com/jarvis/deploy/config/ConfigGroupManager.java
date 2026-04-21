package com.jarvis.deploy.config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages logical grouping of configuration keys across environments.
 *
 * <p>Groups allow operators to organize related config keys (e.g., "database",
 * "messaging", "auth") so they can be bulk-loaded, exported, or validated
 * as a cohesive unit without touching unrelated keys.
 *
 * <p>Groups are environment-scoped: the same group name may exist in multiple
 * environments with different key memberships.
 */
public class ConfigGroupManager {

    /** environment -> groupName -> set of config keys */
    private final Map<String, Map<String, Set<String>>> registry = new ConcurrentHashMap<>();

    /**
     * Creates a new, empty group for the given environment.
     *
     * @param environment the target environment (e.g., "prod", "staging")
     * @param groupName   the logical name for the group
     * @throws IllegalArgumentException if the group already exists in that environment
     */
    public void createGroup(String environment, String groupName) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(groupName, "groupName must not be null");

        Map<String, Set<String>> envGroups =
                registry.computeIfAbsent(environment, e -> new ConcurrentHashMap<>());

        if (envGroups.containsKey(groupName)) {
            throw new IllegalArgumentException(
                    "Group '" + groupName + "' already exists in environment '" + environment + "'");
        }
        envGroups.put(groupName, new LinkedHashSet<>());
    }

    /**
     * Adds one or more config keys to an existing group.
     *
     * @param environment the target environment
     * @param groupName   the group to add keys to
     * @param keys        the config keys to add
     * @throws NoSuchElementException if the group does not exist
     */
    public void addKeys(String environment, String groupName, Collection<String> keys) {
        Set<String> groupKeys = resolveGroup(environment, groupName);
        groupKeys.addAll(keys);
    }

    /**
     * Removes one or more config keys from a group.
     *
     * @param environment the target environment
     * @param groupName   the group to remove keys from
     * @param keys        the config keys to remove
     */
    public void removeKeys(String environment, String groupName, Collection<String> keys) {
        Set<String> groupKeys = resolveGroup(environment, groupName);
        groupKeys.removeAll(keys);
    }

    /**
     * Returns an unmodifiable view of the keys belonging to a group.
     *
     * @param environment the target environment
     * @param groupName   the group name
     * @return unmodifiable set of config keys
     * @throws NoSuchElementException if the group does not exist
     */
    public Set<String> getKeys(String environment, String groupName) {
        return Collections.unmodifiableSet(resolveGroup(environment, groupName));
    }

    /**
     * Lists all group names registered for a given environment.
     *
     * @param environment the target environment
     * @return unmodifiable set of group names (empty if no groups exist)
     */
    public Set<String> listGroups(String environment) {
        Map<String, Set<String>> envGroups = registry.get(environment);
        if (envGroups == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(envGroups.keySet());
    }

    /**
     * Deletes a group and all its key memberships from an environment.
     *
     * @param environment the target environment
     * @param groupName   the group to delete
     * @return {@code true} if the group existed and was removed, {@code false} otherwise
     */
    public boolean deleteGroup(String environment, String groupName) {
        Map<String, Set<String>> envGroups = registry.get(environment);
        if (envGroups == null) {
            return false;
        }
        return envGroups.remove(groupName) != null;
    }

    /**
     * Filters the provided config map to only the keys that belong to the specified group.
     *
     * @param environment the target environment
     * @param groupName   the group name
     * @param config      the full config map to filter
     * @return a new map containing only entries whose keys are in the group
     * @throws NoSuchElementException if the group does not exist
     */
    public Map<String, String> filterByGroup(String environment, String groupName,
                                              Map<String, String> config) {
        Set<String> groupKeys = resolveGroup(environment, groupName);
        return config.entrySet().stream()
                .filter(e -> groupKeys.contains(e.getKey()))
                .collect(Collectors.toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Set<String> resolveGroup(String environment, String groupName) {
        Map<String, Set<String>> envGroups = registry.get(environment);
        if (envGroups == null) {
            throw new NoSuchElementException(
                    "No groups found for environment '" + environment + "'");
        }
        Set<String> keys = envGroups.get(groupName);
        if (keys == null) {
            throw new NoSuchElementException(
                    "Group '" + groupName + "' not found in environment '" + environment + "'");
        }
        return keys;
    }
}
