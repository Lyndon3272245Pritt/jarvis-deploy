package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigQuotaEnforcerTest {

    private ConfigQuotaPolicy policy;
    private ConfigQuotaEnforcer enforcer;

    @BeforeEach
    void setUp() {
        policy = new ConfigQuotaPolicy(5);
        enforcer = new ConfigQuotaEnforcer(policy);
    }

    private EnvironmentConfig configWithKeys(String env, int keyCount) {
        Map<String, String> props = new HashMap<>();
        for (int i = 0; i < keyCount; i++) {
            props.put("key" + i, "value" + i);
        }
        return new EnvironmentConfig(env, props);
    }

    @Test
    void enforce_withinDefaultLimit_doesNotThrow() {
        EnvironmentConfig config = configWithKeys("dev", 4);
        assertDoesNotThrow(() -> enforcer.enforce(config));
    }

    @Test
    void enforce_atDefaultLimit_doesNotThrow() {
        EnvironmentConfig config = configWithKeys("dev", 5);
        assertDoesNotThrow(() -> enforcer.enforce(config));
    }

    @Test
    void enforce_exceedsDefaultLimit_throwsException() {
        EnvironmentConfig config = configWithKeys("dev", 6);
        ConfigQuotaEnforcementException ex = assertThrows(
            ConfigQuotaEnforcementException.class, () -> enforcer.enforce(config));
        assertTrue(ex.getMessage().contains("dev"));
        assertTrue(ex.getMessage().contains("6"));
    }

    @Test
    void enforce_customLimitForEnvironment_usesCustomLimit() {
        policy.setLimit("prod", 2);
        EnvironmentConfig config = configWithKeys("prod", 3);
        assertThrows(ConfigQuotaEnforcementException.class, () -> enforcer.enforce(config));
    }

    @Test
    void enforce_customLimitAllows_withinCustomLimit() {
        policy.setLimit("staging", 10);
        EnvironmentConfig config = configWithKeys("staging", 8);
        assertDoesNotThrow(() -> enforcer.enforce(config));
    }

    @Test
    void isWithinQuota_returnsTrueWhenWithinLimit() {
        EnvironmentConfig config = configWithKeys("dev", 3);
        assertTrue(enforcer.isWithinQuota(config));
    }

    @Test
    void isWithinQuota_returnsFalseWhenExceedsLimit() {
        EnvironmentConfig config = configWithKeys("dev", 10);
        assertFalse(enforcer.isWithinQuota(config));
    }

    @Test
    void constructor_nullPolicy_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigQuotaEnforcer(null));
    }

    @Test
    void enforce_nullConfig_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> enforcer.enforce(null));
    }
}
