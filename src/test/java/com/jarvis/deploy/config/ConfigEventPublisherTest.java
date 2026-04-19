package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigEventPublisherTest {

    private ConfigEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ConfigEventPublisher();
    }

    @Test
    void registerAndReceiveEvent() {
        List<ConfigEventPublisher.ConfigEvent> received = new ArrayList<>();
        publisher.register(received::add);
        publisher.publish("prod", ConfigEventPublisher.EventType.LOADED, "3 properties");
        assertEquals(1, received.size());
        assertEquals("prod", received.get(0).getEnvironment());
        assertEquals(ConfigEventPublisher.EventType.LOADED, received.get(0).getType());
    }

    @Test
    void multipleListenersAllReceiveEvent() {
        List<ConfigEventPublisher.ConfigEvent> list1 = new ArrayList<>();
        List<ConfigEventPublisher.ConfigEvent> list2 = new ArrayList<>();
        publisher.register(list1::add);
        publisher.register(list2::add);
        publisher.publish("staging", ConfigEventPublisher.EventType.UPDATED, "override applied");
        assertEquals(1, list1.size());
        assertEquals(1, list2.size());
    }

    @Test
    void unregisterStopsReceivingEvents() {
        List<ConfigEventPublisher.ConfigEvent> received = new ArrayList<>();
        ConfigEventPublisher.ConfigEventListener listener = received::add;
        publisher.register(listener);
        publisher.unregister(listener);
        publisher.publish("dev", ConfigEventPublisher.EventType.DELETED, "removed");
        assertTrue(received.isEmpty());
    }

    @Test
    void listenerCountReflectsRegistrations() {
        assertEquals(0, publisher.listenerCount());
        publisher.register(e -> {});
        publisher.register(e -> {});
        assertEquals(2, publisher.listenerCount());
    }

    @Test
    void registerNullListenerThrows() {
        assertThrows(IllegalArgumentException.class, () -> publisher.register(null));
    }

    @Test
    void eventToStringContainsDetails() {
        ConfigEventPublisher.ConfigEvent event = new ConfigEventPublisher.ConfigEvent(
                "prod", ConfigEventPublisher.EventType.PROMOTED, "promoted to prod");
        String str = event.toString();
        assertTrue(str.contains("PROMOTED"));
        assertTrue(str.contains("prod"));
        assertTrue(str.contains("promoted to prod"));
    }
}
