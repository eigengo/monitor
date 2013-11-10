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
import org.eigengo.monitor.output.OutputConfigurationFactory

class StatsdOutputConfigurationSpec extends Specification {

  "Loading the configuration" should {

    "Parse correct configuration" in {
      val soc = OutputConfigurationFactory.getAgentCofiguration("statsd")(StatsdOutputConfiguration.apply).outputConfig

      soc.prefix mustEqual ""
      soc.remoteAddress mustEqual "localhost"
      soc.remotePort mustEqual 12345
      soc.refresh mustEqual 5
      soc.constantTags.toList must containAllOf(List("t1:v1", "t2:v2"))
    }
  }

}
