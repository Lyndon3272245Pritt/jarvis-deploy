package com.jarvis.deploy.config;

/**
 * Thrown when a config value contains a placeholder that cannot be resolved
 * during interpolation.
 */
public class InterpolationException extends RuntimeException {

    public InterpolationException(String message) {
        super(message);
    }

    public InterpolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
