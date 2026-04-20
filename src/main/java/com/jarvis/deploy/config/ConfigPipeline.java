package com.jarvis.deploy.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an ordered deployment pipeline consisting of multiple stages.
 */
public class ConfigPipeline {

    private final String pipelineId;
    private final String name;
    private final List<ConfigPipelineStage> stages;
    private boolean abortOnFailure;

    public ConfigPipeline(String pipelineId, String name) {
        this.pipelineId = Objects.requireNonNull(pipelineId, "pipelineId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.stages = new ArrayList<>();
        this.abortOnFailure = true;
    }

    public void addStage(ConfigPipelineStage stage) {
        Objects.requireNonNull(stage, "stage must not be null");
        stages.add(stage);
    }

    public String getPipelineId() { return pipelineId; }
    public String getName() { return name; }
    public boolean isAbortOnFailure() { return abortOnFailure; }
    public void setAbortOnFailure(boolean abortOnFailure) { this.abortOnFailure = abortOnFailure; }

    public List<ConfigPipelineStage> getStages() {
        return Collections.unmodifiableList(stages);
    }

    public boolean hasFailures() {
        return stages.stream().anyMatch(s -> s.getStatus() == ConfigPipelineStage.Status.FAILED);
    }

    public boolean isComplete() {
        return stages.stream().allMatch(s ->
            s.getStatus() == ConfigPipelineStage.Status.SUCCESS ||
            s.getStatus() == ConfigPipelineStage.Status.FAILED ||
            s.getStatus() == ConfigPipelineStage.Status.SKIPPED
        );
    }

    @Override
    public String toString() {
        return "ConfigPipeline{id='" + pipelineId + "', name='" + name + "', stages=" + stages.size() + "}";
    }
}
