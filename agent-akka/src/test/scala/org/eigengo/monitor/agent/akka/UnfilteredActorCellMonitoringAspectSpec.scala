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
class UnfilteredActorCellMonitoringAspectSpec extends ActorCellMonitoringAspectSpec(Some("Unfiltered.conf")) {
  sequential
  import Aspects._
  import TestCounterInterface.takeLHS

  "Non-routed actor monitoring" should {

    // records the count of actors, grouped by simple class name
    "Record the actor count" in {
      withActorOf(Props[SimpleActor]) { ca =>
        TestCounterInterface.foldlByAspect(actorCount, ContainsTag(ca.pathTag))(takeLHS) must contain(TestCounter(actorCount, 1, ca.tags))

        // stop(self)
        ca.actor ! 'stop

        Thread.sleep(500)   // wait for the messages
         // we're sending gauge values here. We want the latest (hence our fold takes the 'head')
        TestCounterInterface.foldlByAspect(actorCount, ContainsTag(ca.pathTag))(takeLHS) must contain(TestCounter(actorCount, 0, ca.tags))
      }
    }

    "Record the actor count using a creator" in {
      TestCounterInterface.clear()
      val props = Props.create(new SimpleActorCreator)
      val simpleActor = system.actorOf(props, "xxx")
      val tags = getTags(simpleActor, props)

      TestCounterInterface.foldlByAspect(actorCount, ContainsTag(tags.head))(takeLHS) must contain(TestCounter(actorCount, 1, tags))
      // stop(self)
      simpleActor ! 'stop

      Thread.sleep(500) // wait for the messages
      // we're sending gauge values here. We want the latest (hence our fold takes the 'head')
      TestCounterInterface.foldlByAspect(actorCount, ContainsTag(tags.head))(takeLHS) must contain(TestCounter(actorCount, 0, tags))
    }

    "Record the actor count of a named actor using a creator" in {
      withActorOf(Props.create(new SimpleActorCreator)) { ca =>
        TestCounterInterface.foldlByAspect(actorCount, ContainsTag(ca.pathTag))(takeLHS) must contain(TestCounter(actorCount, 1, ca.tags))
        // stop(self)
        ca.actor ! 'stop

        Thread.sleep(500) // wait for the messages
        // we're sending gauge values here. We want the latest (hence our fold takes the 'head')
        TestCounterInterface.foldlByAspect(actorCount, ContainsTag(ca.pathTag))(takeLHS) must contain(TestCounter(actorCount, 0, ca.tags))
      }
    }

    // records the count of messages received, grouped by message type
    "Record the message sent to actor" in {
      withActorOf(Props[SimpleActor]) { ca =>
        ca.actor ! 1                 // OK
        ca.actor ! 1                 // OK
        ca.actor ! "Bantha Poodoo!"  // OK
        ca.actor ! 2.2               // original Actor.unhandled

        ca.actor ! 'stop             // OK. stop self

        Thread.sleep(500)   // wait for the messages

        // we expect to see 2 integers, 1 string and 1 undelivered
        TestCounterInterface.foldlByAspect(delivered(1: Int))(TestCounter.plus) must contain(TestCounter(delivered(1: Int), 2, ca.tags))
        TestCounterInterface.foldlByAspect(delivered(""))(TestCounter.plus) must contain(TestCounter(delivered(""), 1, ca.tags))
        // NB: undelivered does not include the actor class name
        TestCounterInterface.foldlByAspect(undelivered)(TestCounter.plus) must contain(TestCounter(undelivered, 1, ca.pathTags ++ ca.systemTags))
      }
    }

    // records the queue size at any given time
    "Record the message queue size" in {
      withActorOf(Props[SimpleActor]) { ca =>
        // because we are using the test ActorSystem, which uses single-threaded dispatcher
        // we expect to see the queue size to reach the count in size. But we must
        // allow for some crafty threading, and allow 3 the queue to be a bit smaller
        val count = 100
        val tolerance = 3
        for (i <- 0 to count) ca.actor ! 10
        Thread.sleep(count * (10 + 2))

        // fold by _max_ over the counters by the ``queueSizeAspect``, tagged with this actor's name
        val counter = TestCounterInterface.foldlByAspect(queueSize, ContainsTag(ca.pathTag))(TestCounter.max)(0)
        counter.value must beGreaterThan(count - tolerance)
        counter.tags must containAllOf(ca.tags)
      }
    }

    // keep track of the actor duration; that is the time the receive method takes
    "Record the actor duration" in {
      withActorOf(Props[SimpleActor]) { ca =>
        ca.actor ! 1000

        Thread.sleep(1100)

        val counter = TestCounterInterface.foldlByAspect(actorDuration, ContainsTag(ca.pathTag))(TestCounter.max)(0)
        counter.value must beGreaterThan(900)
        counter.value must beLessThan(1100)
        counter.tags must containAllOf(ca.tags)
      }
    }

    "Record the errors" in {
      withActorOf(Props[SimpleActor]) { ca =>
        // match error in receive
        ca.actor ! false

        Thread.sleep(500)   // wait for the messages

        TestCounterInterface.foldlByAspect(actorError)(TestCounter.plus) must contain(TestCounter(actorError, 1, ca.tags))
      }
    }

  }

  // If we create actor "foo" with round-robin routing with x | x > 1 instances, then each instance's metrics
  // should _also_ be contributed to the supervisor
  //
  // Nota bene the _also_ bit: we record the metrics for each instance _and_ add them to the parent. Put more
  // plainly, the tags for routed actors should include the actor itself and its supervisor
  "Routed actor monitoring" should {

    "Record the message sent to actor" in {
      val count = 10
      withActorOf(Props[SimpleActor].withRouter(RoundRobinRouter(nrOfInstances = count))) { ca =>
        for (i <- 0 until count) ca.actor ! 100

        Thread.sleep(3500)

        // we expect to see 10 integers for the supervisor and 1 integer for each child
        val supCounter = TestCounterInterface.foldlByAspect(delivered(1: Int), ContainsTag(ca.pathTag))(TestCounter.plus)(0)
        val c1Counter  = TestCounterInterface.foldlByAspect(delivered(1: Int), ContainsTag(ca.pathTag + "/$a"))(TestCounter.plus)(0)

        supCounter.value mustEqual 10
        c1Counter.value mustEqual 1
      }
    }
  }

}
