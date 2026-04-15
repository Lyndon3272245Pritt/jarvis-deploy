package com.jarvis.deploy.config;

import java.util.Map;

/**
 * Wraps a ConfigLoader to attach comments to loaded configs from a ConfigCommentManager.
 */
public class CommentAwareConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigCommentManager commentManager;

    public CommentAwareConfigLoader(ConfigLoader delegate, ConfigCommentManager commentManager) {
        if (delegate == null) throw new IllegalArgumentException("delegate must not be null");
        if (commentManager == null) throw new IllegalArgumentException("commentManager must not be null");
        this.delegate = delegate;
        this.commentManager = commentManager;
    }

    /**
     * Loads the environment config and returns it, while ensuring comments are
     * pre-populated for any keys that do not yet have one (as empty string placeholder).
     */
    public EnvironmentConfig load(String environment) throws ConfigLoadException {
        EnvironmentConfig config = delegate.load(environment);
        Map<String, String> properties = config.getProperties();
        for (String key : properties.keySet()) {
            if (!commentManager.hasComment(environment, key)) {
                commentManager.addComment(environment, key, "");
            }
        }
        return config;
    }

    public ConfigCommentManager getCommentManager() {
        return commentManager;
    }
}
