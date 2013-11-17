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

/**
 * Provides the tagging convention that matches the one in the aspect. Mix this into your
 * tests to avoid having to construct the tags manually
 */
trait ActorCellMonitoringTaggingConvention {

  /**
   * Gets the tags for the given ``ref``, ``props`` and number of ``routees``
   *
   * @param ref the ActorRef for the created actor
   * @param props the Props used to create the actor
   * @param routees the number of routees
   * @return the tags
   */
  def getTags(ref: ActorRef, props: Props, routees: Int = 0): List[String] =
    getPathTags(ref, routees) ++ getTypeTags(props)

  /**
   * Gets the path tags for the given ``ref`` and ``routees``.
   *
   * @param ref the ActorRef for the created actor
   * @param routees the number of routees
   * @return the path tags
   */
  def getPathTags(ref: ActorRef, routees: Int): List[String] = {
    val system = ref.path.address.system
    val elems  = ref.path.elements.mkString("/")
    val tag    = s"akka.path:/$system/$elems"
    val routeeTags = (0 until routees).map(x => tag + "/$" + (x + 97).toChar).toList
    tag :: routeeTags
  }

  /**
   * Get the type tags for the given ``props``
   *
   * @param props the Props used to create the actor
   * @return the type tags
   */
  def getTypeTags(props: Props): List[String] = {
    List(s"akka.type:default.${props.actorClass().getCanonicalName}")
  }

}

/**
 * Contains functions that configure the ``ActorCellMonitoringAspect``
 */
trait ActorCellMonitoringAspectConfigurer {
  val aspect: ActorCellMonitoringAspect = ActorCellMonitoringAspect.aspectOf()

  /**
   * Inject new configuration to the aspect.
   *
   * @param config the full file name of the config, without any path elements, e.g. ``foo.conf``
   */
  protected final def configure(config: String): Unit = {
    val configuration = AkkaAgentConfiguration(ConfigFactory.load(s"META-INF/monitor/$config"))
    aspect.setAgentConfiguration(configuration)
  }

}

/**
 * Initializes the aspect at the start of the specification
 */
trait ActorCellMonitoringAspectSpecLike extends ActorCellMonitoringAspectConfigurer {
  def agentConfig: Option[String]

  agentConfig.foreach(configure)

}

/**
 * Convenience type for all ``ActorCellMonitoringAspect`` specifications
 *
 * @param agentConfig the location of the configuration file
 * @see ActorCellMonitoringAspectConfigurer#configure
 */
abstract class ActorCellMonitoringAspectSpec(val agentConfig: Option[String])
  extends TestKit(ActorSystem())
  with SpecificationLike with ActorCellMonitoringAspectSpecLike with ActorCellMonitoringTaggingConvention {
  sequential

  /**
   * Holds the details of the created actor that allow you to send it messages, examine its
   * name and tags
   *
   * @param actor the valid ActorRef
   * @param name the generated or given actor name
   * @param pathTag the path tag
   * @param typeTag the type tag
   */
  case class CreatedActor(actor: ActorRef, name: String, pathTag: String, typeTag: String) {
    /**
     * Computes all type tags
     * @return the type tags
     */
    def typeTags: List[String] = List(typeTag)

    /**
     * Computes all path tags
     * @return the path tags
     */
    def pathTags: List[String] = List(pathTag)

    /**
     * Computes all tags (path + type)
     * @return the tags
     */
    def tags: List[String] = List(pathTag, typeTag)
  }

  /**
   * Creates the actor from the ``props``, optionally using the ``name``
   *
   * @param props the props that are used to create the actor
   * @param name the optional name
   * @return the ``CreatedActor`` instance
   */
  protected def createActor(props: => Props, name: Option[String] = None): CreatedActor = {
    val actorName = name.getOrElse(List.fill(15)((Random.nextInt(25) + 65).toChar).mkString)
    val actorRef = system.actorOf(props, actorName)
    val pathTag = getPathTags(actorRef, 0).head
    val typeTag = getTypeTags(props).head

    CreatedActor(actorRef, actorName, pathTag, typeTag)
  }

  /**
   * Convenience method for two-actor testing. Typical usage is
   *
   * {{{
   *   "some test" in { withActorsOf(Props[A], Props[B]) { (a, b) => ... } }
   * }}}
   *
   * @param p1 the props for the first actor
   * @param p2 the props for the second actor
   * @param withActors the function that is applied to the ``CreatedActor`` instances, executing
   *                   the body of the test
   * @return whatever the body of the test returns
   */
  protected final def withActorsOf(p1: Props, p2: Props)(withActors: (CreatedActor, CreatedActor) => Result): Result = {
    TestCounterInterface.clear()
    withActors(createActor(p1), createActor(p2))
  }

  /**
   * Convenience method for two-actor testing. Typical usage is
   *
   * {{{
   *   "some test" in { withActorOf(Props[A]) { a => ... } }
   * }}}
   *
   * @param props the props for the actor
   * @param withActors the function that is applied to the ``CreatedActor`` instances, executing
   *                   the body of the test
   * @return whatever the body of the test returns
   */
  protected final def withActorOf(props: => Props)(withActor: (CreatedActor => Result)): Result = {
    TestCounterInterface.clear()
    withActor(createActor(props))
  }

}
