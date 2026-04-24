package com.jarvis.deploy.config;

import java.util.*;

/**
 * Exports configuration entries scoped to a specific group.
 * Delegates actual serialization to {@link ConfigExporter}.
 */
public class ConfigGroupExportService {

    private final ConfigGroupManager groupManager;
    private final ConfigExporter exporter;

    public ConfigGroupExportService(ConfigGroupManager groupManager, ConfigExporter exporter) {
        this.groupManager = Objects.requireNonNull(groupManager, "groupManager must not be null");
        this.exporter = Objects.requireNonNull(exporter, "exporter must not be null");
    }

    /**
     * Exports only the keys belonging to {@code groupName} from the given environment config.
     *
     * @param groupName   the group whose keys should be exported
     * @param environment the source environment config
     * @param format      target export format (e.g. "json", "yaml", "properties")
     * @return serialized string of the filtered config
     * @throws ExportException if serialization fails
     */
    public String exportGroup(String groupName, EnvironmentConfig environment, String format)
            throws ExportException {
        if (!groupManager.groupExists(groupName)) {
            throw new IllegalArgumentException("Group not found: " + groupName);
        }
        Map<String, String> filtered = groupManager.filterByGroup(groupName, environment.getProperties());
        EnvironmentConfig groupEnv = new EnvironmentConfig(
                environment.getName() + "#" + groupName,
                filtered
        );
        return exporter.export(groupEnv, format);
    }

    /**
     * Exports all groups as a map of groupName -> serialized config string.
     */
    public Map<String, String> exportAllGroups(EnvironmentConfig environment, String format)
            throws ExportException {
        Map<String, String> result = new LinkedHashMap<>();
        for (String group : groupManager.getAllGroups()) {
            result.put(group, exportGroup(group, environment, format));
        }
        return Collections.unmodifiableMap(result);
    }
}