package org.eigengo.monitor.agent.akka

import org.eigengo.monitor.{TestCounter, TestCounterInterface}
import org.eigengo.monitor.agent.akka.Aspects._
import akka.testkit.TestActorRef

class ActorSamplingSpec extends ActorCellMonitoringAspectSpec(Some("sample.conf")){

  "Actor sampling" should {
    val a = TestActorRef[SimpleActor]("a")
    val b = TestActorRef[WithUnhandledActor]("b")
    val c = TestActorRef[NullTestingActor1]("c")
    val d = TestActorRef[NullTestingActor2]("d")

    "Sample concrete path" in {
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

    "Sample wildcard path" in {

      TestCounterInterface.clear()
      (0 until 497) foreach {_ => c ! 1}   // if we weren't incrementing the counters separately for each actor, then we'd
      (0 until 501) foreach {_ => d ! 1}   // expect 998 messages, and thus 250*4 = 1000 messages logged. But we are -- so
                                           // we expect 125*4 = 500 for actor c, and 126*4 = 504 for actor d
      Thread.sleep(500)   // wait for the messages

      // we expect to see (500/4)*4*2 messages to actor c and d
      val counter3 = TestCounterInterface.foldlByAspect(deliveredInteger)(TestCounter.plus)

      counter3(0).value mustEqual 1004
      counter3(0).tags must contain(d.path.toString)
      counter3(125).tags must contain(d.path.toString)
      counter3(126).tags must contain(c.path.toString)
      counter3.size === 251

    }
  }

}
