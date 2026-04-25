package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigKeyNormalizerTest {

    @Test
    void normalizeLowercase() {
        ConfigKeyNormalizer normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.LOWERCASE);
        assertEquals("db_host", normalizer.normalize("DB_HOST"));
        assertEquals("app.port", normalizer.normalize("APP.PORT"));
    }

    @Test
    void normalizeUppercase() {
        ConfigKeyNormalizer normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.UPPERCASE);
        assertEquals("DB_HOST", normalizer.normalize("db_host"));
    }

    @ParameterizedTest
    @CsvSource({
        "dbHost, db_host",
        "APP-URL, app_url",
        "some.key, some_key"
    })
    void normalizeSnakeCase(String input, String expected) {
        ConfigKeyNormalizer normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.SNAKE_CASE);
        assertEquals(expected, normalizer.normalize(input));
    }

    @Test
    void normalizeDotNotation() {
        ConfigKeyNormalizer normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.DOT_NOTATION);
        assertEquals("db.host", normalizer.normalize("DB_HOST"));
        assertEquals("app.server.port", normalizer.normalize("APP_SERVER_PORT"));
    }

    @Test
    void normalizeTrimsWhitespace() {
        ConfigKeyNormalizer normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.LOWERCASE);
        assertEquals("db_host", normalizer.normalize("  db host  "));
    }

    @Test
    void normalizeNullThrows() {
        ConfigKeyNormalizer normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.LOWERCASE);
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(null));
    }

    @Test
    void normalizeKeysMap() {
        ConfigKeyNormalizer normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.LOWERCASE);
        Map<String, String> input = new LinkedHashMap<>();
        input.put("DB_HOST", "localhost");
        input.put("APP_PORT", "8080");
        Map<String, String> result = normalizer.normalizeKeys(input);
        assertTrue(result.containsKey("db_host"));
        assertTrue(result.containsKey("app_port"));
        assertEquals("localhost", result.get("db_host"));
    }

    @Test
    void stripsLeadingAndTrailingDots() {
        ConfigKeyNormalizer normalizer = new ConfigKeyNormalizer(ConfigKeyNormalizer.NormalizationStrategy.DOT_NOTATION);
        String result = normalizer.normalize("_KEY_");
        assertFalse(result.startsWith("."), "Should not start with dot");
        assertFalse(result.endsWith("."), "Should not end with dot");
    }
}
