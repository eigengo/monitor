package org.eigengo.monitor.agent

import com.typesafe.config._

/**
 * Loads the configuration for the monitoring's agents (and outputs)
 */
object AgentConfigurationFactory {
  private val config = ConfigFactory.load("META-INF/monitor/agent.conf",
    ConfigParseOptions.defaults().setAllowMissing(false),
    ConfigResolveOptions.defaults())
  private val agentConfig = config.getConfig("org.eigengo.monitor.agent")

  /**
   * Load the configuration for the agent
   *
   * @return the loaded ``AgentConfiguration`` instance
   */
  def getCommonAgentConfiguration(): CommonAgentConfiguration = {
    val className = agentConfig.getString("output.class")
    CommonAgentConfiguration(className)
  }

  def getAgentCofiguration(agentName: String): AgentConfiguration = {
    val configuration:Config = try {config.getConfig(s"org.eigengo.monitor.agent.$agentName")} catch {
      case e: ConfigException.Missing => ConfigFactory.empty()
    }
    AgentConfiguration(getCommonAgentConfiguration(), configuration)
  }

}
