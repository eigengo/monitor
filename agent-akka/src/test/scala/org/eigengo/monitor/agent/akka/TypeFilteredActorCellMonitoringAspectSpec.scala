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

import akka.testkit.TestActorRef
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
class TypeFilteredActorCellMonitoringAspectSpec extends ActorCellMonitoringAspectSpec(Some("type-filter.conf")) {
  import Aspects._

  "With path included filter" should {
    val a = TestActorRef[SimpleActor]("a")
    val b = TestActorRef[WithUnhandledActor]("b")
    val c = TestActorRef[NullTestingActor1]("c")
    val d = TestActorRef[NullTestingActor2]("d")
    val e = TestActorRef[NullTestingActor3]("e")

    "Skip non-included actor" in {
      TestCounterInterface.clear()
      a ! 100
      b ! 100

      Thread.sleep(500)   // wait for the messages

      // we expect to see 2 integers, 1 string and 1 undelivered
      val counter = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)(0)

      counter.value mustEqual 1
      counter.tags must contain(a.path.toString)
    }

    "Sample concrete path of included actors" in {
      TestCounterInterface.clear()
      (0 until 1000) foreach {_ => a ! 1}

      Thread.sleep(500)   // wait for the messages

      // we expect to see (1000/5)*5 messages to actor a
      val counter = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)

      counter(0).value mustEqual 1000
      counter(0).tags must contain(a.path.toString)
      counter.size === 200


      TestCounterInterface.clear()
      (0 until 1000) foreach {_ => b ! 1}
      Thread.sleep(500)   // wait for the messages

      // we expect to see (1000/15 ~=67)*15 = 1005 messages to actor b (we round up, since logging the first message)
      val counter2 = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)

      counter2(0).value mustEqual 1005
      counter2(0).tags must contain(b.path.toString)
      counter2.size === 67
    }


    "Ignore non-included actors" in {

      TestCounterInterface.clear()
      c ! 1

      val counter3 = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)

      counter3.size === 0

    }
  }

}
