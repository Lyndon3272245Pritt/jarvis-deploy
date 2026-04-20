package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigPipelineExecutorTest {

    private ConfigPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new ConfigPipeline("p-001", "Test Pipeline");
    }

    @Test
    void execute_allStagesSucceed_returnsTrue() {
        pipeline.addStage(new ConfigPipelineStage("validate", "dev"));
        pipeline.addStage(new ConfigPipelineStage("deploy", "dev"));

        ConfigPipelineExecutor executor = new ConfigPipelineExecutor(stage -> true);
        boolean result = executor.execute(pipeline);

        assertTrue(result);
        pipeline.getStages().forEach(s ->
            assertEquals(ConfigPipelineStage.Status.SUCCESS, s.getStatus())
        );
    }

    @Test
    void execute_firstStageFails_abortsRemainingStages() {
        pipeline.addStage(new ConfigPipelineStage("validate", "staging"));
        pipeline.addStage(new ConfigPipelineStage("deploy", "staging"));
        pipeline.setAbortOnFailure(true);

        ConfigPipelineExecutor executor = new ConfigPipelineExecutor(stage -> {
            if (stage.getStageName().equals("validate")) return false;
            return true;
        });

        boolean result = executor.execute(pipeline);

        assertFalse(result);
        assertEquals(ConfigPipelineStage.Status.FAILED, pipeline.getStages().get(0).getStatus());
        assertEquals(ConfigPipelineStage.Status.SKIPPED, pipeline.getStages().get(1).getStatus());
    }

    @Test
    void execute_abortOnFailureFalse_continuesAfterFailure() {
        pipeline.addStage(new ConfigPipelineStage("validate", "prod"));
        pipeline.addStage(new ConfigPipelineStage("deploy", "prod"));
        pipeline.setAbortOnFailure(false);

        ConfigPipelineExecutor executor = new ConfigPipelineExecutor(stage -> {
            if (stage.getStageName().equals("validate")) return false;
            return true;
        });

        boolean result = executor.execute(pipeline);

        assertFalse(result);
        assertEquals(ConfigPipelineStage.Status.FAILED, pipeline.getStages().get(0).getStatus());
        assertEquals(ConfigPipelineStage.Status.SUCCESS, pipeline.getStages().get(1).getStatus());
    }

    @Test
    void execute_stageThrowsException_marksFailedAndAborts() {
        pipeline.addStage(new ConfigPipelineStage("validate", "dev"));
        pipeline.addStage(new ConfigPipelineStage("notify", "dev"));

        ConfigPipelineExecutor executor = new ConfigPipelineExecutor(stage -> {
            throw new RuntimeException("Unexpected error");
        });

        boolean result = executor.execute(pipeline);

        assertFalse(result);
        assertEquals(ConfigPipelineStage.Status.FAILED, pipeline.getStages().get(0).getStatus());
        assertTrue(pipeline.getStages().get(0).getMessage().contains("Unexpected error"));
        assertEquals(ConfigPipelineStage.Status.SKIPPED, pipeline.getStages().get(1).getStatus());
    }

    @Test
    void execute_emptyPipeline_returnsTrue() {
        ConfigPipelineExecutor executor = new ConfigPipelineExecutor(stage -> true);
        boolean result = executor.execute(pipeline);
        assertTrue(result);
    }

    @Test
    void execute_nullPipeline_throwsException() {
        ConfigPipelineExecutor executor = new ConfigPipelineExecutor(stage -> true);
        assertThrows(NullPointerException.class, () -> executor.execute(null));
    }
}
