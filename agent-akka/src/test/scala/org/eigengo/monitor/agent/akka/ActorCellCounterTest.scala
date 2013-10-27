package org.eigengo.monitor.agent.akka

import org.specs2.mutable.SpecificationLike
import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
import akka.routing.{RouterConfig, RoundRobinRouter}
import org.specs2.execute.Result

/**
 * Checks that the ``ActorCellMonitoringAspect`` records the required information.
 *
 * Here, we check that we can successfully record the message counts, and that we can
 * monitor the queue size.
 *
 * When running from your IDE, remember to include the -javaagent JVM parameter:
 * -javaagent:$HOME/.m2/repository/org/aspectj/aspectjweaver/1.7.3/aspectjweaver-1.7.3.jar
 * in my case -javaagent:/Users/janmachacek/.m2/repository/org/aspectj/aspectjweaver/1.7.3/aspectjweaver-1.7.3.jar
 */
@RunWith(classOf[JUnitRunner])
class ActorCellCounterTest extends TestKit(ActorSystem()) with SpecificationLike {
  sequential
  val deliveredIntegerAspect = "akka.actor.delivered.Integer"
  val deliveredStringAspect  = "akka.actor.delivered.String"
  val undeliveredAspect      = "akka.actor.undelivered"
  val queueSizeAspect        = "akka.queue.size"
  val actorDurationAspect    = "akka.actor.duration"
  val actorErrorAspect       = "akka.actor.error"

  "Non-routed actor monitoring" should {

    // records the count of messages received, grouped by message type
    "Record the message sent to actor" in {
      val actorName = "foo"
      val tag = s"akka://default/user/$actorName"
      val simpleActor = TestActorRef[SimpleActor](actorName)

      simpleActor ! 1                 // OK
      simpleActor ! 1                 // OK
      simpleActor ! "Bantha Poodoo!"  // OK
      simpleActor ! 2.2               // original Actor.unhandled

      simpleActor ! 'stop             // OK. stop self

      // we expect to see 2 integers, 1 string and 1 undelivered
      TestCounterInterface.foldlByAspect(deliveredIntegerAspect)(TestCounter.plus) must contain(TestCounter(deliveredIntegerAspect, 2, List(tag)))
      TestCounterInterface.foldlByAspect(deliveredStringAspect)(TestCounter.plus) must contain(TestCounter(deliveredStringAspect, 1, List(tag)))
      TestCounterInterface.foldlByAspect(undeliveredAspect)(TestCounter.plus) must contain(TestCounter(undeliveredAspect, 1, List(tag)))
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
      val counter = TestCounterInterface.foldlByAspect(queueSizeAspect, ExactTag(tag))(TestCounter.max)(0)
      counter.value must beGreaterThan(count - tolerance)
      counter.tags must containAllOf(List(tag))
    }

    // keep track of the actor duration; that is the time the receive method takes
    "Record the actor duration" in {
      val actorName = "dur"
      val tag = s"akka://default/user/$actorName"
      val simpleActor = system.actorOf(Props[SimpleActor], actorName)

      simpleActor ! 1000

      Thread.sleep(1100)

      val counter = TestCounterInterface.foldlByAspect(actorDurationAspect, ExactTag(tag))(TestCounter.max)(0)
      counter.value must beGreaterThan(900)
      counter.value must beLessThan(1100)
      counter.tags must containAllOf(List(tag))
    }

    "Record the errors" in {
      val actorName = "sodoff"
      val tag = s"akka://default/user/$actorName"
      val simpleActor = TestActorRef[SimpleActor](actorName)

      // match error in receive
      simpleActor ! false

      TestCounterInterface.foldlByAspect(actorErrorAspect)(TestCounter.plus) must contain(TestCounter(actorErrorAspect, 1, List(tag)))
    }

  }

  // If we create actor "foo" with round-robin routing with x | x > 1 instances, then each instance's metrics
  // should _also_ be contributed to the supervisor
  //
  // Nota bene the _also_ bit: we record the metrics for each instance _and_ add them to the parent. Put more
  // plainly, the tags for routed actors should include the actor itself and its supervisor
  "Routed actor monitoring" should {

    "Record the message sent to actor" in {
      val actorName = "routedFoo"
      val tag = s"akka://default/user/$actorName"
      val count = 10
      val simpleActor = system.actorOf(Props[SimpleActor].withRouter(RoundRobinRouter(nrOfInstances = count)), actorName)

      for (i <- 0 until count) simpleActor ! 100

      Thread.sleep(2500)

      // we expect to see 10 integers for the supervisor and 1 integer for each child
      val supCounter = TestCounterInterface.foldlByAspect(deliveredIntegerAspect, ContainsTag(tag))(TestCounter.plus)(0)
      val c1Counter  = TestCounterInterface.foldlByAspect(deliveredIntegerAspect, ExactTag(tag + "/$a"))(TestCounter.plus)(0)

      supCounter.value mustEqual 10
      c1Counter.value mustEqual 1
    }
  }

}
