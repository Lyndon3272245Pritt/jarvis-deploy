package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigVariableResolverTest {

    private Map<String, String> variables;
    private ConfigVariableResolver resolver;

    @BeforeEach
    void setUp() {
        variables = new HashMap<>();
        variables.put("HOST", "localhost");
        variables.put("PORT", "8080");
        variables.put("BASE_URL", "http://${HOST}:${PORT}");
        resolver = new ConfigVariableResolver(variables);
    }

    @Test
    void resolve_simpleVariable_returnsValue() {
        assertEquals("localhost", resolver.resolve("${HOST}"));
    }

    @Test
    void resolve_nestedVariable_resolvesTransitively() {
        assertEquals("http://localhost:8080", resolver.resolve("${BASE_URL}"));
    }

    @Test
    void resolve_withDefault_usesDefaultWhenMissing() {
        assertEquals("production", resolver.resolve("${ENV:-production}"));
    }

    @Test
    void resolve_withDefault_usesVariableWhenPresent() {
        resolver.addVariable("ENV", "staging");
        assertEquals("staging", resolver.resolve("${ENV:-production}"));
    }

    @Test
    void resolve_undefinedVariableNoDefault_throwsInterpolationException() {
        assertThrows(InterpolationException.class, () -> resolver.resolve("${UNDEFINED_VAR}"));
    }

    @Test
    void resolve_circularReference_throwsInterpolationException() {
        variables.put("A", "${B}");
        variables.put("B", "${A}");
        ConfigVariableResolver circularResolver = new ConfigVariableResolver(variables);
        assertThrows(InterpolationException.class, () -> circularResolver.resolve("${A}"));
    }

    @Test
    void resolve_nullValue_returnsNull() {
        assertNull(resolver.resolve(null));
    }

    @Test
    void resolve_noVariables_returnsOriginalString() {
        assertEquals("plain text", resolver.resolve("plain text"));
    }

    @Test
    void resolveAll_resolvesAllEntries() {
        Map<String, String> entries = new HashMap<>();
        entries.put("url", "${BASE_URL}/api");
        entries.put("host", "${HOST}");

        Map<String, String> result = resolver.resolveAll(entries);

        assertEquals("http://localhost:8080/api", result.get("url"));
        assertEquals("localhost", result.get("host"));
    }

    @Test
    void addVariable_addsAndResolves() {
        resolver.addVariable("REGION", "us-east-1");
        assertEquals("us-east-1", resolver.resolve("${REGION}"));
    }

    @Test
    void addVariable_blankName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> resolver.addVariable("  ", "value"));
    }

    @Test
    void constructor_nullMap_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigVariableResolver(null));
    }

    @Test
    void getVariables_returnsImmutableCopy() {
        Map<String, String> vars = resolver.getVariables();
        assertThrows(UnsupportedOperationException.class, () -> vars.put("NEW", "val"));
    }
}
