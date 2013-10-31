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
