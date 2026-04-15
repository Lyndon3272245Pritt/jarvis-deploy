package com.jarvis.deploy.config;

import java.util.*;

/**
 * Manages config aliases — allows registering, resolving, and removing
 * named aliases that point to environment-specific config keys.
 */
public class ConfigAliasManager {

    private final Map<String, ConfigAlias> aliases = new LinkedHashMap<>();
    private final ConfigLoader configLoader;

    public ConfigAliasManager(ConfigLoader configLoader) {
        Objects.requireNonNull(configLoader, "ConfigLoader must not be null");
        this.configLoader = configLoader;
    }

    /**
     * Registers a new alias. Throws if alias name is already taken.
     */
    public void registerAlias(String aliasName, String targetEnvironment, String targetKey, String createdBy) {
        if (aliases.containsKey(aliasName)) {
            throw new IllegalStateException("Alias already exists: " + aliasName);
        }
        ConfigAlias alias = new ConfigAlias(aliasName, targetEnvironment, targetKey, createdBy);
        aliases.put(aliasName, alias);
    }

    /**
     * Resolves the value pointed to by the alias.
     */
    public Optional<String> resolve(String aliasName) {
        ConfigAlias alias = aliases.get(aliasName);
        if (alias == null) return Optional.empty();
        try {
            EnvironmentConfig config = configLoader.load(alias.getTargetEnvironment());
            return Optional.ofNullable(config.getProperties().get(alias.getTargetKey()));
        } catch (ConfigLoadException e) {
            return Optional.empty();
        }
    }

    /**
     * Removes an alias by name. Returns true if it existed.
     */
    public boolean removeAlias(String aliasName) {
        return aliases.remove(aliasName) != null;
    }

    /**
     * Returns the alias metadata without resolving the value.
     */
    public Optional<ConfigAlias> getAlias(String aliasName) {
        return Optional.ofNullable(aliases.get(aliasName));
    }

    /**
     * Returns all registered aliases.
     */
    public List<ConfigAlias> listAliases() {
        return Collections.unmodifiableList(new ArrayList<>(aliases.values()));
    }

    /**
     * Returns all aliases targeting a specific environment.
     */
    public List<ConfigAlias> listAliasesForEnvironment(String environment) {
        List<ConfigAlias> result = new ArrayList<>();
        for (ConfigAlias alias : aliases.values()) {
            if (alias.getTargetEnvironment().equals(environment)) {
                result.add(alias);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
