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

class PerformanceSpec extends Specification {

  def timed[U](repetitions: Int)(f: => U): Long = {
    val start = System.currentTimeMillis()
    for (_ <- 0 to repetitions) f
    System.currentTimeMillis() - start
  }

  "Performance of the Statsd client" should {
    val dog = new StatsdCounterInterface()
    val aio = new AkkaIOStatsdCounterInterface()

    "Be fast" in {
      val count = 100000
      println("DOG " + timed(count)(dog.incrementCounter("foo")))
      println("AIO " + timed(count)(aio.incrementCounter("foo")))

      Thread.sleep(10000)

      success
    }

  }


}
