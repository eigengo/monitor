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

class JavaApiActorCellMonitoringAspectSpec
  extends AbstractJavaApiActorCellMonitoringAspectSpec
  with SpecificationLike with ActorCellMonitoringAspectConfigurer with ActorCellMonitoringTaggingConvention {

  sequential
  import Aspects._
  import TestCounterInterface._

  "Counting the number of actors using the java api" should {
    configure("JavaApi.conf")
    val unnamedGreetPrinterTags = getTags(unnamedGreetPrinter, unnamedGreetPrinterProps)
    val namedGreetPrinterTags = getTags(greetPrinter, greetPrinterProps)
    val greeterTags = getTags(greeter, greeterProps)
//    val outerActorTags = getTags(outerActor, outerActorProps)
//    val innerActorTags = getTags(innerActor, innerActorProps) // = akka.path:/javaapi/user/$c, akka.type:javaapi.akka.actor.Actor
    val outerActorTags = List("org.eigengo.monitor.agent.akka.AbstractJavaApiActorCellMonitoringAspectSpec.OuterActor")
    val innerActorTags = List("org.eigengo.monitor.agent.akka.AbstractJavaApiActorCellMonitoringAspectSpec.InnerActor")

    def tagsContain(tags: Seq[String], check: String): MatchResult[Any] = {
      val doesContain = tags.exists(_.contains(check))
      if (!doesContain) println("TEST FAILURE: "+tags + "\n doesn't contain\n  "+check)
      doesContain === true
    }


    "Tag actors with appropriate names" in {
      tagsContain(unnamedGreetPrinterTags, "GreetPrinter")
      tagsContain(namedGreetPrinterTags, "GreetPrinter")
      tagsContain(greeterTags, "Greeter")
      tagsContain(outerActorTags, "OuterActor")
      tagsContain(innerActorTags, "InnerActor")
    }

    // records the count of actors, grouped by simple class name
    "Record the actor creation" in {

      Thread.sleep(100L)
      val createdCounters = TestCounterInterface.foldlByAspect(actorCount)(takeLHS)
      createdCounters must containAllOf(Seq(
//        TestCounter(actorCount, 1, unnamedGreetPrinterTags),
        TestCounter(actorCount, 1, namedGreetPrinterTags),
        TestCounter(actorCount, 1, greeterTags),
        TestCounter(actorCount, 1, outerActorTags),
        TestCounter(actorCount, 1, innerActorTags)))
    }


    "Group actors appropriately when measuring actor count" in {

      (0 until 5) foreach {_ => outerActor ! UUID.randomUUID()}

      Thread.sleep(1000L)
      TestCounterInterface.foldlByAspect(actorCount)(takeLHS) must containAllOf(Seq(
        TestCounter(actorCount, 1, unnamedGreetPrinterTags),
        TestCounter(actorCount, 1, namedGreetPrinterTags),
        TestCounter(actorCount, 1, greeterTags),
        TestCounter(actorCount, 1, outerActorTags),
        TestCounter(actorCount, 6, innerActorTags)))
    }

    "Record messages sent to an ActorSelection" in {
      TestCounterInterface.clear()
      val innerActorSelection = system.actorSelection("/javaapi/user/$b/$e") // fixing previous tests may break this.
      innerActorSelection.tell(1, ActorRef.noSender)
      Thread.sleep(1000L)

      TestCounterInterface.foldlByAspect(delivered(1: Int))(TestCounter.plus) must containAllOf(Seq(
        TestCounter(actorCount, 1, innerActorTags)))
    }

    "Record actor death" in {
      TestCounterInterface.clear()

      system.shutdown()
      Thread.sleep(1000L)

      TestCounterInterface.foldlByAspect(actorCount)(takeLHS) must containAllOf(Seq(
        TestCounter(actorCount, 0, unnamedGreetPrinterTags),
        TestCounter(actorCount, 0, namedGreetPrinterTags),
        TestCounter(actorCount, 0, greeterTags),
        TestCounter(actorCount, 0, outerActorTags),
        TestCounter(actorCount, 0, innerActorTags)))
    }

  }

}
