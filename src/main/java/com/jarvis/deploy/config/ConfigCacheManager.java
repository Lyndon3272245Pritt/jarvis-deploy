package com.jarvis.deploy.config;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages an in-memory cache for loaded {@link EnvironmentConfig} instances.
 * <p>
 * Reduces repeated disk/network I/O by storing recently loaded configs with
 * a configurable TTL (time-to-live). Entries are evicted lazily on access
 * and eagerly when {@link #invalidate(String)} or {@link #invalidateAll()} is
 * called.
 * </p>
 */
public class ConfigCacheManager {

    private final Duration ttl;
    private final int maxSize;
    private final Map<String, CacheEntry> cache;

    /**
     * Creates a new cache with the given TTL and maximum number of entries.
     *
     * @param ttl     how long a cached entry remains valid
     * @param maxSize maximum number of entries to keep; oldest entry is evicted
     *                when this limit is exceeded (LRU-style via LinkedHashMap)
     */
    public ConfigCacheManager(Duration ttl, int maxSize) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be a positive duration");
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than zero");
        }
        this.ttl = ttl;
        this.maxSize = maxSize;
        // Access-ordered LinkedHashMap for simple LRU eviction, wrapped for thread safety
        this.cache = new ConcurrentHashMap<>(
                new LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                        return size() > maxSize;
                    }
                }
        );
    }

    /**
     * Retrieves a cached config for the given environment key, if present and
     * not yet expired.
     *
     * @param environmentKey the environment identifier used as cache key
     * @return an {@link Optional} containing the cached config, or empty if
     *         absent or expired
     */
    public Optional<EnvironmentConfig> get(String environmentKey) {
        CacheEntry entry = cache.get(environmentKey);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            cache.remove(environmentKey);
            return Optional.empty();
        }
        return Optional.of(entry.config);
    }

    /**
     * Stores the given config in the cache under the specified environment key.
     *
     * @param environmentKey the environment identifier used as cache key
     * @param config         the config to cache
     */
    public void put(String environmentKey, EnvironmentConfig config) {
        if (environmentKey == null || environmentKey.isBlank()) {
            throw new IllegalArgumentException("environmentKey must not be null or blank");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        cache.put(environmentKey, new CacheEntry(config, Instant.now().plus(ttl)));
    }

    /**
     * Removes the cached entry for the given environment key, if present.
     *
     * @param environmentKey the environment identifier to invalidate
     */
    public void invalidate(String environmentKey) {
        cache.remove(environmentKey);
    }

    /** Removes all entries from the cache. */
    public void invalidateAll() {
        cache.clear();
    }

    /**
     * Returns the number of entries currently held in the cache (including
     * potentially expired ones that have not yet been lazily evicted).
     *
     * @return current cache size
     */
    public int size() {
        return cache.size();
    }

    /** Returns {@code true} if the cache contains no entries. */
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static final class CacheEntry {
        final EnvironmentConfig config;
        final Instant expiresAt;

        CacheEntry(EnvironmentConfig config, Instant expiresAt) {
            this.config = config;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
