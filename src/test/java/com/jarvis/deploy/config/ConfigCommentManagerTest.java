package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigCommentManagerTest {

    private ConfigCommentManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConfigCommentManager();
    }

    @Test
    void addAndGetComment() {
        manager.addComment("prod", "db.url", "Primary DB endpoint");
        Optional<String> comment = manager.getComment("prod", "db.url");
        assertTrue(comment.isPresent());
        assertEquals("Primary DB endpoint", comment.get());
    }

    @Test
    void getComment_missingKey_returnsEmpty() {
        Optional<String> comment = manager.getComment("prod", "nonexistent");
        assertFalse(comment.isPresent());
    }

    @Test
    void getComment_missingEnvironment_returnsEmpty() {
        Optional<String> comment = manager.getComment("staging", "db.url");
        assertFalse(comment.isPresent());
    }

    @Test
    void removeComment_existingKey_returnsTrue() {
        manager.addComment("prod", "db.url", "some comment");
        assertTrue(manager.removeComment("prod", "db.url"));
        assertFalse(manager.hasComment("prod", "db.url"));
    }

    @Test
    void removeComment_missingKey_returnsFalse() {
        assertFalse(manager.removeComment("prod", "missing.key"));
    }

    @Test
    void getCommentsForEnvironment_returnsAllComments() {
        manager.addComment("dev", "app.name", "App name");
        manager.addComment("dev", "app.port", "HTTP port");
        Map<String, String> comments = manager.getCommentsForEnvironment("dev");
        assertEquals(2, comments.size());
        assertEquals("App name", comments.get("app.name"));
        assertEquals("HTTP port", comments.get("app.port"));
    }

    @Test
    void clearEnvironmentComments_removesAll() {
        manager.addComment("prod", "key1", "c1");
        manager.addComment("prod", "key2", "c2");
        manager.clearEnvironmentComments("prod");
        assertTrue(manager.getCommentsForEnvironment("prod").isEmpty());
    }

    @Test
    void getCommentedKeys_returnsCorrectSet() {
        manager.addComment("staging", "a", "alpha");
        manager.addComment("staging", "b", "beta");
        Set<String> keys = manager.getCommentedKeys("staging");
        assertEquals(2, keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
    }

    @Test
    void addComment_nullArgs_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> manager.addComment(null, "k", "v"));
        assertThrows(IllegalArgumentException.class, () -> manager.addComment("env", null, "v"));
        assertThrows(IllegalArgumentException.class, () -> manager.addComment("env", "k", null));
    }
}
