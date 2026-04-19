package com.jarvis.deploy.config;

public class ConfigQuotaEnforcementException extends RuntimeException {

    public ConfigQuotaEnforcementException(String message) {
        super(message);
    }

    public ConfigQuotaEnforcementException(String message, Throwable cause) {
        super(message, cause);
    }
}
