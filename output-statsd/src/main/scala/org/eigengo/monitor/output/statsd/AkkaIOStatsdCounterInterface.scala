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

import org.eigengo.monitor.output.{OutputConfigurationFactory, CounterInterface}
import akka.actor.{Props, ActorSystem}

/**
 * CounterInterface implementation that uses the Akka IO statsd client. Note that it starts its own
 * ``ActorSystem``, which allows you to further configure it.
 */
class AkkaIOStatsdCounterInterface extends CounterInterface {
  val configuration = OutputConfigurationFactory.getAgentCofiguration("statsd")(StatsdOutputConfiguration.apply)
  val outputConfiguration = configuration.outputConfig
  val system = ActorSystem("statsd", configuration.rootConfig)
  val statsd = system.actorOf(Props(new StatsdActor(outputConfiguration.inetSocketAddress, outputConfiguration.prefix, outputConfiguration.constantTags)))

  import StatsdActor._

  def recordExecutionTime(aspect: String, duration: Int, tags: String*): Unit =
    statsd ! ExecutionTime(aspect, duration, tags)

  def recordGaugeValue(aspect: String, value: Int, tags: String*): Unit =
    statsd ! Gauge(aspect, value, tags)

  def decrementCounter(aspect: String, tags: String*): Unit =
    statsd ! Counter(aspect, -1, tags)

  def incrementCounter(aspect: String, delta: Int, tags: String*): Unit =
    statsd ! Counter(aspect, delta, tags)

  def incrementCounter(aspect: String, tags: String*): Unit =
    statsd ! Counter(aspect, +1, tags)
}
