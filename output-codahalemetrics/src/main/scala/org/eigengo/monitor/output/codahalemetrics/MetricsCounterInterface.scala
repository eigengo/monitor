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

import org.eigengo.monitor.output.{OutputConfigurationFactory, CounterInterface}
import com.codahale.metrics.MetricRegistry

/**
* CounterInterface implementation that uses a metrics registry to register the Codahale Metrics updates.
*/
class MetricsCounterInterface extends CounterInterface with MetricsHandler {

  val configuration = OutputConfigurationFactory.getAgentCofiguration("codahalemetrics")(MetricsOutputConfiguration.apply)
  val outputConfiguration = configuration.outputConfig

  // Set the registry
  val provider = RegistryFactory.getRegistryProvider(outputConfiguration.registryClass)
  def registry: MetricRegistry = provider.registry

  // Set the naming marshaller
  def marshaller: NameMarshaller = NameMarshallerFactory.getNameMarshaller(outputConfiguration.namingClass, outputConfiguration.prefix)

  override def recordExecutionTime(aspect: String, duration: Int, tags: String*): Unit =
    updateExecutionTime(aspect, duration, tags)

  override def recordGaugeValue(aspect: String, value: Int, tags: String*): Unit =
    updateGaugeValue(aspect, value, tags)

  override def decrementCounter(aspect: String, tags: String*): Unit =
    updateCounter(aspect, -1, tags)

  override def incrementCounter(aspect: String, delta: Int, tags: String*): Unit =
    updateCounter(aspect, delta, tags)

  override def incrementCounter(aspect: String, tags: String*): Unit =
    updateCounter(aspect, 1, tags)

}

