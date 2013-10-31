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

import akka.actor.ActorPath
import org.eigengo.monitor.agent.akka.ActorFilter._

import scala.annotation.tailrec

/**
 * ActorFilters always start by filtering the actor system, and then filtering either by looking
 * at the actor path, or by looking at the actor type
 *
 * Instances of this trait will be created by parsing the filter expressions:
 *
 * <pre><code>
 * akka://actor-system-name/user/concrete/path
 * akka://&#42;/user/concrete/path
 * akka://&#42;/&#42;/wildcard-path
 * akka://&#42;/../child
 * akka://&#42;/../child/+
 *
 * akka:actor-system-name.com.foo.BarActor
 * akka:&#42;.com.foo.BarActor
 * ...
 * </code></pre>
 */
sealed trait ActorFilter {

  /**
   * Decides whether to accept this ``actorSystemName`` and ``actorPath``
   *
   * @param actorPath the actor path being examined
   * @param actorClassName the type of the actor being examined
   * @return ``true`` if the actor system and path are acceptable
   */
  def accept(actorPath: ActorPath, actorClassName: String): Boolean

}

/**
 * Filter that matches all given ``filters``, returning ``zero`` if ``filters`` is ``Nil``
 *
 * @param filters the filters to accept
 * @param zero the zero
 */
case class AnyAcceptActorFilter(filters: List[ActorFilter], zero: Boolean) extends ActorFilter {
  override def accept(actorPath: ActorPath, actorClassName: String): Boolean = {

    @tailrec
    def accept0(xs: List[ActorFilter]): Boolean = filters match {
      case Nil  => zero
      case x::t => if (x.accept(actorPath, actorClassName)) accept0(t) else false
    }

    accept0(filters)
  }
}

/**
 * Filter that uses the actor path to determine whether to accept the actor or not
 *
 * @param actorSystem the actor system filter
 * @param actorPathElements the elements of the actor path
 */
case class ActorPathFilter(actorSystem: ActorSystemNameFilter, actorPathElements: List[ActorPathElement]) extends ActorFilter {

  override def accept(actorPath: ActorPath, actorClassName: String): Boolean = actorSystem match {
    case AnyActorSystem                    => localAccept(actorPath, actorClassName)
    case NamedActorSystem(actorSystemName) => actorPath.root.address.system == actorSystemName && localAccept(actorPath, actorClassName)
    case _                                 => false
  }

  private def localAccept(actorPath: ActorPath, actorClassName: String): Boolean = {

    @tailrec
    def acceptAll(xs: List[(ActorPathElement, String)]): Boolean = xs match {
      case Nil         => true
      case (ape, e)::t => if (ape.accept(e)) acceptAll(t) else false
    }

    val elements = actorPath.elements.toList
    actorPathElements.size == elements.size && acceptAll(actorPathElements.zip(elements))
  }
}

/**
 * Filter that uses the actor type to determine whether to accept the actor or not
 *
 * @param actorSystem the actor system filter
 * @param actorType the type comparison operator
 */
case class ActorTypeFilter(actorSystem: ActorSystemNameFilter, actorType: ActorTypeOperator) extends ActorFilter {

  override def accept(actorPath: ActorPath, actorClassName: String): Boolean = actorSystem match {
    case AnyActorSystem                    => localAccept(actorPath, actorClassName)
    case NamedActorSystem(actorSystemName) => actorPath.root.address.system == actorSystemName && localAccept(actorPath, actorClassName)
    case _                                 => false
  }

  private def localAccept(actorPath: ActorPath, actorClassName: String): Boolean = actorType match {
    case SameType(`actorClassName`) => true
    case _                          => false
  }
}

/**
 * Companion object for the trait containing more specialized filters
 */
object ActorFilter {

  /**
   * ActorSystem filter accept either _any_ actor system or one whose name matches exactly the expected name
   */
  sealed trait ActorSystemNameFilter
  case object AnyActorSystem extends ActorSystemNameFilter
  case class NamedActorSystem(name: String) extends ActorSystemNameFilter

  /**
   * Actor path element can be either specified name (e.g. "user" or "barkeeper"), a single wildcard matching any
   * single name, or a multi wildcard matching multiple names.
   *
   * Let's tackle some examples
   *
   * - /foo/bar/baz     is ``NamedPathElement(foo) :: NamedPathElement(bar) :: NamedPathElement(baz) :: Nil``,
   * - /foo/&#42;/baz   is ``NamedPathElement(foo) :: SingleWildcardPathElement :: NamedPathElement(baz) :: Nil``
   */
  sealed trait ActorPathElement {
    /**
     * Indicates whether the ``element`` is acceptable
     *
     * @param element the path element
     * @return ``true`` if acceptable
     */
    def accept(element: String): Boolean
  }
  case object SingleWildcardPathElement extends ActorPathElement {
    def accept(element: String): Boolean = true
  }
  // (maybe add this in the future) case object MultiWildcardPathElement extends ActorPathElement
  case class NamedPathElement(name: String) extends ActorPathElement {
    def accept(element: String): Boolean = element == name
  }

  /**
   * The actor type can be matched against exactly required type, subtype or supertype
   */
  sealed trait ActorTypeOperator
  case class SameType(typeName: String) extends ActorTypeOperator
  // (maybe we add these in the future) case class Subtype(tpe: Class[_ <: Actor]) extends ActorTypeOperator
  // (maybe we add these in the future) case class Supertype(tpe: Class[_ <: Actor]) extends ActorTypeOperator

}