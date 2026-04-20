package com.jarvis.deploy.config;

import java.util.Objects;

/**
 * Represents a single stage in a config deployment pipeline.
 */
public class ConfigPipelineStage {

    public enum Status {
        PENDING, RUNNING, SUCCESS, FAILED, SKIPPED
    }

    private final String stageName;
    private final String environment;
    private Status status;
    private String message;
    private long startedAt;
    private long completedAt;

    public ConfigPipelineStage(String stageName, String environment) {
        this.stageName = Objects.requireNonNull(stageName, "stageName must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.status = Status.PENDING;
    }

    public String getStageName() { return stageName; }
    public String getEnvironment() { return environment; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public long getStartedAt() { return startedAt; }
    public long getCompletedAt() { return completedAt; }

    public void markRunning() {
        this.status = Status.RUNNING;
        this.startedAt = System.currentTimeMillis();
    }

    public void markSuccess(String message) {
        this.status = Status.SUCCESS;
        this.message = message;
        this.completedAt = System.currentTimeMillis();
    }

    public void markFailed(String message) {
        this.status = Status.FAILED;
        this.message = message;
        this.completedAt = System.currentTimeMillis();
    }

    public void markSkipped(String reason) {
        this.status = Status.SKIPPED;
        this.message = reason;
        this.completedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "ConfigPipelineStage{name='" + stageName + "', env='" + environment + "', status=" + status + "}";
    }
}
