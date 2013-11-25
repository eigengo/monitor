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
package org.eigengo.monitor.output.codahalemetrics

import org.specs2.mutable.Specification
import scala.collection.JavaConversions._

/**
 * Checks the performance of our Codahale metrics
 */
class MetricsCollectionSpec extends Specification {

  sequential

  def timed[U](repetitions: Int)(f: => U): Long = {
    val start = System.currentTimeMillis()
    for (_ <- 0 until repetitions) f
    System.currentTimeMillis() - start
  }

  "The metrics client" should {
    val aio = new AkkaMetricsCounterInterface()
    val registry = DefaultRegistryProvider.registry

    "accurately collect counter statistics" in {
      val count = 5000
      timed(count)(aio.incrementCounter("akka.actor.delivered.testmessage", "akka://server/user/myactor"))
      // wait for all messages
      Thread.sleep(500)

      // Get the metrics for the registry
      aio.metrics
      val system = aio.system
      val metrics = registry.getCounters

      // we should see 1 entry, with $count
      metrics.size mustEqual 1
      metrics.head._2.getCount must be equalTo 5000

      timed(count)(aio.decrementCounter("akka.actor.delivered.testmessage", "akka://server/user/myactor"))
      // wait for all messages
      Thread.sleep(500)

      // we should see 1 entry, with 0
      metrics.size mustEqual 1
      metrics.head._2.getCount must be equalTo 0
    }

    "accurately collect gauge statistics" in {

      val count = 5000
      timed(count)(aio.recordGaugeValue("akka.queue.size", 125, "akka://server/user/myactor"))
      // wait for all messages
      Thread.sleep(500)

      // Get the metrics for the registry
      val metrics = registry.getGauges

      // we should see 1 entry, with $count values
      metrics.size mustEqual 1
      metrics.head._2.getValue.asInstanceOf[Int] must be equalTo 125
    }

    "accurately collect timing statistics" in {

      val count = 5000
      timed(count)(aio.recordExecutionTime("akka.actor.duration", 125, "akka://server/user/myactor"))
      // wait for all messages
      Thread.sleep(500)

      // Get the metrics for the registry
      val metrics = registry.getTimers

      // we should see 1 entry, with $count values
      metrics.size mustEqual 1
      metrics.head._2.getCount must be equalTo 5000
    }
  }

}
