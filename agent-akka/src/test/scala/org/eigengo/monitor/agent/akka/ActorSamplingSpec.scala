package org.eigengo.monitor.agent.akka

import org.specs2.mutable.Specification
import org.eigengo.monitor.{TestCounter, TestCounterInterface}
import org.eigengo.monitor.agent.akka.Aspects._
import akka.testkit.TestActorRef

class ActorSamplingSpec extends ActorCellMonitoringAspectSpec(Some("sample.conf")){

  "Actor sampling" should {
    val a = TestActorRef[SimpleActor]("a")
    val b = TestActorRef[WithUnhandledActor]("b")

    "Sample every 'n' messages (where 'n' is defined in conf file)" in {
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
  }

}
