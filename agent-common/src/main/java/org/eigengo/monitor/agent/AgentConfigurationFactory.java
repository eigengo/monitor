package org.eigengo.monitor.agent;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigResolveOptions;

/**
 * Loads & finds the configuration file for the agents.
 */
public final class AgentConfigurationFactory {
    private AgentConfigurationFactory() {

    }

    private static Config config = ConfigFactory.load("META-INF/monitor/agent.conf",
            ConfigParseOptions.defaults().setAllowMissing(false),
            ConfigResolveOptions.defaults());
    private static Config agentConfig = config.getConfig("org.eigengo.monitor.agent");

    /**
     * Load the configuration for the given agent name <code>org.eigengo.monitor.agent.akka</code>
     *
     * @return the loaded {@code AgentConfiguration} instance
     */
    public static AgentConfiguration getAgentConfiguration(String agentName) {
        String className = agentConfig.getConfig(agentName).getString("output.class");
        return new AgentConfiguration(className);
    }

}
