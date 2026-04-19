package com.jarvis.deploy.config;

/**
 * Thrown when a clone operation would overwrite an existing environment
 * without explicit force flag.
 */
public class CloneConflictException extends RuntimeException {

    private final String targetEnvName;

    public CloneConflictException(String targetEnvName) {
        super("Environment already exists and cannot be overwritten: " + targetEnvName);
        this.targetEnvName = targetEnvName;
    }

    public String getTargetEnvName() {
        return targetEnvName;
    }
}
