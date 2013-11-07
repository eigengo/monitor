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
package org.eigengo.monitor.agent

/**
 * A case class for packaging configuration settings common across agents.
 *
 * @param counterInterfaceClassName the name of the class that implements the ``CounterInterface``
 */
case class CommonAgentConfiguration(counterInterfaceClassName: String)

/**
 * A case class for packaging agent-specific configuration settings together with the common agent settings.
 *
 * @param common the cross-agent configuration
 * @param agent the agent-specific configuration
 *
 */
case class AgentConfiguration[A](common: CommonAgentConfiguration, agent: A)
