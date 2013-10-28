package org.eigengo.monitor.agent

import com.typesafe.config.Config

case class CommonAgentConfiguration(counterInterfaceClassName: String)

case class AgentConfiguration(common: CommonAgentConfiguration, config: Config)
