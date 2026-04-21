package com.jarvis.deploy.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Masks sensitive config values (e.g. passwords, tokens, secrets)
 * before they are displayed in logs, reports, or exports.
 */
public class ConfigSecretMaskingService {

    private static final String MASK = "****";

    private final Set<String> sensitiveKeyPatterns;

    public ConfigSecretMaskingService() {
        this.sensitiveKeyPatterns = new HashSet<>();
        // Default patterns considered sensitive
        sensitiveKeyPatterns.add("(?i).*password.*");
        sensitiveKeyPatterns.add("(?i).*secret.*");
        sensitiveKeyPatterns.add("(?i).*token.*");
        sensitiveKeyPatterns.add("(?i).*api[_\\-]?key.*");
        sensitiveKeyPatterns.add("(?i).*private[_\\-]?key.*");
        sensitiveKeyPatterns.add("(?i).*credential.*");
    }

    public ConfigSecretMaskingService(Set<String> additionalPatterns) {
        this();
        if (additionalPatterns != null) {
            sensitiveKeyPatterns.addAll(additionalPatterns);
        }
    }

    /**
     * Returns a copy of the config map with sensitive values masked.
     */
    public Map<String, String> mask(Map<String, String> config) {
        if (config == null) {
            return Collections.emptyMap();
        }
        Map<String, String> masked = new HashMap<>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            if (isSensitive(entry.getKey())) {
                masked.put(entry.getKey(), MASK);
            } else {
                masked.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(masked);
    }

    /**
     * Returns the masked value for a single key/value pair.
     */
    public String maskValue(String key, String value) {
        return isSensitive(key) ? MASK : value;
    }

    public boolean isSensitive(String key) {
        if (key == null) return false;
        for (String pattern : sensitiveKeyPatterns) {
            if (Pattern.matches(pattern, key)) {
                return true;
            }
        }
        return false;
    }

    public void registerPattern(String pattern) {
        if (pattern != null && !pattern.isBlank()) {
            sensitiveKeyPatterns.add(pattern);
        }
    }

    public Set<String> getPatterns() {
        return Collections.unmodifiableSet(sensitiveKeyPatterns);
    }
}
