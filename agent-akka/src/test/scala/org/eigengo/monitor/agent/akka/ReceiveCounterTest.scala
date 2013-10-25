package org.eigengo.monitor.agent.akka

import org.specs2.mutable.SpecificationLike
import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ReceiveCounterTest extends TestKit(ActorSystem()) with SpecificationLike {
  sequential

  val simpleActor = TestActorRef[SimpleActor]

  "Monitoring" should {
    "Record the message sent to actor" in {
      simpleActor ! 1000
      simpleActor ! 2000
      simpleActor ! "Bantha Poodoo!"

      TestCounterInterface.counters("message.Integer") mustEqual 2
      TestCounterInterface.counters("message.String") mustEqual 1
    }

  }

}
