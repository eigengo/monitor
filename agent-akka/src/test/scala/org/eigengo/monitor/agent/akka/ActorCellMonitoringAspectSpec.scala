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
