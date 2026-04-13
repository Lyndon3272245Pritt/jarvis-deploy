package com.jarvis.deploy.config;

/**
 * Thrown when a deployment configuration file cannot be loaded or parsed.
 */
public class ConfigLoadException extends RuntimeException {

    public ConfigLoadException(String message) {
        super(message);
    }

    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
