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
import akka.actor.{ActorRef, Props, ActorSystem}
import org.specs2.mutable.SpecificationLike
import com.typesafe.config.ConfigFactory
import org.specs2.execute.Result
import org.eigengo.monitor.TestCounterInterface
import scala.util.Random

trait ActorCellMonitoringAspectConfigurer {
  val aspect: ActorCellMonitoringAspect = ActorCellMonitoringAspect.aspectOf()

  protected final def configure(config: String): Unit = {
    val configuration = AkkaAgentConfiguration(ConfigFactory.load(s"META-INF/monitor/$config"))
    aspect.setAgentConfiguration(configuration)
  }

}

trait ActorCellMonitoringAspectSpecLike extends ActorCellMonitoringAspectConfigurer {
  def agentConfig: Option[String]

  agentConfig.foreach(configure)

}

abstract class ActorCellMonitoringAspectSpec(val agentConfig: Option[String]) extends TestKit(ActorSystem()) with SpecificationLike with ActorCellMonitoringAspectSpecLike {
  sequential

  case class CreatedActor(actor: ActorRef, name: String, pathTag: String, typeTag: String) {
    def typeTags: List[String] = List(typeTag)
    def pathTags: List[String] = List(pathTag)
    def tags: List[String] = List(pathTag, typeTag)
  }

  type WithActor = CreatedActor => Result

  protected final def withActorOf(props: => Props)(withActor: WithActor): Result = {
    TestCounterInterface.clear()

    val actorName = List.fill(15)((Random.nextInt(25) + 65).toChar).mkString
    val actorRef = system.actorOf(props, actorName)
    val pathTag = s"akka://default/user/$actorName"
    val typeTag = s"akka:default.${props.actorClass().getCanonicalName}"

    withActor(CreatedActor(actorRef, actorName, pathTag, typeTag))
  }

}
