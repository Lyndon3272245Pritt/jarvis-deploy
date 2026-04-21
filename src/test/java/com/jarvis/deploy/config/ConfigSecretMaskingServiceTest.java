package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSecretMaskingServiceTest {

    private ConfigSecretMaskingService service;

    @BeforeEach
    void setUp() {
        service = new ConfigSecretMaskingService();
    }

    @Test
    void maskSensitiveKeys_replacesWithAsterisks() {
        Map<String, String> config = new HashMap<>();
        config.put("db.password", "supersecret");
        config.put("app.name", "jarvis");
        config.put("auth.token", "abc123");
        config.put("api_key", "key-xyz");

        Map<String, String> masked = service.mask(config);

        assertEquals("****", masked.get("db.password"));
        assertEquals("jarvis", masked.get("app.name"));
        assertEquals("****", masked.get("auth.token"));
        assertEquals("****", masked.get("api_key"));
    }

    @Test
    void mask_nullInput_returnsEmptyMap() {
        Map<String, String> result = service.mask(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mask_returnsUnmodifiableMap() {
        Map<String, String> config = Map.of("host", "localhost");
        Map<String, String> result = service.mask(config);
        assertThrows(UnsupportedOperationException.class, () -> result.put("x", "y"));
    }

    @Test
    void isSensitive_detectsKnownPatterns() {
        assertTrue(service.isSensitive("db.password"));
        assertTrue(service.isSensitive("AUTH_SECRET"));
        assertTrue(service.isSensitive("private_key"));
        assertTrue(service.isSensitive("CREDENTIAL_STORE"));
        assertFalse(service.isSensitive("app.port"));
        assertFalse(service.isSensitive("database.host"));
    }

    @Test
    void isSensitive_nullKey_returnsFalse() {
        assertFalse(service.isSensitive(null));
    }

    @Test
    void maskValue_sensitiveSingleEntry() {
        assertEquals("****", service.maskValue("api-key", "my-key"));
        assertEquals("localhost", service.maskValue("db.host", "localhost"));
    }

    @Test
    void registerPattern_customPatternIsApplied() {
        service.registerPattern("(?i).*internal.*");
        assertTrue(service.isSensitive("internal_url"));
        assertEquals("****", service.maskValue("internal_url", "http://internal"));
    }

    @Test
    void registerPattern_blankPattern_ignored() {
        int before = service.getPatterns().size();
        service.registerPattern("  ");
        assertEquals(before, service.getPatterns().size());
    }

    @Test
    void constructor_withAdditionalPatterns_mergesDefaults() {
        Set<String> extra = Set.of("(?i).*vault.*");
        ConfigSecretMaskingService custom = new ConfigSecretMaskingService(extra);
        assertTrue(custom.isSensitive("vault_path"));
        assertTrue(custom.isSensitive("db.password")); // default still active
    }
}
