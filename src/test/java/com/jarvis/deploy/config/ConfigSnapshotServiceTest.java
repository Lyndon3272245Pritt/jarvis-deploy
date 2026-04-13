package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSnapshotServiceTest {

    private ConfigSnapshotService service;
    private EnvironmentConfig prodConfig;
    private EnvironmentConfig stagingConfig;

    @BeforeEach
    void setUp() {
        service = new ConfigSnapshotService();

        Map<String, String> prodProps = new HashMap<>();
        prodProps.put("db.url", "jdbc:prod");
        prodProps.put("cache.ttl", "3600");
        prodConfig = new EnvironmentConfig("prod", prodProps);

        Map<String, String> stagingProps = new HashMap<>();
        stagingProps.put("db.url", "jdbc:staging");
        stagingConfig = new EnvironmentConfig("staging", stagingProps);
    }

    @Test
    void capture_shouldReturnSnapshotWithCorrectEnvironmentAndProperties() {
        ConfigSnapshot snapshot = service.capture(prodConfig);

        assertNotNull(snapshot);
        assertEquals("prod", snapshot.getEnvironment());
        assertEquals("jdbc:prod", snapshot.getProperties().get("db.url"));
        assertNotNull(snapshot.getSnapshotId());
        assertNotNull(snapshot.getCapturedAt());
    }

    @Test
    void capture_shouldThrowWhenConfigIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.capture(null));
    }

    @Test
    void findById_shouldReturnSnapshotWhenExists() {
        ConfigSnapshot snapshot = service.capture(prodConfig);
        Optional<ConfigSnapshot> found = service.findById(snapshot.getSnapshotId());

        assertTrue(found.isPresent());
        assertEquals(snapshot.getSnapshotId(), found.get().getSnapshotId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        Optional<ConfigSnapshot> found = service.findById("non-existent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void listByEnvironment_shouldReturnOnlyMatchingEnvironment() {
        service.capture(prodConfig);
        service.capture(prodConfig);
        service.capture(stagingConfig);

        List<ConfigSnapshot> prodSnapshots = service.listByEnvironment("prod");
        assertEquals(2, prodSnapshots.size());
        prodSnapshots.forEach(s -> assertEquals("prod", s.getEnvironment()));
    }

    @Test
    void delete_shouldRemoveSnapshotSuccessfully() {
        ConfigSnapshot snapshot = service.capture(prodConfig);
        boolean deleted = service.delete(snapshot.getSnapshotId());

        assertTrue(deleted);
        assertFalse(service.findById(snapshot.getSnapshotId()).isPresent());
    }

    @Test
    void delete_shouldReturnFalseForNonExistentSnapshot() {
        assertFalse(service.delete("ghost-id"));
    }

    @Test
    void listAll_shouldReturnAllSnapshots() {
        service.capture(prodConfig);
        service.capture(stagingConfig);

        assertEquals(2, service.listAll().size());
    }
}
