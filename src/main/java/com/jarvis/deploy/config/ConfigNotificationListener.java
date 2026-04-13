package com.jarvis.deploy.config;

/**
 * Listener interface for receiving configuration notification events.
 */
@FunctionalInterface
public interface ConfigNotificationListener {

    /**
     * Called when a configuration notification event occurs.
     *
     * @param event the notification event
     */
    void onEvent(ConfigNotificationEvent event);
}
