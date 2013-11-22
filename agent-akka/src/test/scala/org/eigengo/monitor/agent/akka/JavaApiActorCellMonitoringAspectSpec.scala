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
import org.specs2.mutable.SpecificationLike
import java.util.UUID
import akka.actor.ActorRef
import org.specs2.matcher.MatchResult
import org.eigengo.monitor.agent.akka.AbstractJavaApiActorCellMonitoringAspectSpec.{Greeter, GreetPrinter}

class JavaApiActorCellMonitoringAspectSpec
  extends AbstractJavaApiActorCellMonitoringAspectSpec
  with SpecificationLike with ActorCellMonitoringAspectConfigurer with ActorCellMonitoringTaggingConvention {

  sequential
  import Aspects._
  import TestCounterInterface._

  "Counting the number of actors using the java api" should {
    configure("JavaApi.conf")

    val greetPrinterTypeTag = s"akka.type:javaapi.${classOf[GreetPrinter].getCanonicalName}"
    val greeterTypeTag = s"akka.type:javaapi.${classOf[Greeter].getCanonicalName}"
    val outerActorTypeTag = s"akka.type:javaapi.${classOf[OuterActor].getCanonicalName}"
    val innerActorTypeTag = s"akka.type:javaapi.${classOf[InnerActor].getCanonicalName}"

    implicit class PimpedTestCounters(testCounters: List[TestCounter]) {

      def containsCounters(aspect:String, counters: Seq[(Int, String)]): MatchResult[Any] = {
        counters.foldLeft(true){(b, counter) =>
          val aspectIsFine = testCounters.filter(_.aspect == aspect)
          val tagIsFine = aspectIsFine.filter(_.tags.contains(counter._2))
          val valueIsFine = tagIsFine.headOption

        if (aspectIsFine.isEmpty) println(s"Failure: no corresponding aspect: $aspect\n Found: ${testCounters.map(_.aspect).toSet} ")
        if (!aspectIsFine.isEmpty && tagIsFine.isEmpty) println(s"Failure: no corresponding tag: ${counter._2}\n Found: $aspectIsFine")
        if (!tagIsFine.isEmpty && valueIsFine.get.value != counter._1) println(s"Failure: wrong value for tag ${counter._2}, expected: ${counter._1}\n Found ${tagIsFine.map(_.value)}")

          valueIsFine.map(_.value) == Some(counter._1)
        } must beTrue
      }

    }

    // records the count of actors, grouped by simple class name
    "Record the actor creation" in {
      TestCounterInterface.clear()
      Thread.sleep(100L)
      val createdCounters = TestCounterInterface.foldlByAspect(actorCount)(takeLHS)
      createdCounters containsCounters(actorCount, Seq(
        (2, greetPrinterTypeTag),  // one named, one unnamed.
        (1, greeterTypeTag),
        (1, outerActorTypeTag),
        (1, innerActorTypeTag)))
    }


    "Group actors appropriately when measuring actor count" in {

      (0 until 5) foreach {_ => outerActor ! UUID.randomUUID()}

      Thread.sleep(1000L)
      val testCounters: List[TestCounter] = TestCounterInterface.foldlByAspect(actorCount)(takeLHS)
      testCounters containsCounters(actorCount, Seq(
        (2, greetPrinterTypeTag),
        (1, greeterTypeTag),
        (1, outerActorTypeTag),
        (6, innerActorTypeTag)))
    }

    "Record messages sent to an actor with an 'invisible' runtime type" in {
      TestCounterInterface.clear()
      innerActor ! 1
      Thread.sleep(1000L)

      TestCounterInterface.foldlByAspect(delivered(1: Int))(TestCounter.plus) containsCounters (delivered(1: Int), Seq(
        (1, innerActorTypeTag)
      ))
    }

    "Record messages sent to an ActorSelection" in {
      TestCounterInterface.clear()
            val innerActorSelection = system.actorSelection("/javaapi/user/$b/$a")
            innerActorSelection.tell(1, ActorRef.noSender)
      Thread.sleep(1000L)

      TestCounterInterface.foldlByAspect(delivered(1: Int))(TestCounter.plus)containsCounters (delivered(1: Int), Seq(
        (1, innerActorTypeTag)
      ))
    }.pendingUntilFixed("this may just be failing because of actor selection syntax. This isn't needed atm, but is a test we should have for completeness")

    "Record actor death" in {
      TestCounterInterface.clear()

      system.shutdown()
      Thread.sleep(1000L)

      val testCounters: List[TestCounter] = TestCounterInterface.foldlByAspect(actorCount)(takeLHS)
      testCounters containsCounters(actorCount, Seq(
        (0, greetPrinterTypeTag),
        (0, greeterTypeTag),
        (0, outerActorTypeTag),
        (0, innerActorTypeTag)
      ))
    }

  }

}
