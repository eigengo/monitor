package org.eigengo.monitor.agent.akka

import org.specs2.mutable.Specification
import org.eigengo.monitor.{TestCounter, TestCounterInterface}
import org.eigengo.monitor.agent.akka.Aspects._
import akka.testkit.TestActorRef

class ActorSamplingSpec extends ActorCellMonitoringAspectSpec(Some("sample.conf")){

  "Actor sampling" should {
    val a = TestActorRef[SimpleActor]("a")
    val b = TestActorRef[WithUnhandledActor]("b")
    val c = TestActorRef[NullTestingActor1]("c")
    val d = TestActorRef[NullTestingActor2]("d")

    "Sample every 'n' messages (where 'n' is defined in conf file for an individual actor)" in {
      TestCounterInterface.clear()
      (0 until 1000) foreach {_ => a ! 1}

      Thread.sleep(500)   // wait for the messages

      // we expect to see 1000/5 = 200 messages to actor a
      val counter = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)(0)

      counter.value mustEqual 200
      counter.tags must contain(a.path.toString)


      TestCounterInterface.clear()
      (0 until 1000) foreach {_ => b ! 1}
      Thread.sleep(500)   // wait for the messages

      // we expect to see 1000/15 ~=67 messages to actor b (we round up, since logging the first message)
      val counter2 = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)(0)

      counter2.value mustEqual 67
      counter2.tags must contain(b.path.toString)
    }

    "sample every 'n' messages for a broader filter matching on partial path" in {

      TestCounterInterface.clear()
      (0 until 500) foreach {_ => c ! 1}
      (0 until 500) foreach {_ => d ! 1}
      Thread.sleep(500)   // wait for the messages

      // we expect to see 1000/4 = 250 messages to actor c and d
      val counter3 = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)

      counter3(0).value mustEqual 250
      counter3(0).tags must contain(d.path.toString)
      counter3(249).tags must contain(c.path.toString)

    }
  }

}
