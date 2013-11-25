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

import com.codahale.metrics.Gauge

/**
 * This is a 'push' style of gauge
 * @tparam T
 */
class UpdatableGauge[T] extends Gauge[T] {

  var value: T = _

  /**
   * Set the value
   * @param newValue
   */
  def setValue(newValue: T) = {
    value = newValue
  }

  /**
   * Returns the metric's current value.
   *
   * @return the metric's current value
   */
  override def getValue: T = value
}
