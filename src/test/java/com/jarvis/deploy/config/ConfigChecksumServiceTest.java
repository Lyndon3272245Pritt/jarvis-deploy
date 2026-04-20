package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigChecksumServiceTest {

    private ConfigChecksumService service;

    @BeforeEach
    void setUp() {
        service = new ConfigChecksumService();
    }

    private EnvironmentConfig buildConfig(String env, Map<String, String> props) {
        EnvironmentConfig config = new EnvironmentConfig(env);
        props.forEach(config::setProperty);
        return config;
    }

    @Test
    void computeChecksum_returnsNonNullHexString() {
        EnvironmentConfig config = buildConfig("prod", Map.of("db.url", "jdbc:pg://host/db"));
        String checksum = service.computeChecksum(config);
        assertNotNull(checksum);
        assertEquals(64, checksum.length(), "SHA-256 hex should be 64 characters");
    }

    @Test
    void computeChecksum_isDeterministic() {
        EnvironmentConfig config = buildConfig("staging", Map.of("key1", "val1", "key2", "val2"));
        String first = service.computeChecksum(config);
        String second = service.computeChecksum(config);
        assertEquals(first, second);
    }

    @Test
    void computeChecksum_differsWhenPropertyChanges() {
        EnvironmentConfig original = buildConfig("dev", Map.of("timeout", "30"));
        EnvironmentConfig modified = buildConfig("dev", Map.of("timeout", "60"));
        assertNotEquals(service.computeChecksum(original), service.computeChecksum(modified));
    }

    @Test
    void computeChecksum_differsForDifferentEnvironmentNames() {
        EnvironmentConfig prod = buildConfig("prod", Map.of("key", "value"));
        EnvironmentConfig staging = buildConfig("staging", Map.of("key", "value"));
        assertNotEquals(service.computeChecksum(prod), service.computeChecksum(staging));
    }

    @Test
    void computeChecksum_throwsOnNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> service.computeChecksum(null));
    }

    @Test
    void verify_returnsTrueForMatchingChecksum() {
        EnvironmentConfig config = buildConfig("prod", Map.of("port", "8080"));
        String checksum = service.computeChecksum(config);
        assertTrue(service.verify(config, checksum));
    }

    @Test
    void verify_returnsFalseForMismatchedChecksum() {
        EnvironmentConfig config = buildConfig("prod", Map.of("port", "8080"));
        assertFalse(service.verify(config, "0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    void verify_throwsOnBlankExpectedChecksum() {
        EnvironmentConfig config = buildConfig("prod", Map.of("port", "8080"));
        assertThrows(IllegalArgumentException.class, () -> service.verify(config, "  "));
    }

    @Test
    void matches_returnsTrueForIdenticalConfigs() {
        EnvironmentConfig a = buildConfig("dev", Map.of("x", "1", "y", "2"));
        EnvironmentConfig b = buildConfig("dev", Map.of("x", "1", "y", "2"));
        assertTrue(service.matches(a, b));
    }

    @Test
    void matches_returnsFalseForDifferentConfigs() {
        EnvironmentConfig a = buildConfig("dev", Map.of("x", "1"));
        EnvironmentConfig b = buildConfig("dev", Map.of("x", "2"));
        assertFalse(service.matches(a, b));
    }
}
