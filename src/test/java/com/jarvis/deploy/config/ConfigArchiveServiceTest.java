package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigArchiveServiceTest {

    private ConfigArchiveService archiveService;
    private EnvironmentConfig prodConfig;
    private EnvironmentConfig stagingConfig;

    @BeforeEach
    void setUp() {
        archiveService = new ConfigArchiveService(new ConfigArchiveIdGenerator());
        prodConfig = new EnvironmentConfig("prod",
                Map.of("db.url", "jdbc:prod", "cache.ttl", "300"));
        stagingConfig = new EnvironmentConfig("staging",
                Map.of("db.url", "jdbc:staging", "cache.ttl", "60"));
    }

    @Test
    void archive_shouldCreateEntryWithCorrectMetadata() {
        ConfigArchiveEntry entry = archiveService.archive(prodConfig, "alice", "pre-release freeze");

        assertNotNull(entry.getArchiveId());
        assertTrue(entry.getArchiveId().startsWith("archive-prod-"));
        assertEquals("prod", entry.getEnvironment());
        assertEquals("alice", entry.getArchivedBy());
        assertEquals("pre-release freeze", entry.getReason());
        assertNotNull(entry.getArchivedAt());
        assertEquals(prodConfig.getProperties(), entry.getProperties());
    }

    @Test
    void archive_propertiesShouldBeImmutableSnapshot() {
        ConfigArchiveEntry entry = archiveService.archive(prodConfig, "alice", "");
        assertThrows(UnsupportedOperationException.class,
                () -> entry.getProperties().put("new.key", "value"));
    }

    @Test
    void findById_shouldReturnEntryWhenExists() {
        ConfigArchiveEntry created = archiveService.archive(prodConfig, "bob", "manual");
        Optional<ConfigArchiveEntry> found = archiveService.findById(created.getArchiveId());
        assertTrue(found.isPresent());
        assertEquals(created.getArchiveId(), found.get().getArchiveId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        Optional<ConfigArchiveEntry> result = archiveService.findById("archive-nonexistent-abc");
        assertFalse(result.isPresent());
    }

    @Test
    void listByEnvironment_shouldReturnOnlyMatchingEntries() {
        archiveService.archive(prodConfig, "alice", "v1");
        archiveService.archive(prodConfig, "alice", "v2");
        archiveService.archive(stagingConfig, "bob", "staging-v1");

        List<ConfigArchiveEntry> prodEntries = archiveService.listByEnvironment("prod");
        assertEquals(2, prodEntries.size());
        prodEntries.forEach(e -> assertEquals("prod", e.getEnvironment()));
    }

    @Test
    void listByEnvironment_shouldBeSortedNewestFirst() throws InterruptedException {
        archiveService.archive(prodConfig, "alice", "first");
        Thread.sleep(10);
        ConfigArchiveEntry second = archiveService.archive(prodConfig, "alice", "second");

        List<ConfigArchiveEntry> entries = archiveService.listByEnvironment("prod");
        assertEquals(second.getArchiveId(), entries.get(0).getArchiveId());
    }

    @Test
    void delete_shouldRemoveEntryAndReturnTrue() {
        ConfigArchiveEntry entry = archiveService.archive(prodConfig, "alice", "");
        assertTrue(archiveService.delete(entry.getArchiveId()));
        assertFalse(archiveService.findById(entry.getArchiveId()).isPresent());
    }

    @Test
    void delete_shouldReturnFalseWhenEntryNotFound() {
        assertFalse(archiveService.delete("archive-missing-xyz"));
    }

    @Test
    void size_shouldReflectNumberOfArchivedEntries() {
        assertEquals(0, archiveService.size());
        archiveService.archive(prodConfig, "alice", "");
        archiveService.archive(stagingConfig, "bob", "");
        assertEquals(2, archiveService.size());
    }

    @Test
    void archive_shouldThrowWhenConfigIsNull() {
        assertThrows(NullPointerException.class,
                () -> archiveService.archive(null, "alice", "reason"));
    }
}
