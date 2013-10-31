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

class ActorTypeFilterSpec extends Specification {
  import akka.actor.ActorPath
  import org.eigengo.monitor.agent.akka.ActorFilter._

  "Type filter" should {
    val actorSystemName = "default"
    val singlePath = ActorPath.fromString(s"akka://$actorSystemName/foo/bar/baz")

    "Match concrete path" in {
      ActorTypeFilter(AnyActorSystem, SameType("com.foo.BarActor")).accept(singlePath, Some("com.foo.BarActor")) mustEqual true
      ActorTypeFilter(NamedActorSystem(actorSystemName), SameType("com.foo.BarActor")).accept(singlePath, Some("com.foo.BarActor")) mustEqual true
      ActorTypeFilter(NamedActorSystem("asdadasdasdas"), SameType("com.foo.BarActor")).accept(singlePath, Some("com.foo.BarActor")) mustEqual false

      ActorTypeFilter(AnyActorSystem, SameType("com.foo.BarActor")).accept(singlePath, Some("com.faa.BarActor")) mustEqual false
      ActorTypeFilter(AnyActorSystem, SameType("com.foo.BarActor")).accept(singlePath, Some("com.faa.BarActor")) mustEqual false
    }

  }

}
