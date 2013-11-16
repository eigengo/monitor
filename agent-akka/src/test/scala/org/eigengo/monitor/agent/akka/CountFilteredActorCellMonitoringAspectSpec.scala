package org.eigengo.monitor.agent.akka

import org.eigengo.monitor.{TestCounter, TestCounterInterface}
import akka.actor.Props

class CountFilteredActorCellMonitoringAspectSpec extends ActorCellMonitoringAspectSpec(Some("CountFiltered.conf")) {
  sequential
  import Aspects._
  import TestCounterInterface.takeLHS

  "Actor count monitoring" should {

    // records the count of actors, grouped by simple class name
    "Not record the count of exluded actors" in {
      TestCounterInterface.clear()

      val monitoredActorTag = "akka://default/user/simple"
      val monitoredActor = system.actorOf(Props[SimpleActor], "simple")
      // this actor is excluded -- we shouldn't be monitoring it
      val unmonitoredActor = system.actorOf(Props[KillableActor], "killable")

      Thread.sleep(1000)

      val counterBeforeKill = TestCounterInterface.foldlByAspect(actorCount)(takeLHS)
      counterBeforeKill.size === 1
      counterBeforeKill must contain(TestCounter(actorCount, 1, List(monitoredActorTag)))

      monitoredActor ! 'stop
      unmonitoredActor ! 'stop

      Thread.sleep(500)

      val counterAfterKill = TestCounterInterface.foldlByAspect(actorCount)(takeLHS)
      counterAfterKill.size === 2
      counterAfterKill must contain(TestCounter(actorCount, 0, List(monitoredActorTag)))
    }
  }
}