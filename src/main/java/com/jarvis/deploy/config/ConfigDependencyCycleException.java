package com.jarvis.deploy.config;

/**
 * Thrown when a circular dependency is detected in the config dependency graph.
 */
public class ConfigDependencyCycleException extends RuntimeException {

    public ConfigDependencyCycleException(String message) {
        super(message);
    }

    public ConfigDependencyCycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
