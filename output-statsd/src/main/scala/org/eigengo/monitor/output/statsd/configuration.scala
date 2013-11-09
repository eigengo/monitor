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

import com.typesafe.config.Config

/**
 * Holds the configuration for the Statsd output.
 *
 * @param remoteAddress the address of the statsd agent
 * @param remotePort the port of the statsd agent
 * @param refresh how often (in seconds) should the agent report the number of actors
 * @param prefix the prefix to apply to all values sent to statsd
 * @param constantTags the constant tags (in form of ``[tag:value]``) to apply to all values sent to statsd
 */
case class StatsdOutputConfiguration(remoteAddress: String, remotePort: Int,
                                      refresh: Int,
                                      prefix: String, constantTags: Array[String])

/**
 * Companion object that makes instances of ``StatsdOutputConfiguration`` from the
 * instances of ``Config``.
 */
object StatsdOutputConfiguration {

  def apply(config: Config): StatsdOutputConfiguration = {
    val prefix = config.getString("prefix")
    val remoteAddress = config.getString("remoteAddress")
    val remotePort = config.getInt("remotePort")
    val refresh = config.getInt("refresh")
    val tagList = config.getStringList("constantTags")
    val constantTags: Array[String] = tagList.toArray(Array.ofDim(tagList.size()))

    StatsdOutputConfiguration(remoteAddress, remotePort, refresh, prefix, constantTags)
  }

}

/**
 * Exposes the ``StatsdOutputConfiguration.apply(Config)`` function in Java-friendly API
 */
object StatsdOutputConfigurationJapi {

  def apply: Config => StatsdOutputConfiguration = StatsdOutputConfiguration.apply

}