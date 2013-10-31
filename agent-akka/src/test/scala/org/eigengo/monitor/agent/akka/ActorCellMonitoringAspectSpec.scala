package org.eigengo.monitor.agent.akka

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike
import com.typesafe.config.ConfigFactory

abstract class ActorCellMonitoringAspectSpec(agentConfig: Option[String]) extends TestKit(ActorSystem()) with SpecificationLike {
  sequential
  val aspect: ActorCellMonitoringAspect = ActorCellMonitoringAspect.aspectOf()
  agentConfig.map { config =>
    val configuration = AkkaAgentConfiguration(ConfigFactory.load(s"META-INF/monitor/$config"))
    aspect.setAgentConfiguration(configuration)
  }

}
