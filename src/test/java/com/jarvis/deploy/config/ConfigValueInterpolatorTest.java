package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValueInterpolatorTest {

    private ConfigValueInterpolator interpolator;

    @BeforeEach
    void setUp() {
        interpolator = new ConfigValueInterpolator();
    }

    @Test
    void interpolate_replacesPlaceholderWithValue() {
        Map<String, String> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("url", "http://${host}:8080");

        Map<String, String> result = interpolator.interpolate(config);

        assertEquals("http://localhost:8080", result.get("url"));
    }

    @Test
    void interpolate_multipleReferencesInSingleValue() {
        Map<String, String> config = new HashMap<>();
        config.put("scheme", "https");
        config.put("host", "example.com");
        config.put("port", "443");
        config.put("endpoint", "${scheme}://${host}:${port}/api");

        Map<String, String> result = interpolator.interpolate(config);

        assertEquals("https://example.com:443/api", result.get("endpoint"));
    }

    @Test
    void interpolate_noPlaceholders_returnsOriginal() {
        Map<String, String> config = new HashMap<>();
        config.put("key", "plain-value");

        Map<String, String> result = interpolator.interpolate(config);

        assertEquals("plain-value", result.get("key"));
    }

    @Test
    void interpolate_throwsOnUnresolved() {
        Map<String, String> config = new HashMap<>();
        config.put("url", "http://${missing}:8080");

        assertThrows(InterpolationException.class, () -> interpolator.interpolate(config));
    }

    @Test
    void interpolate_nullValue_returnsNull() {
        Map<String, String> vars = new HashMap<>();
        assertNull(interpolator.resolveValue(null, vars));
    }

    @Test
    void interpolate_fallbackToSystemProperty() {
        System.setProperty("jarvis.test.prop", "sys-value");
        try {
            ConfigValueInterpolator sysInterpolator = new ConfigValueInterpolator(true);
            Map<String, String> vars = new HashMap<>();
            String result = sysInterpolator.resolveValue("prefix-${jarvis.test.prop}-suffix", vars);
            assertEquals("prefix-sys-value-suffix", result);
        } finally {
            System.clearProperty("jarvis.test.prop");
        }
    }

    @Test
    void interpolate_chainedReferences_resolvedIndependently() {
        Map<String, String> config = new HashMap<>();
        config.put("base", "prod");
        config.put("env", "${base}");
        // env itself is resolved against original map, not re-interpolated result
        Map<String, String> result = interpolator.interpolate(config);
        assertEquals("prod", result.get("env"));
    }
}
