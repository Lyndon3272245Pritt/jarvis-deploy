package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages environment-level config locks to prevent concurrent modifications
 * during deployments or sensitive operations.
 */
public class ConfigLockManager {

    private static final long DEFAULT_LOCK_TTL_SECONDS = 300;

    private final Map<String, LockEntry> locks = new ConcurrentHashMap<>();
    private final long lockTtlSeconds;

    public ConfigLockManager() {
        this(DEFAULT_LOCK_TTL_SECONDS);
    }

    public ConfigLockManager(long lockTtlSeconds) {
        this.lockTtlSeconds = lockTtlSeconds;
    }

    /**
     * Attempts to acquire a lock for the given environment.
     *
     * @param environment the environment name to lock
     * @param owner       identifier of the lock owner (e.g. user or process)
     * @return true if lock was acquired, false if already locked by another owner
     */
    public boolean acquireLock(String environment, String owner) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Owner must not be blank");
        }

        evictExpiredLock(environment);

        LockEntry existing = locks.get(environment);
        if (existing != null && !existing.owner().equals(owner)) {
            return false;
        }

        locks.put(environment, new LockEntry(owner, Instant.now().plusSeconds(lockTtlSeconds)));
        return true;
    }

    /**
     * Releases the lock for the given environment if owned by the specified owner.
     *
     * @return true if lock was released, false if not owned by caller
     */
    public boolean releaseLock(String environment, String owner) {
        LockEntry entry = locks.get(environment);
        if (entry == null) {
            return false;
        }
        if (!entry.owner().equals(owner)) {
            return false;
        }
        locks.remove(environment);
        return true;
    }

    /**
     * Returns the current lock entry for the given environment, if any.
     */
    public Optional<LockEntry> getLock(String environment) {
        evictExpiredLock(environment);
        return Optional.ofNullable(locks.get(environment));
    }

    public boolean isLocked(String environment) {
        return getLock(environment).isPresent();
    }

    private void evictExpiredLock(String environment) {
        LockEntry entry = locks.get(environment);
        if (entry != null && Instant.now().isAfter(entry.expiresAt())) {
            locks.remove(environment);
        }
    }

    public record LockEntry(String owner, Instant expiresAt) {}
}
