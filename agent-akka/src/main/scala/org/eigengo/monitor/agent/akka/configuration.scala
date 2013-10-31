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
package org.eigengo.monitor.agent.akka

import com.typesafe.config.Config

/**
 * Configures the Akka agent by specifying the ``included`` and ``excluded`` actor filters.
 *
 * @param includeRoutees include the routed instances in the monitoring?
 * @param incuded the filter that matches the included actors
 * @param excluded the filter that matches the excluded actors
 */
case class AkkaAgentConfiguration(includeRoutees: Boolean, incuded: ActorFilter, excluded: ActorFilter)

/**
 * Companion for AkkaAgentConfiguration that provides a method to turn a ``Config`` into
 * ``AkkaAgentConfiguration``.
 */
object AkkaAgentConfiguration {

  /**
   * Parses the given ``config`` into a valid ``AkkaAgentConfiguration``
   *
   * @param config the agent-specific configuration
   * @return the AkkaAgentConfiguration
   */
  def apply(config: Config): AkkaAgentConfiguration = {
    val includeRoutees = config.getBoolean("includeRoutees");
    AkkaAgentConfiguration(includeRoutees, AnyAcceptActorFilter(Nil, true), AnyAcceptActorFilter(Nil, true))
  }
}

/**
 * Exposes the ``AkkaAgentConfiguration``'s Java-friendly API
 */
object AkkaAgentConfigurationJapi {

  /**
   * Lifted ``AkkaAgentConfiguration.apply``
   *
   * @return the lifted function
   */
  def apply: Config => AkkaAgentConfiguration = AkkaAgentConfiguration.apply

}

//TODO: complete me
//case class SamplingRate(included: ActorFilter, sampleEvery: Int)