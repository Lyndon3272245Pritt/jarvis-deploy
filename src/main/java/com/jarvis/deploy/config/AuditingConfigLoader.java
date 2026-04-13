package com.jarvis.deploy.config;

import java.util.Objects;

/**
 * Decorator around {@link ConfigLoader} that records audit entries
 * whenever a configuration is loaded.
 */
public class AuditingConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigAuditLog auditLog;

    public AuditingConfigLoader(ConfigLoader delegate, ConfigAuditLog auditLog) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.auditLog = Objects.requireNonNull(auditLog, "auditLog must not be null");
    }

    /**
     * Loads an {@link EnvironmentConfig} for the given environment name,
     * recording the event in the audit log.
     *
     * @param environment the environment identifier
     * @return the loaded config
     * @throws ConfigLoadException if loading fails
     */
    public EnvironmentConfig load(String environment) throws ConfigLoadException {
        try {
            EnvironmentConfig config = delegate.load(environment);
            auditLog.record(environment, ConfigAuditEntry.Action.LOADED,
                    "Loaded " + config.getProperties().size() + " properties");
            return config;
        } catch (ConfigLoadException ex) {
            auditLog.record(environment, ConfigAuditEntry.Action.LOADED,
                    "FAILED: " + ex.getMessage());
            throw ex;
        }
    }

    public ConfigAuditLog getAuditLog() {
        return auditLog;
    }
}
