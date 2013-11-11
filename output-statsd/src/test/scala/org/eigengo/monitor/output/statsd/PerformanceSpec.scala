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
package org.eigengo.monitor.output.statsd

import org.specs2.mutable.Specification
import akka.actor.{Props, ActorSystem}
import akka.routing.RoundRobinRouter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Checks the performance of our statsd clients
 */
class PerformanceSpec extends Specification {
  val system = ActorSystem()
  val map = new ConcurrentHashMap[String, AtomicInteger]()
  private def recordCount(payload: String): Unit = {
    map.putIfAbsent(payload, new AtomicInteger(1))
    map.get(payload).incrementAndGet()
  }
  val statsd = system.actorOf(Props(new StatsdRecorderActor(12345, recordCount)).withRouter(RoundRobinRouter(nrOfInstances = 5)))
  sequential

  def timed[U](repetitions: Int)(f: => U): Long = {
    val start = System.currentTimeMillis()
    for (_ <- 1 until repetitions) f
    System.currentTimeMillis() - start
  }

  "Performance of the Statsd client" should {
    val dog = new StatsdCounterInterface()
    val aio = new AkkaIOStatsdCounterInterface()

    "Be fast" in {
      val count = 10000
      val dogTime = timed(count)(dog.incrementCounter("dog"))
      val aioTime = timed(count)(aio.incrementCounter("aio"))

      // wait for all messages
      Thread.sleep(2000)

      // we should see 2 entries, each with $count values
      import scala.collection.JavaConversions._
      map.size() mustEqual 2
      map.elements().toList.map(_.intValue()) must containAllOf(List(count, count))

      // we expect to be at least 5 times faster
      aioTime < (dogTime / 5)
    }

  }

}
