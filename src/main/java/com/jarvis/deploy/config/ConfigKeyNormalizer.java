package com.jarvis.deploy.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Normalizes config keys to a canonical form for consistent lookups
 * and cross-environment comparisons.
 */
public class ConfigKeyNormalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern NON_ALNUM_DASH_DOT = Pattern.compile("[^a-zA-Z0-9._-]");

    public enum NormalizationStrategy {
        LOWERCASE,
        UPPERCASE,
        SNAKE_CASE,
        DOT_NOTATION
    }

    private final NormalizationStrategy strategy;
    private final boolean stripLeadingTrailingDots;

    public ConfigKeyNormalizer(NormalizationStrategy strategy) {
        this.strategy = strategy;
        this.stripLeadingTrailingDots = true;
    }

    public ConfigKeyNormalizer(NormalizationStrategy strategy, boolean stripLeadingTrailingDots) {
        this.strategy = strategy;
        this.stripLeadingTrailingDots = stripLeadingTrailingDots;
    }

    public String normalize(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Config key must not be null");
        }
        String normalized = WHITESPACE.matcher(key.trim()).replaceAll("_");
        normalized = NON_ALNUM_DASH_DOT.matcher(normalized).replaceAll("_");

        switch (strategy) {
            case LOWERCASE:
                normalized = normalized.toLowerCase();
                break;
            case UPPERCASE:
                normalized = normalized.toUpperCase();
                break;
            case SNAKE_CASE:
                normalized = toSnakeCase(normalized).toLowerCase();
                break;
            case DOT_NOTATION:
                normalized = normalized.replace('_', '.').replace('-', '.').toLowerCase();
                break;
        }

        if (stripLeadingTrailingDots) {
            normalized = normalized.replaceAll("^\\.+|\\.+$", "");
        }
        return normalized;
    }

    public Map<String, String> normalizeKeys(Map<String, String> config) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            result.put(normalize(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2")
                    .replaceAll("-", "_")
                    .replaceAll("\\.+", "_");
    }

    public NormalizationStrategy getStrategy() {
        return strategy;
    }
}
