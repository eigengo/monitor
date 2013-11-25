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

import com.codahale.metrics.{MetricRegistry, Gauge, Metric, MetricFilter}
import org.specs2.mutable.Specification
import scala.collection.JavaConversions._

class UpdatableGaugeSpec extends Specification {

  "The UpdatableGauge" should {

    "be able to be created and the value updated" in {
      val gauge = new UpdatableGauge[Int]
      gauge.setValue(5)
      gauge.getValue should be equalTo 5
    }

    "be able to be registered as a Gauge in a MetricsRegistry" in {
      val gauge = new UpdatableGauge[Int]
      gauge.setValue(5)

      val registry = new MetricRegistry()
      registry.register("test-gauge", gauge)

      val found = registry.getGauges(new MetricFilter {
        def matches(regName: String, metric: Metric): Boolean =
          regName.equals("test-gauge")
      })

      found.size must be equalTo 1
      // Need to pull out as an Int type of Gauge since gauges can be applied to different types
      found.head._2.asInstanceOf[Gauge[Int]].getValue must be equalTo 5
    }
  }
}
