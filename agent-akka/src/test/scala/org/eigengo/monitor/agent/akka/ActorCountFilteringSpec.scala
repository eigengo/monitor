package org.eigengo.monitor.agent.akka

import org.eigengo.monitor.{TestCounter, TestCounterInterface}
import akka.actor.Props

class ActorCountFilteringSpec extends ActorCellMonitoringAspectSpec(Some("count-filtering.conf")) {
  sequential
  import Aspects._

  "Actor count monitoring" should {

    // records the count of actors, grouped by simple class name
    "Not record the count of exluded actors" in {
      val monitoredActorTag = "org.eigengo.monitor.agent.akka.SimpleActor"
      val unmonitoredActorTag = "org.eigengo.monitor.agent.akka.KillableActor"
      TestCounterInterface.clear()

      val monitoredActor = system.actorOf(Props[SimpleActor], "simple")

      // this actor is excluded -- we shouldn't be monitoring it
      val unmonitoredActor = system.actorOf(Props[KillableActor], "killable")

      Thread.sleep(1000)

      val counterBeforeKill = TestCounterInterface.foldlByAspect(actorCount)((a,_) =>a)
      counterBeforeKill.size === 1
      counterBeforeKill must contain(TestCounter(actorCount, 1, List(monitoredActorTag)))

      monitoredActor ! 'stop
      unmonitoredActor ! 'stop

      Thread.sleep(500)

      val counterAfterKill = TestCounterInterface.foldlByAspect(actorCount)((a,_) =>a)
      counterAfterKill.size === 2
      counterAfterKill must contain(TestCounter(actorCount, 0, List(monitoredActorTag)))
    }
  }
}