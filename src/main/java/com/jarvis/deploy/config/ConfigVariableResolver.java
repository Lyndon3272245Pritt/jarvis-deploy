package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves variable references within config values using a defined variable store.
 * Supports ${VAR_NAME} syntax with optional default values via ${VAR_NAME:-default}.
 */
public class ConfigVariableResolver {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}:]+)(?::-(.*?))?\\}");
    private static final int MAX_DEPTH = 10;

    private final Map<String, String> variables;

    public ConfigVariableResolver(Map<String, String> variables) {
        if (variables == null) {
            throw new IllegalArgumentException("Variables map must not be null");
        }
        this.variables = new HashMap<>(variables);
    }

    public String resolve(String value) {
        return resolve(value, new HashSet<>(), 0);
    }

    private String resolve(String value, Set<String> resolving, int depth) {
        if (value == null) return null;
        if (depth > MAX_DEPTH) {
            throw new InterpolationException("Max variable resolution depth exceeded — possible circular reference");
        }

        Matcher matcher = VAR_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String defaultVal = matcher.group(2);

            if (resolving.contains(varName)) {
                throw new InterpolationException("Circular variable reference detected for: " + varName);
            }

            String resolved;
            if (variables.containsKey(varName)) {
                resolving.add(varName);
                resolved = resolve(variables.get(varName), resolving, depth + 1);
                resolving.remove(varName);
            } else if (defaultVal != null) {
                resolved = defaultVal;
            } else {
                throw new InterpolationException("Unresolved variable: " + varName);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public Map<String, String> resolveAll(Map<String, String> configEntries) {
        Map<String, String> resolved = new HashMap<>();
        for (Map.Entry<String, String> entry : configEntries.entrySet()) {
            resolved.put(entry.getKey(), resolve(entry.getValue()));
        }
        return resolved;
    }

    public void addVariable(String name, String value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Variable name must not be blank");
        }
        variables.put(name, value);
    }

    public Map<String, String> getVariables() {
        return Map.copyOf(variables);
    }
}
