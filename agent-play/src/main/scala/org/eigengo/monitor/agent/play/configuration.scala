/*
 * Copyright (c) 2014 original authors
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
package org.eigengo.monitor.agent.play

import com.typesafe.config.Config

/**
 * Configuration of the Play agent.
 */
case class PlayAgentConfiguration(foo: String)

/**
 * Companion for PlayAgentConfiguration that provides a method to turn a ``Config`` into
 * ``PlayAgentConfiguration``.
 */
object PlayAgentConfiguration {
  /**
   * Parses the given ``config`` into a valid ``PlayAgentConfiguration``
   *
   * @param config the agent-specific configuration
   * @return the PlayAgentConfiguration
   */
  def apply(config: Config): PlayAgentConfiguration = PlayAgentConfiguration(config.getString("foo"))
}

/**
 * Exposes the ``PlayAgentConfigurationJapi``'s Java-friendly API
 */
object PlayAgentConfigurationJapi {
  /**
   * Lifted ``AkkaAgentConfiguration.apply``
   *
   * @return the lifted function
   */
  def apply: Config => PlayAgentConfiguration = PlayAgentConfiguration.apply
}