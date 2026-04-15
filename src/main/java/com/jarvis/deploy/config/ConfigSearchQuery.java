package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a search query for filtering environment configs by key, value, or tags.
 */
public class ConfigSearchQuery {

    private String keyPattern;
    private String valuePattern;
    private List<String> requiredTags;
    private String environment;
    private boolean caseSensitive;

    private ConfigSearchQuery() {
        this.requiredTags = new ArrayList<>();
        this.caseSensitive = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getKeyPattern() { return keyPattern; }
    public String getValuePattern() { return valuePattern; }
    public List<String> getRequiredTags() { return requiredTags; }
    public String getEnvironment() { return environment; }
    public boolean isCaseSensitive() { return caseSensitive; }

    public static class Builder {
        private final ConfigSearchQuery query = new ConfigSearchQuery();

        public Builder keyPattern(String keyPattern) {
            query.keyPattern = keyPattern;
            return this;
        }

        public Builder valuePattern(String valuePattern) {
            query.valuePattern = valuePattern;
            return this;
        }

        public Builder requiredTag(String tag) {
            query.requiredTags.add(Objects.requireNonNull(tag, "tag must not be null"));
            return this;
        }

        public Builder environment(String environment) {
            query.environment = environment;
            return this;
        }

        public Builder caseSensitive(boolean caseSensitive) {
            query.caseSensitive = caseSensitive;
            return this;
        }

        public ConfigSearchQuery build() {
            return query;
        }
    }
}
