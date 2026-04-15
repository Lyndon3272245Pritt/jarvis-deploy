package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A ConfigLoader decorator that injects resolved alias values into the loaded config
 * as additional properties, prefixed with "alias.".
 */
public class AliasAwareConfigLoader implements ConfigLoader {

    private final ConfigLoader delegate;
    private final ConfigAliasManager aliasManager;

    public AliasAwareConfigLoader(ConfigLoader delegate, ConfigAliasManager aliasManager) {
        Objects.requireNonNull(delegate, "Delegate ConfigLoader must not be null");
        Objects.requireNonNull(aliasManager, "ConfigAliasManager must not be null");
        this.delegate = delegate;
        this.aliasManager = aliasManager;
    }

    @Override
    public EnvironmentConfig load(String environment) throws ConfigLoadException {
        EnvironmentConfig base = delegate.load(environment);
        Map<String, String> enriched = new HashMap<>(base.getProperties());

        for (ConfigAlias alias : aliasManager.listAliasesForEnvironment(environment)) {
            aliasManager.resolve(alias.getAliasName()).ifPresent(value ->
                enriched.put("alias." + alias.getAliasName(), value)
            );
        }

        return new EnvironmentConfig(base.getEnvironmentName(), enriched);
    }
}
