package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSearchQueryTest {

    @Test
    void builder_setsAllFields() {
        ConfigSearchQuery query = ConfigSearchQuery.builder()
                .environment("prod")
                .keyPattern("db.*")
                .valuePattern("localhost.*")
                .requiredTag("infra")
                .requiredTag("monitored")
                .caseSensitive(true)
                .build();

        assertEquals("prod", query.getEnvironment());
        assertEquals("db.*", query.getKeyPattern());
        assertEquals("localhost.*", query.getValuePattern());
        assertEquals(2, query.getRequiredTags().size());
        assertTrue(query.getRequiredTags().contains("infra"));
        assertTrue(query.getRequiredTags().contains("monitored"));
        assertTrue(query.isCaseSensitive());
    }

    @Test
    void builder_defaultsAreSane() {
        ConfigSearchQuery query = ConfigSearchQuery.builder()
                .environment("dev")
                .build();

        assertNull(query.getKeyPattern());
        assertNull(query.getValuePattern());
        assertTrue(query.getRequiredTags().isEmpty());
        assertFalse(query.isCaseSensitive());
    }

    @Test
    void builder_throwsOnNullTag() {
        assertThrows(NullPointerException.class, () ->
                ConfigSearchQuery.builder().requiredTag(null));
    }
}
