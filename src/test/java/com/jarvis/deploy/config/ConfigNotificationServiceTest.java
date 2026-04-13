package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigNotificationServiceTest {

    private ConfigNotificationService service;

    @BeforeEach
    void setUp() {
        service = new ConfigNotificationService();
    }

    @Test
    void globalListenerReceivesAllEvents() {
        List<ConfigNotificationEvent> received = new ArrayList<>();
        service.registerGlobalListener(received::add);

        ConfigNotificationEvent e1 = new ConfigNotificationEvent("dev", ConfigNotificationEvent.EventType.CREATED, "user", null);
        ConfigNotificationEvent e2 = new ConfigNotificationEvent("prod", ConfigNotificationEvent.EventType.UPDATED, "user", null);
        service.publish(e1);
        service.publish(e2);

        assertEquals(2, received.size());
        assertSame(e1, received.get(0));
        assertSame(e2, received.get(1));
    }

    @Test
    void envListenerReceivesOnlyScopedEvents() {
        List<ConfigNotificationEvent> received = new ArrayList<>();
        service.registerEnvListener("staging", received::add);

        service.publish(new ConfigNotificationEvent("dev", ConfigNotificationEvent.EventType.UPDATED, "user", null));
        service.publish(new ConfigNotificationEvent("staging", ConfigNotificationEvent.EventType.UPDATED, "user", null));

        assertEquals(1, received.size());
        assertEquals("staging", received.get(0).getEnvironment());
    }

    @Test
    void bothGlobalAndEnvListenersReceiveMatchingEvent() {
        List<ConfigNotificationEvent> global = new ArrayList<>();
        List<ConfigNotificationEvent> scoped = new ArrayList<>();
        service.registerGlobalListener(global::add);
        service.registerEnvListener("prod", scoped::add);

        service.publish(new ConfigNotificationEvent("prod", ConfigNotificationEvent.EventType.DELETED, "admin", null));

        assertEquals(1, global.size());
        assertEquals(1, scoped.size());
    }

    @Test
    void clearAllRemovesAllListeners() {
        List<ConfigNotificationEvent> received = new ArrayList<>();
        service.registerGlobalListener(received::add);
        service.registerEnvListener("dev", received::add);
        service.clearAll();

        service.publish(new ConfigNotificationEvent("dev", ConfigNotificationEvent.EventType.CREATED, "user", null));
        assertTrue(received.isEmpty());
    }

    @Test
    void shouldThrowOnNullListener() {
        assertThrows(IllegalArgumentException.class, () -> service.registerGlobalListener(null));
        assertThrows(IllegalArgumentException.class, () -> service.registerEnvListener("dev", null));
    }

    @Test
    void shouldThrowOnNullEvent() {
        assertThrows(IllegalArgumentException.class, () -> service.publish(null));
    }
}
