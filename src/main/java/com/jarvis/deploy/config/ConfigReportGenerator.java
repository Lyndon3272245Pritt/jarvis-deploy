package com.jarvis.deploy.config;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates human-readable and structured summary reports for deployment
 * configuration state across environments.
 *
 * <p>Reports include environment summaries, key counts, validation status,
 * checksum info, and recent audit activity — useful for operational dashboards
 * and pre-deployment review gates.
 */
public class ConfigReportGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final ConfigVersionRegistry versionRegistry;
    private final ConfigAuditLog auditLog;
    private final ConfigChecksumService checksumService;
    private final EnvironmentValidator validator;

    public ConfigReportGenerator(
            ConfigVersionRegistry versionRegistry,
            ConfigAuditLog auditLog,
            ConfigChecksumService checksumService,
            EnvironmentValidator validator) {
        this.versionRegistry = versionRegistry;
        this.auditLog = auditLog;
        this.checksumService = checksumService;
        this.validator = validator;
    }

    /**
     * Generates a full report for a single environment config.
     *
     * @param env the environment configuration to report on
     * @return a {@link ConfigReport} containing structured report data
     */
    public ConfigReport generateReport(EnvironmentConfig env) {
        if (env == null) {
            throw new IllegalArgumentException("EnvironmentConfig must not be null");
        }

        String environment = env.getEnvironment();
        Map<String, String> properties = env.getProperties();

        // Validation status
        List<String> validationErrors = validator.validate(env);
        boolean valid = validationErrors.isEmpty();

        // Checksum
        String checksum = checksumService.computeChecksum(env);

        // Version info
        String currentVersion = versionRegistry.getCurrentVersion(environment)
                .orElse("unversioned");

        // Recent audit entries (up to 5)
        List<ConfigAuditEntry> recentEntries = auditLog.getEntriesForEnvironment(environment);
        List<String> recentActivity = new ArrayList<>();
        int limit = Math.min(5, recentEntries.size());
        for (int i = recentEntries.size() - 1; i >= recentEntries.size() - limit; i--) {
            ConfigAuditEntry entry = recentEntries.get(i);
            recentActivity.add(String.format("[%s] %s by %s",
                    FORMATTER.format(entry.getTimestamp()),
                    entry.getAction(),
                    entry.getUser()));
        }

        // Build summary sections
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("environment", environment);
        summary.put("version", currentVersion);
        summary.put("propertyCount", properties.size());
        summary.put("checksum", checksum);
        summary.put("valid", valid);
        summary.put("validationErrors", validationErrors);
        summary.put("generatedAt", FORMATTER.format(Instant.now()));
        summary.put("recentActivity", recentActivity);

        return new ConfigReport(environment, summary);
    }

    /**
     * Generates a multi-environment comparison report.
     *
     * @param environments list of environment configs to compare
     * @return a list of {@link ConfigReport} objects, one per environment
     */
    public List<ConfigReport> generateBulkReport(List<EnvironmentConfig> environments) {
        if (environments == null || environments.isEmpty()) {
            throw new IllegalArgumentException("Environment list must not be null or empty");
        }
        List<ConfigReport> reports = new ArrayList<>();
        for (EnvironmentConfig env : environments) {
            reports.add(generateReport(env));
        }
        return reports;
    }

    /**
     * Formats a {@link ConfigReport} as a plain-text string suitable for CLI output.
     *
     * @param report the report to format
     * @return formatted string representation
     */
    public String formatAsText(ConfigReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Config Report: ").append(report.getEnvironment()).append(" ===").append("\n");
        Map<String, Object> data = report.getData();
        data.forEach((key, value) -> {
            if (value instanceof List) {
                sb.append(String.format("  %-22s:\n", key));
                ((List<?>) value).forEach(item -> sb.append("    - ").append(item).append("\n"));
            } else {
                sb.append(String.format("  %-22s: %s\n", key, value));
            }
        });
        return sb.toString();
    }
}
