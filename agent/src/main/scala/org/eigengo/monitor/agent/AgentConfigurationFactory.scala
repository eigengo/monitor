/*
 * Copyright (c) 2013 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eigengo.monitor.agent

import com.typesafe.config._
import scala.util.Try

/**
 * Loads the configuration for the monitoring's agents (and outputs)
 */
object AgentConfigurationFactory {
  private val config = ConfigFactory.load("META-INF/monitor/agent.conf",
    ConfigParseOptions.defaults().setAllowMissing(false),
    ConfigResolveOptions.defaults())
  private val agentConfig = config.getConfig("org.eigengo.monitor.agent")

  /**
   * Load the common configurations for all agents
   *
   * @return the loaded ``CommonAgentConfiguration`` instance
   */
  private def getCommonAgentConfiguration(): CommonAgentConfiguration = {
    val className = agentConfig.getString("output.class")
    CommonAgentConfiguration(className)
  }

  /**
  * Load agent-specific configurations, based on agentName (e.g. "akka"), and package corresponding Config with common agent configurations.
  * Will use empty Config if no corresponding agent settings are found
  *
  * @return the loaded ``AgentConfiguration`` instance
  */
  def getAgentCofiguration(agentName: String): AgentConfiguration = {
    val configuration:Config = Try(config.getConfig(s"org.eigengo.monitor.agent.$agentName")).getOrElse(ConfigFactory.empty())
    AgentConfiguration(getCommonAgentConfiguration(), configuration)
  }

}
