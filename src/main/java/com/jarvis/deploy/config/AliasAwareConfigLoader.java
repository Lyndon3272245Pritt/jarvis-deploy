package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A ConfigLoader decorator that injects resolved alias values into the loaded config
 * as additional properties, prefixed with "alias.".
 */
public class AliasAwareConfigLoader implements ConfigLoader {

    private static final Logger logger = Logger.getLogger(AliasAwareConfigLoader.class.getName());
    private static final String ALIAS_PREFIX = "alias.";

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
            aliasManager.resolve(alias.getAliasName()).ifPresentOrElse(
                value -> enriched.put(ALIAS_PREFIX + alias.getAliasName(), value),
                () -> logger.warning("Could not resolve alias '" + alias.getAliasName()
                    + "' for environment '" + environment + "'")
            );
        }

        return new EnvironmentConfig(base.getEnvironmentName(), enriched);
    }
}
