package com.jarvis.deploy.config;

import java.util.*;

/**
 * Manages logical groupings of configuration keys across environments.
 * Groups allow bulk operations (export, diff, promote) on related keys.
 */
public class ConfigGroupManager {

    private final Map<String, Set<String>> groups = new LinkedHashMap<>();
    private final Map<String, String> keyToGroup = new HashMap<>();

    public void createGroup(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name must not be blank");
        }
        groups.putIfAbsent(groupName, new LinkedHashSet<>());
    }

    public void addKeyToGroup(String groupName, String key) {
        if (!groups.containsKey(groupName)) {
            throw new IllegalArgumentException("Group not found: " + groupName);
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key must not be blank");
        }
        groups.get(groupName).add(key);
        keyToGroup.put(key, groupName);
    }

    public void removeKeyFromGroup(String groupName, String key) {
        Set<String> keys = groups.get(groupName);
        if (keys != null) {
            keys.remove(key);
            keyToGroup.remove(key);
        }
    }

    public void deleteGroup(String groupName) {
        Set<String> keys = groups.remove(groupName);
        if (keys != null) {
            keys.forEach(keyToGroup::remove);
        }
    }

    public Set<String> getKeysInGroup(String groupName) {
        return Collections.unmodifiableSet(
            groups.getOrDefault(groupName, Collections.emptySet())
        );
    }

    public Optional<String> getGroupForKey(String key) {
        return Optional.ofNullable(keyToGroup.get(key));
    }

    public Set<String> getAllGroups() {
        return Collections.unmodifiableSet(groups.keySet());
    }

    public boolean groupExists(String groupName) {
        return groups.containsKey(groupName);
    }

    public Map<String, Set<String>> snapshot() {
        Map<String, Set<String>> copy = new LinkedHashMap<>();
        groups.forEach((g, keys) -> copy.put(g, new LinkedHashSet<>(keys)));
        return Collections.unmodifiableMap(copy);
    }

    public Map<String, String> filterByGroup(String groupName, Map<String, String> config) {
        Set<String> keys = groups.getOrDefault(groupName, Collections.emptySet());
        Map<String, String> result = new LinkedHashMap<>();
        keys.forEach(k -> {
            if (config.containsKey(k)) {
                result.put(k, config.get(k));
            }
        });
        return Collections.unmodifiableMap(result);
    }
}