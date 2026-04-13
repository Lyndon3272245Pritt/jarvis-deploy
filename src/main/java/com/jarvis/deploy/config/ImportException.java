package com.jarvis.deploy.config;

/**
 * Thrown when a configuration import operation fails.
 */
public class ImportException extends Exception {

    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
