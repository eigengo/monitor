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
import org.specs2.mutable.Specification
import akka.actor.ActorSystem

class RegistryProviderSpec extends Specification {

  implicit val system = ActorSystem()

  "The registry functionality" should {

    "allow for the creation of a valid RegistryProvider" in {
      val provider = RegistryFactory.getRegistryProvider("org.eigengo.monitor.output.metrics.TestRegistryProvider")
      provider must beAnInstanceOf[TestRegistryProvider]
    }

    "create a default RegistryProvider when an invalid class is given" in {
      val provider = RegistryFactory.getRegistryProvider("myclass")
      provider must beAnInstanceOf[DefaultRegistryProvider]
    }
  }

  step {
    system.shutdown
  }
}

class TestRegistryProvider extends RegistryProvider {
  val registry = new MetricRegistry()
}