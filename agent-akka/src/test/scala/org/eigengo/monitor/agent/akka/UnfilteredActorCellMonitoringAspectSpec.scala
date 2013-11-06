/*
 * Copyright (c) 2013 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eigengo.monitor.agent.akka

import akka.actor.Props
import akka.testkit.TestActorRef
import akka.routing.RoundRobinRouter
import org.eigengo.monitor.{TestCounterInterface, ContainsTag, TestCounter}

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
class UnfilteredActorCellMonitoringAspectSpec extends ActorCellMonitoringAspectSpec(Some("unfiltered.conf")) {
  sequential
  import Aspects._

  val simpleActorClassTag    = "akka:default.org.eigengo.monitor.agent.akka.SimpleActor"

  def simpleActorTagWithTag(tag: String): List[String] = List(tag, simpleActorClassTag)

  "Non-routed actor monitoring" should {

    // records the count of messages received, grouped by message type
    "Record the actor count" in {
      TestCounterInterface.clear()
      val actorName = "counter"
      val tag = "akka://default/"
      val simpleActor = system.actorOf(Props[SimpleActor], actorName)

      // stop(self)
      simpleActor ! 'stop

      Thread.sleep(1500)   // wait for the messages

      TestCounterInterface.foldlByAspect(actorCount)(TestCounter.plus) must contain(TestCounter(actorCount, 0, List(tag)))
    }

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

      Thread.sleep(500)   // wait for the messages

      // we expect to see 2 integers, 1 string and 1 undelivered
      TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus) must contain(TestCounter(deliveredInteger, 2, simpleActorTagWithTag(tag)))
      TestCounterInterface.foldlByAspect(deliveredString)(TestCounter.plus) must contain(TestCounter(deliveredString, 1, simpleActorTagWithTag(tag)))
      // NB: undelivered does not include the actor class name
      TestCounterInterface.foldlByAspect(undelivered)(TestCounter.plus) must contain(TestCounter(undelivered, 1, List(tag)))
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
      val counter = TestCounterInterface.foldlByAspect(queueSize, ContainsTag(tag))(TestCounter.max)(0)
      counter.value must beGreaterThan(count - tolerance)
      counter.tags must containAllOf(simpleActorTagWithTag(tag))
    }

    // keep track of the actor duration; that is the time the receive method takes
    "Record the actor duration" in {
      val actorName = "dur"
      val tag = s"akka://default/user/$actorName"
      val simpleActor = system.actorOf(Props[SimpleActor], actorName)

      simpleActor ! 1000

      Thread.sleep(1100)

      val counter = TestCounterInterface.foldlByAspect(actorDuration, ContainsTag(tag))(TestCounter.max)(0)
      counter.value must beGreaterThan(900)
      counter.value must beLessThan(1100)
      counter.tags must containAllOf(simpleActorTagWithTag(tag))
    }

    "Record the errors" in {
      val actorName = "sodoff"
      val tag = s"akka://default/user/$actorName"
      val simpleActor = TestActorRef[SimpleActor](actorName)

      // match error in receive
      simpleActor ! false

      Thread.sleep(500)   // wait for the messages

      TestCounterInterface.foldlByAspect(actorError)(TestCounter.plus) must contain(TestCounter(actorError, 1, simpleActorTagWithTag(tag)))
    }

  }

  // If we create actor "foo" with round-robin routing with x | x > 1 instances, then each instance's metrics
  // should _also_ be contributed to the supervisor
  //
  // Nota bene the _also_ bit: we record the metrics for each instance _and_ add them to the parent. Put more
  // plainly, the tags for routed actors should include the actor itself and its supervisor
  "Routed actor monitoring" should {

    "Record the message sent to actor" in {
      TestCounterInterface.clear()
      val actorName = "routedFoo"
      val tag = s"akka://default/user/$actorName"
      val count = 10
      val simpleActor = system.actorOf(Props[SimpleActor].withRouter(RoundRobinRouter(nrOfInstances = count)), actorName)

      for (i <- 0 until count) simpleActor ! 100

      Thread.sleep(3500)

      // we expect to see 10 integers for the supervisor and 1 integer for each child
      val supCounter = TestCounterInterface.foldlByAspect(deliveredInteger, ContainsTag(tag))(TestCounter.plus)(0)
      val c1Counter  = TestCounterInterface.foldlByAspect(deliveredInteger, ContainsTag(tag + "/$a"))(TestCounter.plus)(0)

      supCounter.value mustEqual 10
      c1Counter.value mustEqual 1
    }
  }

}
