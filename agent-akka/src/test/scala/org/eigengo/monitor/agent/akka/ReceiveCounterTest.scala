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
      val simpleActor = TestActorRef[SimpleActor]("foo")
      simpleActor ! 1000
      simpleActor ! 2000
      simpleActor ! "Bantha Poodoo!"

      TestCounterInterface.counters("message.Integer") mustEqual TestCounter(2, List("foo"))
      TestCounterInterface.counters("message.String") mustEqual TestCounter(1, List("foo"))
    }

    "Record the message queue size" in {
      val simpleActor = system.actorOf(Props[SimpleActor], "bar")
      for (i <- 0 to 100) simpleActor ! 10
      Thread.sleep(100 * (10 + 2))

      println(TestCounterInterface.counters)

      success
    }

  }

}
