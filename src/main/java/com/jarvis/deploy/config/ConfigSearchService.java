package com.jarvis.deploy.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Service for searching config entries across environments using flexible query criteria.
 */
public class ConfigSearchService {

    private final ConfigLoader configLoader;
    private final ConfigTagManager tagManager;

    public ConfigSearchService(ConfigLoader configLoader, ConfigTagManager tagManager) {
        this.configLoader = Objects.requireNonNull(configLoader, "configLoader must not be null");
        this.tagManager = Objects.requireNonNull(tagManager, "tagManager must not be null");
    }

    public ConfigSearchResult search(ConfigSearchQuery query) throws ConfigLoadException {
        Objects.requireNonNull(query, "query must not be null");

        String env = query.getEnvironment();
        if (env == null || env.isBlank()) {
            throw new IllegalArgumentException("Environment must be specified in search query");
        }

        EnvironmentConfig config = configLoader.load(env);
        Map<String, String> properties = config.getProperties();
        Map<String, String> matched = new LinkedHashMap<>();

        int flags = query.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE;
        Pattern keyPattern = compilePattern(query.getKeyPattern(), flags);
        Pattern valuePattern = compilePattern(query.getValuePattern(), flags);

        List<String> requiredTags = query.getRequiredTags();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (keyPattern != null && !keyPattern.matcher(key).find()) continue;
            if (valuePattern != null && !valuePattern.matcher(value).find()) continue;
            if (!requiredTags.isEmpty()) {
                List<String> keyTags = tagManager.getTagsForKey(env, key);
                if (!keyTags.containsAll(requiredTags)) continue;
            }

            matched.put(key, value);
        }

        return new ConfigSearchResult(env, matched);
    }

    private Pattern compilePattern(String pattern, int flags) {
        if (pattern == null || pattern.isBlank()) return null;
        try {
            return Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid search pattern: " + pattern, e);
        }
    }
}
