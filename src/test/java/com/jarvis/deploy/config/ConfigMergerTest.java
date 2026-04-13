package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMergerTest {

    private ConfigMerger merger;

    @BeforeEach
    void setUp() {
        merger = new ConfigMerger();
    }

    private EnvironmentConfig makeConfig(String name, String region, Map<String, String> props) {
        return new EnvironmentConfig(name, region, props);
    }

    @Test
    void merge_overrideKeysTakePrecedence() {
        Map<String, String> baseProps = new HashMap<>();
        baseProps.put("db.host", "localhost");
        baseProps.put("db.port", "5432");

        Map<String, String> overrideProps = new HashMap<>();
        overrideProps.put("db.host", "prod-db.example.com");

        EnvironmentConfig base = makeConfig("base", "us-east-1", baseProps);
        EnvironmentConfig override = makeConfig("prod", "us-west-2", overrideProps);

        EnvironmentConfig merged = merger.merge(base, override);

        assertEquals("prod-db.example.com", merged.getProperties().get("db.host"));
        assertEquals("5432", merged.getProperties().get("db.port"));
    }

    @Test
    void merge_overrideNameAndRegionTakePrecedence() {
        EnvironmentConfig base = makeConfig("base", "us-east-1", Collections.emptyMap());
        EnvironmentConfig override = makeConfig("prod", "eu-west-1", Collections.emptyMap());

        EnvironmentConfig merged = merger.merge(base, override);

        assertEquals("prod", merged.getName());
        assertEquals("eu-west-1", merged.getRegion());
    }

    @Test
    void merge_baseNameKeptWhenOverrideNameBlank() {
        EnvironmentConfig base = makeConfig("base", "us-east-1", Collections.emptyMap());
        EnvironmentConfig override = makeConfig("", "", Collections.emptyMap());

        EnvironmentConfig merged = merger.merge(base, override);

        assertEquals("base", merged.getName());
        assertEquals("us-east-1", merged.getRegion());
    }

    @Test
    void merge_nullBaseThrowsException() {
        EnvironmentConfig override = makeConfig("prod", "us-east-1", Collections.emptyMap());
        assertThrows(NullPointerException.class, () -> merger.merge(null, override));
    }

    @Test
    void merge_nullOverrideThrowsException() {
        EnvironmentConfig base = makeConfig("base", "us-east-1", Collections.emptyMap());
        assertThrows(NullPointerException.class, () -> merger.merge(base, null));
    }

    @Test
    void mergeAll_appliesInOrder() {
        Map<String, String> p1 = Map.of("key", "v1", "only-base", "yes");
        Map<String, String> p2 = Map.of("key", "v2");
        Map<String, String> p3 = Map.of("key", "v3");

        EnvironmentConfig c1 = makeConfig("c1", "r1", p1);
        EnvironmentConfig c2 = makeConfig("c2", "r2", p2);
        EnvironmentConfig c3 = makeConfig("c3", "r3", p3);

        EnvironmentConfig merged = merger.mergeAll(Arrays.asList(c1, c2, c3));

        assertEquals("v3", merged.getProperties().get("key"));
        assertEquals("yes", merged.getProperties().get("only-base"));
        assertEquals("c3", merged.getName());
    }

    @Test
    void mergeAll_emptyListThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> merger.mergeAll(Collections.emptyList()));
    }
}
