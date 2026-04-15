package com.jarvis.deploy.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Holds the results of a config search operation.
 */
public class ConfigSearchResult {

    private final String environment;
    private final Map<String, String> matchedEntries;
    private final int totalMatches;

    public ConfigSearchResult(String environment, Map<String, String> matchedEntries) {
        this.environment = environment;
        this.matchedEntries = Collections.unmodifiableMap(matchedEntries);
        this.totalMatches = matchedEntries.size();
    }

    public String getEnvironment() {
        return environment;
    }

    public Map<String, String> getMatchedEntries() {
        return matchedEntries;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public boolean isEmpty() {
        return matchedEntries.isEmpty();
    }

    @Override
    public String toString() {
        return "ConfigSearchResult{environment='" + environment +
                "', totalMatches=" + totalMatches + "}";
    }
}
