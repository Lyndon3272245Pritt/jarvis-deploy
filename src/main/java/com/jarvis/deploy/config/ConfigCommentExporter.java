package com.jarvis.deploy.config;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

/**
 * Exports config comments for an environment to a .comments properties-style file.
 */
public class ConfigCommentExporter {

    private final ConfigCommentManager commentManager;

    public ConfigCommentExporter(ConfigCommentManager commentManager) {
        if (commentManager == null) throw new IllegalArgumentException("commentManager must not be null");
        this.commentManager = commentManager;
    }

    /**
     * Exports comments for the given environment to the specified file path.
     * Format: key=comment (one per line), skipping blank comments.
     */
    public void export(String environment, Path outputPath) throws ExportException {
        Map<String, String> envComments = commentManager.getCommentsForEnvironment(environment);
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("# Comments for environment: " + environment);
            writer.newLine();
            for (Map.Entry<String, String> entry : envComments.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new ExportException("Failed to export comments for environment '" + environment + "'", e);
        }
    }

    /**
     * Returns the exported content as a string (useful for testing/preview).
     */
    public String exportToString(String environment) {
        Map<String, String> envComments = commentManager.getCommentsForEnvironment(environment);
        StringBuilder sb = new StringBuilder();
        sb.append("# Comments for environment: ").append(environment).append("\n");
        for (Map.Entry<String, String> entry : envComments.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
        }
        return sb.toString();
    }
}
