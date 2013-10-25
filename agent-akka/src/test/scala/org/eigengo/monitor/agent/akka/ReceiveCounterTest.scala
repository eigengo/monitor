package org.eigengo.monitor.agent.akka

import org.specs2.mutable.SpecificationLike
import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ReceiveCounterTest extends TestKit(ActorSystem()) with SpecificationLike {
  sequential

  "Monitoring" should {

    "Record the message sent to actor" in {
      val simpleActor = TestActorRef[SimpleActor]
      simpleActor ! 1000
      simpleActor ! 2000
      simpleActor ! "Bantha Poodoo!"

      TestCounterInterface.counters("message.Integer") mustEqual 2
      TestCounterInterface.counters("message.String") mustEqual 1
    }

    "Record the message queue size" in {
      val simpleActor = system.actorOf(Props[SimpleActor])
      for (i <- 0 to 100) simpleActor ! 1000

      println(TestCounterInterface.counters)

      success
    }

  }

}
