package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpolates placeholder expressions (e.g. ${key}) inside config values
 * using a provided variable map, with optional fallback to system properties.
 */
public class ConfigValueInterpolator {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private final boolean fallbackToSystemProperties;

    public ConfigValueInterpolator(boolean fallbackToSystemProperties) {
        this.fallbackToSystemProperties = fallbackToSystemProperties;
    }

    public ConfigValueInterpolator() {
        this(false);
    }

    /**
     * Interpolates all values in the given config map using the same map as the
     * variable source, plus optionally system properties.
     */
    public Map<String, String> interpolate(Map<String, String> config) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            result.put(entry.getKey(), resolveValue(entry.getValue(), config));
        }
        return result;
    }

    /**
     * Interpolates a single value string against the provided variable map.
     */
    public String resolveValue(String value, Map<String, String> variables) {
        if (value == null) {
            return null;
        }
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = variables.getOrDefault(key, null);
            if (replacement == null && fallbackToSystemProperties) {
                replacement = System.getProperty(key);
            }
            if (replacement == null) {
                throw new InterpolationException(
                        "Unresolved placeholder '" + key + "' in value: " + value);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
