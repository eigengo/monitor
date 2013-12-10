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

import akka.actor.Actor
import com.codahale.metrics.MetricRegistry

/**
 * Companion object for the ``MetricsActor``, containing the messages that it can handle
 */
object MetricsActor {

  /**
   * Common supertype for all ``MetricsStatistic``s
   */
  sealed trait MetricsStatistic

  /**
   * A counter increments or decrements the specified ``aspect`` by ``delta``, optionally containing
   * non-empty ``tags``.
   *
   * @param aspect the aspect identifying the counter
   * @param delta the delta
   * @param tags the optional tags (DD extension)
   */
  case class Counter(aspect: String, delta: Int, tags: Seq[String] = Nil) extends MetricsStatistic

  /**
   * A gauge records a ``value`` identified by ``aspect``, optionally containing
   * non-empty ``tags``.
   *
   * @param aspect the aspect identifying the gauge
   * @param value the gauge value
   * @param tags the optional tags (DD extension)
   */
  case class Gauge(aspect: String, value: Int, tags: Seq[String] = Nil) extends MetricsStatistic

  /**
   * An execution execution time records time in milliseconds for the given ``aspect``, with
   * optional ``tags``.
   *
   * @param aspect the aspect identifying the execution time
   * @param timeMs the time in milliseconds
   * @param tags the optional tags (DD extension)
   */
  case class ExecutionTime(aspect: String, timeMs: Int, tags: Seq[String] = Nil) extends MetricsStatistic

}

/**
 * Sends the received ``MetricsStatistic`` messages to the Codahale metrics registry.
 *
 * @param provider the ``RegistryProvider`` which provides the registry
 * @param nameMarshaller the ``NameMarshaller`` which is responsible for generating Codahale friendly names
 */
class MetricsActor(provider: RegistryProvider, nameMarshaller: NameMarshaller) extends Actor with MetricsHandler {

  // Set the registry
  def registry: MetricRegistry = provider.registry

  // Set the naming marshaller
  def marshaller: NameMarshaller = nameMarshaller

  import MetricsActor._

  def receive: Receive = {
    case Counter(aspect, delta, tags) => updateCounter(aspect, delta, tags)
    case Gauge(aspect, value, tags) => updateGaugeValue(aspect, value, tags)
    case ExecutionTime(aspect, timeMs, tags) => updateExecutionTime(aspect, timeMs, tags)
  }
}
