package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTagManagerTest {

    private ConfigTagManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConfigTagManager();
    }

    @Test
    void addAndRetrieveTag() {
        manager.addTag("prod", "critical");
        assertTrue(manager.hasTag("prod", "critical"));
    }

    @Test
    void addMultipleTagsToSameEnvironment() {
        manager.addTag("staging", "internal");
        manager.addTag("staging", "beta");
        Set<String> tags = manager.getTags("staging");
        assertEquals(2, tags.size());
        assertTrue(tags.contains("internal"));
        assertTrue(tags.contains("beta"));
    }

    @Test
    void removeTagReturnsTrueWhenPresent() {
        manager.addTag("dev", "experimental");
        assertTrue(manager.removeTag("dev", "experimental"));
        assertFalse(manager.hasTag("dev", "experimental"));
    }

    @Test
    void removeTagReturnsFalseWhenAbsent() {
        assertFalse(manager.removeTag("dev", "nonexistent"));
    }

    @Test
    void getEnvironmentsByTag() {
        manager.addTag("prod", "live");
        manager.addTag("prod-eu", "live");
        manager.addTag("staging", "internal");
        List<String> liveEnvs = manager.getEnvironmentsByTag("live");
        assertEquals(2, liveEnvs.size());
        assertTrue(liveEnvs.contains("prod"));
        assertTrue(liveEnvs.contains("prod-eu"));
    }

    @Test
    void clearTagsRemovesAllTagsForEnvironment() {
        manager.addTag("qa", "test");
        manager.addTag("qa", "nightly");
        manager.clearTags("qa");
        assertTrue(manager.getTags("qa").isEmpty());
    }

    @Test
    void getTaggedEnvironmentsReturnsOnlyTaggedOnes() {
        manager.addTag("prod", "live");
        Set<String> tagged = manager.getTaggedEnvironments();
        assertTrue(tagged.contains("prod"));
    }

    @Test
    void addTagThrowsOnBlankEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> manager.addTag("", "tag"));
    }

    @Test
    void addTagThrowsOnBlankTag() {
        assertThrows(IllegalArgumentException.class, () -> manager.addTag("prod", "  "));
    }

    @Test
    void getTagsReturnsUnmodifiableView() {
        manager.addTag("prod", "live");
        Set<String> tags = manager.getTags("prod");
        assertThrows(UnsupportedOperationException.class, () -> tags.add("new"));
    }
}
