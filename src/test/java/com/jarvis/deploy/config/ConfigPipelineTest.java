package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigPipelineTest {

    @Test
    void constructor_setsFieldsCorrectly() {
        ConfigPipeline pipeline = new ConfigPipeline("p-1", "My Pipeline");
        assertEquals("p-1", pipeline.getPipelineId());
        assertEquals("My Pipeline", pipeline.getName());
        assertTrue(pipeline.isAbortOnFailure());
        assertTrue(pipeline.getStages().isEmpty());
    }

    @Test
    void addStage_increasesStageCount() {
        ConfigPipeline pipeline = new ConfigPipeline("p-2", "Deploy");
        pipeline.addStage(new ConfigPipelineStage("validate", "dev"));
        pipeline.addStage(new ConfigPipelineStage("deploy", "dev"));
        assertEquals(2, pipeline.getStages().size());
    }

    @Test
    void getStages_returnsUnmodifiableList() {
        ConfigPipeline pipeline = new ConfigPipeline("p-3", "Readonly");
        assertThrows(UnsupportedOperationException.class, () ->
            pipeline.getStages().add(new ConfigPipelineStage("x", "dev"))
        );
    }

    @Test
    void hasFailures_noStages_returnsFalse() {
        ConfigPipeline pipeline = new ConfigPipeline("p-4", "Empty");
        assertFalse(pipeline.hasFailures());
    }

    @Test
    void hasFailures_withFailedStage_returnsTrue() {
        ConfigPipeline pipeline = new ConfigPipeline("p-5", "Failing");
        ConfigPipelineStage stage = new ConfigPipelineStage("deploy", "prod");
        stage.markFailed("oops");
        pipeline.addStage(stage);
        assertTrue(pipeline.hasFailures());
    }

    @Test
    void isComplete_allSucceeded_returnsTrue() {
        ConfigPipeline pipeline = new ConfigPipeline("p-6", "Done");
        ConfigPipelineStage s1 = new ConfigPipelineStage("validate", "dev");
        ConfigPipelineStage s2 = new ConfigPipelineStage("deploy", "dev");
        s1.markSuccess("ok");
        s2.markSkipped("not needed");
        pipeline.addStage(s1);
        pipeline.addStage(s2);
        assertTrue(pipeline.isComplete());
    }

    @Test
    void isComplete_withPendingStage_returnsFalse() {
        ConfigPipeline pipeline = new ConfigPipeline("p-7", "Incomplete");
        pipeline.addStage(new ConfigPipelineStage("validate", "dev"));
        assertFalse(pipeline.isComplete());
    }

    @Test
    void addStage_nullStage_throwsException() {
        ConfigPipeline pipeline = new ConfigPipeline("p-8", "NullCheck");
        assertThrows(NullPointerException.class, () -> pipeline.addStage(null));
    }
}
