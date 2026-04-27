package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for renaming config keys across one or more environments,
 * tracking renames for auditing and rollback purposes.
 */
public class ConfigKeyRenameService {

    private final ConfigAuditLog auditLog;

    public ConfigKeyRenameService(ConfigAuditLog auditLog) {
        this.auditLog = Objects.requireNonNull(auditLog, "auditLog must not be null");
    }

    /**
     * Renames a key within the given environment config.
     *
     * @param config   the environment config to mutate
     * @param oldKey   the existing key name
     * @param newKey   the desired new key name
     * @param actor    the user or system performing the rename
     * @return updated properties map with the key renamed
     * @throws KeyRenameException if oldKey does not exist or newKey already exists
     */
    public Map<String, String> renameKey(EnvironmentConfig config,
                                         String oldKey,
                                         String newKey,
                                         String actor) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(oldKey, "oldKey must not be null");
        Objects.requireNonNull(newKey, "newKey must not be null");
        Objects.requireNonNull(actor, "actor must not be null");

        Map<String, String> props = new HashMap<>(config.getProperties());

        if (!props.containsKey(oldKey)) {
            throw new KeyRenameException("Key not found: " + oldKey);
        }
        if (props.containsKey(newKey)) {
            throw new KeyRenameException("Target key already exists: " + newKey);
        }

        String value = props.remove(oldKey);
        props.put(newKey, value);

        auditLog.record(new ConfigAuditEntry(
                actor,
                config.getEnvironment(),
                "RENAME_KEY",
                oldKey + " -> " + newKey
        ));

        return props;
    }

    /**
     * Applies a batch of renames sequentially. If any rename fails the
     * operation is aborted and the exception is re-thrown.
     *
     * @param config  the environment config
     * @param renames ordered map of oldKey -> newKey pairs
     * @param actor   the user or system performing the renames
     * @return updated properties map
     */
    public Map<String, String> batchRename(EnvironmentConfig config,
                                            Map<String, String> renames,
                                            String actor) {
        Objects.requireNonNull(renames, "renames must not be null");
        EnvironmentConfig current = config;
        Map<String, String> result = new HashMap<>(config.getProperties());

        for (Map.Entry<String, String> entry : renames.entrySet()) {
            EnvironmentConfig tmp = new EnvironmentConfig(config.getEnvironment(), result);
            result = renameKey(tmp, entry.getKey(), entry.getValue(), actor);
        }
        return result;
    }
}
