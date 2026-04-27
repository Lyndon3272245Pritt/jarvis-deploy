package com.jarvis.deploy.config;

/**
 * Thrown when a key rename operation cannot be completed.
 */
public class KeyRenameException extends RuntimeException {

    public KeyRenameException(String message) {
        super(message);
    }

    public KeyRenameException(String message, Throwable cause) {
        super(message, cause);
    }
}
