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

import com.typesafe.config.Config

/**
 * Holds the configuration for the Codahale Metrics output.
 *
 * @param registryClass the class to use as the registry provider
 * @param namingClass the class to use to generate naming of statistics
 * @param refresh how often (in seconds) should the agent report the number of actors
 * @param prefix the prefix to apply to all values sent to metrics
 */
case class MetricsOutputConfiguration(registryClass: String,
                                      namingClass: String,
                                      refresh: Int,
                                      prefix: String) {

}

/**
 * Companion object that makes instances of ``MetricsOutputConfiguration`` from the
 * instances of ``Config``.
 */
object MetricsOutputConfiguration {

  def apply(config: Config): MetricsOutputConfiguration = {

    val registryClass = config.getString("registry-class")
    val namingClass = config.getString("naming-class")
    val prefix = config.getString("prefix")
    val refresh = config.getInt("refresh")

    require(prefix.isEmpty || prefix.endsWith("."), "Prefix must be empty or end with '.'")

    MetricsOutputConfiguration(registryClass, namingClass, refresh, prefix)
  }

}

/**
 * Exposes the ``MetricsOutputConfiguration.apply(Config)`` function in Java-friendly API
 */
object MetricsOutputConfigurationJapi {

  def apply: Config => MetricsOutputConfiguration = MetricsOutputConfiguration.apply

}