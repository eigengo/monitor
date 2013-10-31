package org.eigengo.monitor.agent.akka

import akka.testkit.{TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike
import com.typesafe.config.ConfigFactory
import org.eigengo.monitor.{TestCounter, TestCounterInterface}

/**
 * Checks that the ``ActorCellMonitoringAspect`` records the required information, particularly with the applied
 * filtering; that is, the included and excluded actors
 *
 * Here, we check that we can successfully record the message counts, and that we can
 * monitor the queue size.
 *
 * When running from your IDE, remember to include the -javaagent JVM parameter:
 * -javaagent:$HOME/.m2/repository/org/aspectj/aspectjweaver/1.7.3/aspectjweaver-1.7.3.jar
 * in my case -javaagent:/Users/janmachacek/.m2/repository/org/aspectj/aspectjweaver/1.7.3/aspectjweaver-1.7.3.jar
 */
class FilteredActorCellMonitoringAspectSpec extends TestKit(ActorSystem()) with SpecificationLike {
  import Aspects._
/*
  "With path included filter" should {
    val aspect: ActorCellMonitoringAspect = ActorCellMonitoringAspect.aspectOf()
    aspect.setAgentConfiguration(AkkaAgentConfiguration(ConfigFactory.load("META-INF/monitor/filtered1.conf")))
    val a = TestActorRef[SimpleActor]("a")
    val b = TestActorRef[SimpleActor]("b")

    "Skip non-included actor" in {
      a ! 100
      b ! 100

      // we expect to see 2 integers, 1 string and 1 undelivered
      val counter = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)(0)

      counter.value mustEqual 1
      counter.tags must contain(a.toString())
    }
  }
*/
}
