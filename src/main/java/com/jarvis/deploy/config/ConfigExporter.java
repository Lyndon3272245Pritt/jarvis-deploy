package com.jarvis.deploy.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Exports an {@link EnvironmentConfig} to various output formats.
 */
public class ConfigExporter {

    public enum Format {
        PROPERTIES,
        ENV,
        JSON
    }

    /**
     * Exports the given config to the specified file path using the given format.
     *
     * @param config the environment config to export
     * @param destination the file to write to
     * @param format the output format
     * @throws IOException if writing fails
     */
    public void export(EnvironmentConfig config, Path destination, Format format) throws IOException {
        try (Writer writer = Files.newBufferedWriter(destination)) {
            switch (format) {
                case PROPERTIES -> writeProperties(config, writer);
                case ENV        -> writeEnv(config, writer);
                case JSON       -> writeJson(config, writer);
                default         -> throw new IllegalArgumentException("Unsupported format: " + format);
            }
        }
    }

    private void writeProperties(EnvironmentConfig config, Writer writer) throws IOException {
        writer.write("# Environment: " + config.getEnvironment() + "\n");
        for (Map.Entry<String, String> entry : sorted(config.getProperties()).entrySet()) {
            writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
        }
    }

    private void writeEnv(EnvironmentConfig config, Writer writer) throws IOException {
        writer.write("# Environment: " + config.getEnvironment() + "\n");
        for (Map.Entry<String, String> entry : sorted(config.getProperties()).entrySet()) {
            writer.write("export " + entry.getKey().toUpperCase().replace('.', '_')
                    + "=\"" + entry.getValue() + "\"\n");
        }
    }

    private void writeJson(EnvironmentConfig config, Writer writer) throws IOException {
        writer.write("{\n");
        writer.write("  \"environment\": \"" + config.getEnvironment() + "\",\n");
        writer.write("  \"properties\": {\n");
        Map<String, String> sorted = sorted(config.getProperties());
        int i = 0;
        int size = sorted.size();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String comma = (++i < size) ? "," : "";
            writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"" + comma + "\n");
        }
        writer.write("  }\n}\n");
    }

    private Map<String, String> sorted(Map<String, String> map) {
        return new TreeMap<>(map);
    }
}
