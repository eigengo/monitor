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

import org.specs2.mutable.Specification

class ActorPathFilterSpec extends Specification {
  import akka.actor.ActorPath
  import org.eigengo.monitor.agent.akka.ActorFilter._

  "Path filter" should {
    val actorSystemName = "default"
    val singlePath = ActorPath.fromString(s"akka://$actorSystemName/foo/bar/baz")

    "Match concrete path" in {
      ActorPathFilter(AnyActorSystem, List("foo", "bar", "baz").map(NamedPathElement)).accept(singlePath, null) mustEqual true
      ActorPathFilter(NamedActorSystem(actorSystemName), List("foo", "bar", "baz").map(NamedPathElement)).accept(singlePath, null) mustEqual true
      ActorPathFilter(NamedActorSystem("asdadasdadsad"), List("foo", "bar", "baz").map(NamedPathElement)).accept(singlePath, null) mustEqual false

      ActorPathFilter(AnyActorSystem, List("faa", "bar", "baz").map(NamedPathElement)).accept(singlePath, null) mustEqual false
    }

    "Match wildcard path" in {
      ActorPathFilter(AnyActorSystem, SingleWildcardPathElement :: List("bar", "baz").map(NamedPathElement)).accept(singlePath, null) mustEqual true
      ActorPathFilter(AnyActorSystem, SingleWildcardPathElement :: List("baa", "baz").map(NamedPathElement)).accept(singlePath, null) mustEqual false
    }

  /*
  If we ever decide to support multi-paths

  "Match multi-wildcard path" in {
    ActorPathFilter(AnyActorSystem, MultiWildcardPathElement :: NamedPathElement("baz") :: Nil).accept(singlePath, null) mustEqual true
    ActorPathFilter(AnyActorSystem, MultiWildcardPathElement :: NamedPathElement("foo") :: Nil).accept(singlePath, null) mustEqual false
  }
  */
  }

}
