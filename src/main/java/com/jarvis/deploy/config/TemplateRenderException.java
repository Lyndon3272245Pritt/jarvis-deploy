package com.jarvis.deploy.config;

/**
 * Thrown when a configuration template cannot be rendered,
 * typically due to an unresolved placeholder.
 */
public class TemplateRenderException extends RuntimeException {

    public TemplateRenderException(String message) {
        super(message);
    }

    public TemplateRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
