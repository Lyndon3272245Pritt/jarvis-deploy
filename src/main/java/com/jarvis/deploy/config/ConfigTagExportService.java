package com.jarvis.deploy.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Exports tag metadata alongside environment configs.
 * Produces a combined view of config properties and their associated tags.
 */
public class ConfigTagExportService {

    private final ConfigTagManager tagManager;
    private final ConfigExporter configExporter;

    public ConfigTagExportService(ConfigTagManager tagManager, ConfigExporter configExporter) {
        this.tagManager = tagManager;
        this.configExporter = configExporter;
    }

    /**
     * Exports the given environment config enriched with tag metadata.
     *
     * @return a map containing the exported config properties plus a "_tags" entry
     * @throws ExportException if export fails
     */
    public Map<String, Object> exportWithTags(EnvironmentConfig config) throws ExportException {
        Map<String, Object> exported = configExporter.exportAsMap(config);
        Set<String> tags = tagManager.getTags(config.getEnvironment());
        Map<String, Object> result = new LinkedHashMap<>(exported);
        result.put("_tags", tags);
        return result;
    }

    /**
     * Returns a summary map of environment -> tags for all tagged environments.
     */
    public Map<String, Set<String>> exportTagSummary() {
        Map<String, Set<String>> summary = new LinkedHashMap<>();
        for (String env : tagManager.getTaggedEnvironments()) {
            summary.put(env, tagManager.getTags(env));
        }
        return summary;
    }
}
