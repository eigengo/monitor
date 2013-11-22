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

import org.specs2.mutable.Specification
import org.eigengo.monitor.output.OutputConfigurationFactory

class MetricsOutputConfigurationSpec extends Specification {

  "Loading the configuration" should {

    "Parse correct configuration" in {
      val soc = OutputConfigurationFactory.getAgentCofiguration("metrics")(MetricsOutputConfiguration.apply).outputConfig
      soc.registryClass mustEqual "org.eigengo.monitor.output.metrics.DefaultRegistryProvider"
      soc.namingClass mustEqual "org.eigengo.monitor.output.metrics.DefaultNameMarshaller"
      soc.prefix mustEqual ""
      soc.refresh mustEqual 5
    }
  }

}
