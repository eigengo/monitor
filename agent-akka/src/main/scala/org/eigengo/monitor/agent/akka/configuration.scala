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

object AkkaAgentConfigurationJapi {

  def apply: Config => AkkaAgentConfiguration = AkkaAgentConfiguration.apply

}

//TODO: complete me
//case class SamplingRate(included: ActorFilter, sampleEvery: Int)