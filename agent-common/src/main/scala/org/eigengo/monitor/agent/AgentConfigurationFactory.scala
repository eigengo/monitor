package org.eigengo.monitor.agent

import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}

/**
 * Loads the configuration for the monitoring's agents (and outputs)
 */
object AgentConfigurationFactory {
  private val config = ConfigFactory.load("META-INF/monitor/agent.conf",
    ConfigParseOptions.defaults().setAllowMissing(false),
    ConfigResolveOptions.defaults())
  private val agentConfig = config.getConfig("org.eigengo.monitor.agent")

  /**
   * Load the configuration for the given agent name ``org.eigengo.monitor.agent.akka``
   *
   * @return the loaded ``AgentConfiguration`` instance
   */
  def getCommonAgentConfiguration(): CommonAgentConfiguration = {
    val className = agentConfig.getString("output.class")
    CommonAgentConfiguration(className)
  }

}
