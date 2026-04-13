package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders configuration templates by substituting placeholders with values
 * from an EnvironmentConfig. Placeholders use the syntax: ${key}
 */
public class ConfigTemplateRenderer {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Renders a template string by replacing all ${key} placeholders
     * with corresponding values from the provided environment config.
     *
     * @param template the template string containing placeholders
     * @param config   the environment config providing substitution values
     * @return the rendered string with all placeholders replaced
     * @throws TemplateRenderException if any placeholder cannot be resolved
     */
    public String render(String template, EnvironmentConfig config) {
        if (template == null) {
            throw new IllegalArgumentException("Template must not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("EnvironmentConfig must not be null");
        }

        Map<String, String> properties = config.getProperties();
        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = properties.get(key);
            if (value == null) {
                throw new TemplateRenderException(
                    "Unresolved placeholder '" + key + "' in template for environment: "
                    + config.getEnvironmentName()
                );
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Renders a template string using an explicit map of variables,
     * ignoring any EnvironmentConfig.
     *
     * @param template  the template string containing placeholders
     * @param variables a map of key-value substitutions
     * @return the rendered string
     * @throws TemplateRenderException if any placeholder cannot be resolved
     */
    public String render(String template, Map<String, String> variables) {
        if (template == null) throw new IllegalArgumentException("Template must not be null");
        if (variables == null) throw new IllegalArgumentException("Variables map must not be null");

        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.get(key);
            if (value == null) {
                throw new TemplateRenderException("Unresolved placeholder '" + key + "'");
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
