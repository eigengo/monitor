package org.eigengo.monitor.agent.akka

import org.eigengo.monitor.{TestCounter, TestCounterInterface}
import akka.actor.Props

class CountFilteredActorCellMonitoringAspectSpec extends ActorCellMonitoringAspectSpec(Some("CountFiltered.conf")) {
  sequential
  import Aspects._
  import TestCounterInterface.takeLHS

  "Actor count monitoring" should {

    "Not record the count of exluded actors" in {
      TestCounterInterface.clear()
      withActorsOf(Props[SimpleActor], Props[KillableActor]) { (monitored, unmonitored) =>
        val counterBeforeKill = TestCounterInterface.foldlByAspect(actorCount)(takeLHS)
        counterBeforeKill.size === 1
        counterBeforeKill must contain(TestCounter(actorCount, 1, monitored.tags))
  
        monitored.actor ! 'stop
        unmonitored.actor ! 'stop
  
        Thread.sleep(500)
  
        val counterAfterKill = TestCounterInterface.foldlByAspect(actorCount)(takeLHS)
        counterAfterKill.size === 2
        counterAfterKill must contain(TestCounter(actorCount, 0, monitored.tags))
      }
    }

    "Shutdown system" in {
      system.shutdown()
      success
    }
  }
}