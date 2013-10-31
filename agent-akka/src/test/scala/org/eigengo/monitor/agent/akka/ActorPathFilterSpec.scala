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
