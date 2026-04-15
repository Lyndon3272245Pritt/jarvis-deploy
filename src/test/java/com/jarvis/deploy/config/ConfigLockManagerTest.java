package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLockManagerTest {

    private ConfigLockManager lockManager;

    @BeforeEach
    void setUp() {
        lockManager = new ConfigLockManager(60);
    }

    @Test
    void acquireLock_succeeds_whenEnvironmentIsUnlocked() {
        assertTrue(lockManager.acquireLock("production", "alice"));
        assertTrue(lockManager.isLocked("production"));
    }

    @Test
    void acquireLock_succeeds_whenSameOwnerReacquires() {
        lockManager.acquireLock("staging", "alice");
        assertTrue(lockManager.acquireLock("staging", "alice"));
    }

    @Test
    void acquireLock_fails_whenLockedByDifferentOwner() {
        lockManager.acquireLock("production", "alice");
        assertFalse(lockManager.acquireLock("production", "bob"));
    }

    @Test
    void releaseLock_succeeds_whenOwnerMatches() {
        lockManager.acquireLock("production", "alice");
        assertTrue(lockManager.releaseLock("production", "alice"));
        assertFalse(lockManager.isLocked("production"));
    }

    @Test
    void releaseLock_fails_whenOwnerDoesNotMatch() {
        lockManager.acquireLock("production", "alice");
        assertFalse(lockManager.releaseLock("production", "bob"));
        assertTrue(lockManager.isLocked("production"));
    }

    @Test
    void releaseLock_returnsFalse_whenNoLockExists() {
        assertFalse(lockManager.releaseLock("production", "alice"));
    }

    @Test
    void getLock_returnsEmpty_whenNotLocked() {
        assertEquals(Optional.empty(), lockManager.getLock("dev"));
    }

    @Test
    void getLock_returnsEntry_withCorrectOwner() {
        lockManager.acquireLock("dev", "ci-bot");
        Optional<ConfigLockManager.LockEntry> lock = lockManager.getLock("dev");
        assertTrue(lock.isPresent());
        assertEquals("ci-bot", lock.get().owner());
    }

    @Test
    void lock_isEvicted_afterTtlExpires() throws InterruptedException {
        ConfigLockManager shortTtlManager = new ConfigLockManager(0);
        shortTtlManager.acquireLock("qa", "alice");
        Thread.sleep(50);
        assertFalse(shortTtlManager.isLocked("qa"));
    }

    @Test
    void acquireLock_throwsException_onBlankEnvironment() {
        assertThrows(IllegalArgumentException.class,
                () -> lockManager.acquireLock("", "alice"));
    }

    @Test
    void acquireLock_throwsException_onBlankOwner() {
        assertThrows(IllegalArgumentException.class,
                () -> lockManager.acquireLock("production", " "));
    }

    @Test
    void independentEnvironments_doNotConflict() {
        assertTrue(lockManager.acquireLock("production", "alice"));
        assertTrue(lockManager.acquireLock("staging", "bob"));
        assertTrue(lockManager.isLocked("production"));
        assertTrue(lockManager.isLocked("staging"));
    }
}
