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

trait ActorCellMonitoringTaggingConvention {

  def getTags(ref: ActorRef, props: Props, routees: Int = 0): List[String] =
    getPathTags(ref, routees) ++ getTypeTags(props)

  def getPathTags(ref: ActorRef, routees: Int): List[String] = {
    val system = ref.path.address.system
    val elems  = ref.path.elements.mkString("/")
    val tag    = s"akka.path:/$system/$elems"
    val routeeTags = (0 until routees).map(x => tag + "/$" + (x + 97).toChar).toList
    tag :: routeeTags
  }

  def getTypeTags(props: Props): List[String] = {
    List(s"akka.type:default.${props.actorClass().getCanonicalName}")
  }

}

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

abstract class ActorCellMonitoringAspectSpec(val agentConfig: Option[String])
  extends TestKit(ActorSystem())
  with SpecificationLike with ActorCellMonitoringAspectSpecLike with ActorCellMonitoringTaggingConvention {
  sequential

  case class CreatedActor(actor: ActorRef, name: String, pathTag: String, typeTag: String) {
    def typeTags: List[String] = List(typeTag)
    def pathTags: List[String] = List(pathTag)
    def tags: List[String] = List(pathTag, typeTag)
  }

  protected def createActor(props: => Props, name: Option[String] = None): CreatedActor = {
    val actorName = name.getOrElse(List.fill(15)((Random.nextInt(25) + 65).toChar).mkString)
    val actorRef = system.actorOf(props, actorName)
    val pathTag = getPathTags(actorRef, 0).head
    val typeTag = getTypeTags(props).head

    CreatedActor(actorRef, actorName, pathTag, typeTag)
  }

  protected final def withActorsOf(p1: Props, p2: Props)(withActors: (CreatedActor, CreatedActor) => Result): Result = {
    TestCounterInterface.clear()
    withActors(createActor(p1), createActor(p2))
  }

  protected final def withActorOf(props: => Props)(withActor: (CreatedActor => Result)): Result = {
    TestCounterInterface.clear()
    withActor(createActor(props))
  }

}
