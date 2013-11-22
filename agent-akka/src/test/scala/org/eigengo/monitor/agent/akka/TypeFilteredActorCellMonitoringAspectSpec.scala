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

import org.eigengo.monitor.{TestCounter, TestCounterInterface}
import akka.actor.Props

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
class TypeFilteredActorCellMonitoringAspectSpec extends ActorCellMonitoringAspectSpec(Some("TypeFiltered.conf")) {
  import Aspects._

  "With path included filter" should {
    val a = createActor(Props[SimpleActor])
    val b = createActor(Props[WithUnhandledActor])
    val c = createActor(Props[NullTestingActor1])
    val d = createActor(Props[NullTestingActor2])
    val e = createActor(Props[NullTestingActor3])

    "Skip non-included actor" in {
      TestCounterInterface.clear()
      a.actor ! 100
      b.actor ! 100

      Thread.sleep(500)   // wait for the messages

      // we expect to see 2 integers, 1 string and 1 undelivered
      val counter = TestCounterInterface.foldlByAspect(delivered(1: Int))(TestCounter.plus)
      counter.size mustEqual 1                          // "akka:*.org.eigengo.monitor.agent.akka.SimpleActor" sampling rate is 1
      counter(0).value mustEqual 1                      // and WithUnhandledActor isn't included
      counter(0).tags must contain(a.pathTag)           // so this should be true whether or not sampling is working.
    }

    "Sample concrete path of included+sampled actors" in {
      TestCounterInterface.clear()
      (0 until 1000) foreach {_ => c.actor ! 1}

      Thread.sleep(500)   // wait for the messages

      // we expect to see (1000/5)*5 messages to actor a
      val counter = TestCounterInterface.foldlByAspect(delivered(1: Int))(TestCounter.plus)

      counter(0).value mustEqual 1000                   // we include this actor, and sample one in 5
      counter(0).tags must contain(c.pathTag)
      counter.size === 200

      TestCounterInterface.clear()                     // we include this actor, and sample one in 15
      (0 until 1000) foreach {_ => d.actor ! 1}
      Thread.sleep(500)   // wait for the messages

      // we expect to see (1000/15 ~=67)*15 = 1005 messages to actor b (we round up, since logging the first message)
      val counter2 = TestCounterInterface.foldlByAspect(delivered(1: Int))(TestCounter.plus)

      counter2(0).value mustEqual 1005
      counter2(0).tags must contain(d.pathTag)
      counter2.size === 67
    }

    "Skip sampled non-included actors" in {
      TestCounterInterface.clear()                      // we don't include this actor in the monitoring
      e.actor ! 1
      val monitoredIntegerMessages = TestCounterInterface.foldlByAspect(delivered(1: Int))(TestCounter.plus)
      monitoredIntegerMessages.size === 0
    }

    "Shutdown system" in {
      system.shutdown()
      success
    }
  }

}
