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
package org.eigengo.monitor.output

import com.typesafe.config.{Config, ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import scala.util.Try

/**
 * Loads the configuration for the monitoring's agents (and outputs)
 */
object OutputConfigurationFactory {
  private val config = ConfigFactory.load("META-INF/monitor/output.conf",
    ConfigParseOptions.defaults().setAllowMissing(false),
    ConfigResolveOptions.defaults())

  /**
   * Load output-specific configuration, based on outputName (e.g. "statsd")
   *
   * @param agentName the name of the output, e.g. "statsd"
   * @param agent the function that completes the specific outputconfiguration
   *
   * @return the loaded ``A`` instance
   */
  def getAgentCofiguration[A](agentName: String)(agent: Config => A): A = {
    val outputConfig = Try(config.getConfig(s"org.eigengo.monitor.output.$agentName")).getOrElse(ConfigFactory.empty())
    agent(outputConfig)
  }

}
