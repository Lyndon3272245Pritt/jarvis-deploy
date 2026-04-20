package com.jarvis.deploy.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Executes a {@link ConfigPipeline} stage by stage, applying a handler per stage.
 */
public class ConfigPipelineExecutor {

    private static final Logger log = Logger.getLogger(ConfigPipelineExecutor.class.getName());

    private final Function<ConfigPipelineStage, Boolean> stageHandler;

    /**
     * @param stageHandler a function that receives a stage and returns true on success, false on failure.
     */
    public ConfigPipelineExecutor(Function<ConfigPipelineStage, Boolean> stageHandler) {
        this.stageHandler = Objects.requireNonNull(stageHandler, "stageHandler must not be null");
    }

    /**
     * Executes all stages in the pipeline in order.
     *
     * @param pipeline the pipeline to execute
     * @return true if all stages succeeded (or were skipped), false if any stage failed
     */
    public boolean execute(ConfigPipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline must not be null");
        List<ConfigPipelineStage> stages = pipeline.getStages();
        boolean aborted = false;

        for (ConfigPipelineStage stage : stages) {
            if (aborted) {
                stage.markSkipped("Pipeline aborted due to prior failure");
                log.info("Skipping stage: " + stage.getStageName());
                continue;
            }

            stage.markRunning();
            log.info("Running stage: " + stage.getStageName() + " for env: " + stage.getEnvironment());

            try {
                boolean success = stageHandler.apply(stage);
                if (success) {
                    stage.markSuccess("Stage completed successfully");
                    log.info("Stage succeeded: " + stage.getStageName());
                } else {
                    stage.markFailed("Stage handler returned false");
                    log.warning("Stage failed: " + stage.getStageName());
                    if (pipeline.isAbortOnFailure()) {
                        aborted = true;
                    }
                }
            } catch (Exception e) {
                stage.markFailed("Exception: " + e.getMessage());
                log.severe("Stage threw exception: " + stage.getStageName() + " - " + e.getMessage());
                if (pipeline.isAbortOnFailure()) {
                    aborted = true;
                }
            }
        }

        return !pipeline.hasFailures();
    }
}
