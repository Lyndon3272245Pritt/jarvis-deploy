package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTemplateRendererTest {

    private ConfigTemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new ConfigTemplateRenderer();
    }

    @Test
    void renderWithEnvironmentConfig_replacesAllPlaceholders() {
        EnvironmentConfig config = new EnvironmentConfig("staging", Map.of(
            "host", "staging.example.com",
            "port", "8080"
        ));
        String template = "Connect to ${host}:${port}";
        String result = renderer.render(template, config);
        assertEquals("Connect to staging.example.com:8080", result);
    }

    @Test
    void renderWithEnvironmentConfig_noPlaceholders_returnsOriginal() {
        EnvironmentConfig config = new EnvironmentConfig("prod", Map.of("key", "value"));
        String template = "No placeholders here.";
        assertEquals("No placeholders here.", renderer.render(template, config));
    }

    @Test
    void renderWithEnvironmentConfig_unresolvedPlaceholder_throwsException() {
        EnvironmentConfig config = new EnvironmentConfig("dev", Map.of("host", "localhost"));
        String template = "${host}:${port}";
        TemplateRenderException ex = assertThrows(
            TemplateRenderException.class,
            () -> renderer.render(template, config)
        );
        assertTrue(ex.getMessage().contains("port"));
        assertTrue(ex.getMessage().contains("dev"));
    }

    @Test
    void renderWithMap_replacesAllPlaceholders() {
        Map<String, String> vars = new HashMap<>();
        vars.put("app", "jarvis");
        vars.put("version", "1.0.0");
        String template = "Deploying ${app} v${version}";
        assertEquals("Deploying jarvis v1.0.0", renderer.render(template, vars));
    }

    @Test
    void renderWithMap_unresolvedPlaceholder_throwsException() {
        Map<String, String> vars = Map.of("app", "jarvis");
        String template = "${app}-${env}";
        TemplateRenderException ex = assertThrows(
            TemplateRenderException.class,
            () -> renderer.render(template, vars)
        );
        assertTrue(ex.getMessage().contains("env"));
    }

    @Test
    void render_nullTemplate_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> renderer.render(null, Map.of()));
    }

    @Test
    void render_nullConfig_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> renderer.render("template", (EnvironmentConfig) null));
    }

    @Test
    void renderWithMap_emptyTemplate_returnsEmpty() {
        assertEquals("", renderer.render("", Map.of("key", "value")));
    }
}
