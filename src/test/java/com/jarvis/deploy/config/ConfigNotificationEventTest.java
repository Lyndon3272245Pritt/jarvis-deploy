package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ConfigNotificationEventTest {

    @Test
    void shouldCreateEventWithExpectedFields() {
        Instant before = Instant.now();
        ConfigNotificationEvent event = new ConfigNotificationEvent(
                "production",
                ConfigNotificationEvent.EventType.UPDATED,
                "admin",
                "Updated DB_URL"
        );
        Instant after = Instant.now();

        assertEquals("production", event.getEnvironment());
        assertEquals(ConfigNotificationEvent.EventType.UPDATED, event.getEventType());
        assertEquals("admin", event.getTriggeredBy());
        assertEquals("Updated DB_URL", event.getDetails());
        assertFalse(event.getTimestamp().isBefore(before));
        assertFalse(event.getTimestamp().isAfter(after));
    }

    @Test
    void shouldThrowOnNullEnvironment() {
        assertThrows(NullPointerException.class, () ->
                new ConfigNotificationEvent(null, ConfigNotificationEvent.EventType.CREATED, "user", "detail"));
    }

    @Test
    void shouldThrowOnNullEventType() {
        assertThrows(NullPointerException.class, () ->
                new ConfigNotificationEvent("dev", null, "user", "detail"));
    }

    @Test
    void shouldThrowOnNullTriggeredBy() {
        assertThrows(NullPointerException.class, () ->
                new ConfigNotificationEvent("dev", ConfigNotificationEvent.EventType.DELETED, null, "detail"));
    }

    @Test
    void toStringShouldContainKeyFields() {
        ConfigNotificationEvent event = new ConfigNotificationEvent(
                "staging", ConfigNotificationEvent.EventType.ROLLED_BACK, "ops", "rollback to v3");
        String str = event.toString();
        assertTrue(str.contains("staging"));
        assertTrue(str.contains("ROLLED_BACK"));
        assertTrue(str.contains("ops"));
    }
}
