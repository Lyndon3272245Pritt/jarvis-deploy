package com.jarvis.deploy.config;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Applies retention policies to prune old config versions.
 */
public class ConfigRetentionService {

    private final ConfigVersionRegistry versionRegistry;
    private final ConfigTagManager tagManager;
    private final Map<String, ConfigRetentionPolicy> policies = new HashMap<>();

    public ConfigRetentionService(ConfigVersionRegistry versionRegistry, ConfigTagManager tagManager) {
        this.versionRegistry = Objects.requireNonNull(versionRegistry);
        this.tagManager = Objects.requireNonNull(tagManager);
    }

    public void registerPolicy(ConfigRetentionPolicy policy) {
        policies.put(policy.getEnvironment(), policy);
    }

    public List<String> applyPolicy(String environment) {
        ConfigRetentionPolicy policy = policies.get(environment);
        if (policy == null) return Collections.emptyList();

        List<String> allVersions = versionRegistry.listVersions(environment);
        Instant cutoff = Instant.now().minus(policy.getMaxAge());
        Set<String> taggedVersions = policy.isKeepTagged()
                ? new HashSet<>(tagManager.getTaggedVersions(environment))
                : Collections.emptySet();

        List<String> pruned = new ArrayList<>();
        List<String> retained = new ArrayList<>();

        for (String version : allVersions) {
            if (policy.isKeepTagged() && taggedVersions.contains(version)) {
                retained.add(version);
                continue;
            }
            Instant createdAt = versionRegistry.getVersionTimestamp(environment, version);
            if (createdAt != null && createdAt.isBefore(cutoff)) {
                pruned.add(version);
            } else {
                retained.add(version);
            }
        }

        // Also prune if over maxVersions (oldest first, already excluded tagged)
        while (retained.size() > policy.getMaxVersions()) {
            String oldest = retained.remove(0);
            if (!taggedVersions.contains(oldest)) {
                pruned.add(oldest);
            }
        }

        pruned.forEach(v -> versionRegistry.deleteVersion(environment, v));
        return pruned;
    }

    public Optional<ConfigRetentionPolicy> getPolicy(String environment) {
        return Optional.ofNullable(policies.get(environment));
    }
}
