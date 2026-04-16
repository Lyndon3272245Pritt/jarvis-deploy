package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compares two EnvironmentConfig instances and produces a ConfigCompareReport.
 */
public class ConfigCompareService {

    public ConfigCompareReport compare(EnvironmentConfig base, EnvironmentConfig target) {
        if (base == null) throw new IllegalArgumentException("Base config must not be null");
        if (target == null) throw new IllegalArgumentException("Target config must not be null");

        Map<String, String> baseProps = base.getProperties();
        Map<String, String> targetProps = target.getProperties();

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(baseProps.keySet());
        allKeys.addAll(targetProps.keySet());

        List<ConfigCompareReport.Entry> entries = new ArrayList<>();

        for (String key : allKeys) {
            boolean inBase = baseProps.containsKey(key);
            boolean inTarget = targetProps.containsKey(key);

            if (inBase && inTarget) {
                String bv = baseProps.get(key);
                String tv = targetProps.get(key);
                ConfigCompareReport.DiffType type = bv.equals(tv)
                        ? ConfigCompareReport.DiffType.UNCHANGED
                        : ConfigCompareReport.DiffType.MODIFIED;
                entries.add(new ConfigCompareReport.Entry(key, type, bv, tv));
            } else if (inBase) {
                entries.add(new ConfigCompareReport.Entry(key, ConfigCompareReport.DiffType.REMOVED, baseProps.get(key), null));
            } else {
                entries.add(new ConfigCompareReport.Entry(key, ConfigCompareReport.DiffType.ADDED, null, targetProps.get(key)));
            }
        }

        return new ConfigCompareReport(base.getEnvironmentName(), target.getEnvironmentName(), entries);
    }
}
