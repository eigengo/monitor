package org.eigengo.monitor.agent.akka

import org.specs2.mutable.SpecificationLike
import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

/**
 * Checks that the ``ActorCellMonitoringAspect`` records the required information.
 *
 * Here, we check that we can successfully record the message counts, and that we can
 * monitor the queue size.
 */
@RunWith(classOf[JUnitRunner])
class ActorCellCounterTest extends TestKit(ActorSystem()) with SpecificationLike {
  sequential

  "Monitoring" should {
    val messageIntegerAspect = "akka.message.Integer"
    val messageStringAspect  = "akka.message.String"
    val queueSizeAspect      = "akka.queue.size"
    val actorDurationAspect  = "akka.actor.duration"

    // records the count of messages received, grouped by message type
    "Record the message sent to actor" in {
      val actorName = "foo"
      val tag = s"akka://default/user/$actorName"
      val simpleActor = TestActorRef[SimpleActor](actorName)

      simpleActor ! 1
      simpleActor ! 1
      simpleActor ! "Bantha Poodoo!"

      // we expect to see 2 integers and 1 string in total
      TestCounterInterface.foldlByAspect(messageIntegerAspect)(TestCounter.plus) must contain(TestCounter(messageIntegerAspect, 2, List(tag)))
      TestCounterInterface.foldlByAspect(messageStringAspect)(TestCounter.plus) must contain(TestCounter(messageStringAspect, 1, List(tag)))
    }

    // records the queue size at any given time
    "Record the message queue size" in {
      val actorName = "bar"
      val tag = s"akka://default/user/$actorName"
      val simpleActor = system.actorOf(Props[SimpleActor], actorName)

      // because we are using the test ActorSystem, which uses single-threaded dispatcher
      // we expect to see the queue size to reach the count in size. But we must
      // allow for some crafty threading, and allow 3 the queue to be a bit smaller
      val count = 100
      val tolerance = 3
      for (i <- 0 to count) simpleActor ! 10
      Thread.sleep(count * (10 + 2))

      // fold by _max_ over the counters by the ``queueSizeAspect``, tagged with this actor's name
      val counter = TestCounterInterface.foldlByAspect(queueSizeAspect, SingleTag(tag))(TestCounter.max)(0)
      counter.value must beGreaterThan(count - tolerance)
      counter.tags must containAllOf(List(tag))
    }

    "Record the actor duration" in {
      val actorName = "dur"
      val tag = s"akka://default/user/$actorName"
      val simpleActor = system.actorOf(Props[SimpleActor], actorName)

      simpleActor ! 1000

      Thread.sleep(1100)

      val counter = TestCounterInterface.foldlByAspect(actorDurationAspect, SingleTag(tag))(TestCounter.max)(0)
      counter.value must beGreaterThan(900)
      counter.value must beLessThan(1100)
      counter.tags must containAllOf(List(tag))
    }

  }

}
