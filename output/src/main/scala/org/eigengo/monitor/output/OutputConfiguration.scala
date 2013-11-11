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
package org.eigengo.monitor.output

import com.typesafe.config.Config

/**
 * Holds the configuration of the output modules, together with the "root" configuration that
 * points to the top-level node of the configuration file that was loaded
 *
 * @param rootConfig the root ``Config`` node
 * @param outputConfig the specific configuration loaded from [a portion of] the ``root``
 * @tparam A the specific configuration
 */
case class OutputConfiguration[A](rootConfig: Config, outputConfig: A)
