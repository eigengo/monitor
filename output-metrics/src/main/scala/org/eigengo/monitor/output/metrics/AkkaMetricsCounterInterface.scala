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
package org.eigengo.monitor.output.metrics

import akka.actor.{ActorRef, ActorSystem, Props}
import org.eigengo.monitor.output.{OutputConfigurationFactory, CounterInterface}

/**
 * CounterInterface implementation that uses an Akka actor to handle the Codahale Metrics updates. Note that it starts its own
 * ``ActorSystem``, which allows you to further configure it.
 */
class AkkaMetricsCounterInterface extends CounterInterface {

  val configuration = OutputConfigurationFactory.getAgentCofiguration("metrics")(MetricsOutputConfiguration.apply)
  val outputConfiguration = configuration.outputConfig

  import MetricsActor._

  // These are all lazy instantiation due to issues initializing them at the same time of this class construction.
  // TODO: This still has an issue that is the same in the ``AkkaIOStatsdCounterInterface`` where this class is loaded
  // on initialization of the running application's actor system. When the 'metrics' system is loaded we run into issues
  // with threading.
  lazy val system = ActorSystem("metrics")
  lazy val metrics: ActorRef = {
    system.actorOf(Props(classOf[MetricsActor],
      RegistryFactory.getRegistryProvider(outputConfiguration.registryClass),
      NameMarshallerFactory.getNameMarshaller(outputConfiguration.namingClass, outputConfiguration.prefix)))
  }

  def recordExecutionTime(aspect: String, duration: Int, tags: String*): Unit =
    metrics ! ExecutionTime(aspect, duration, tags)

  def recordGaugeValue(aspect: String, value: Int, tags: String*): Unit =
    metrics ! Gauge(aspect, value, tags)

  def decrementCounter(aspect: String, tags: String*): Unit =
    metrics ! Counter(aspect, -1, tags)

  def incrementCounter(aspect: String, delta: Int, tags: String*): Unit =
    metrics ! Counter(aspect, delta, tags)

  def incrementCounter(aspect: String, tags: String*): Unit =
    metrics ! Counter(aspect, 1, tags)

}
