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

import com.codahale.metrics.MetricRegistry

/**
 * This trait is used to provide a registry for statistics to be collected
 */
trait RegistryProvider {
  val registry: MetricRegistry
}

/**
 * The default registry provider which uses the registry
 * that is defined in the ``Registry`` object.
 */
class DefaultRegistryProvider extends RegistryProvider {
  val registry = DefaultRegistryProvider.registry
}

/**
 * The default provided registry which allows access for
 * reporting purposes
 */
object DefaultRegistryProvider {
  val registry = new MetricRegistry
}

object RegistryFactory {
  /**
   * Create an instance of a ``RegistryProvider`` given the passed class
   * @param clazz the fully qualified name of the class to instantiate
   * @return an instance of ``RegistryProvider``
   */
  private[metrics] def getRegistryProvider(clazz: String): RegistryProvider = {
    try {
      Class.forName(clazz).newInstance.asInstanceOf[RegistryProvider]
    }
    catch {
      case e: ReflectiveOperationException => new DefaultRegistryProvider
      case e: ClassCastException => new DefaultRegistryProvider
    }
  }
}