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
package org.eigengo.monitor.agent.akka;

import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.agent.AgentConfigurationFactory;
import org.eigengo.monitor.output.CounterInterface;
import org.eigengo.monitor.output.NullCounterInterface;

abstract aspect AbstractMonitoringAspect {
    // if true, the monitoring will include the /system actors
    protected boolean includeSystemActors = false;
    // if true, the monitoring will include the child actors created as routees
    protected boolean includeRoutees = getAgentConfiguration().agent().includeRoutees();
    // if true, the monitoring will include the actor class name as one of the tags
    protected boolean includeActorClassName = true;

    protected static final CounterInterface counterInterface = createCounterInterface();

    private static CounterInterface createCounterInterface() {
        try {
            AgentConfiguration configuration = getAgentConfiguration();
            CounterInterface counterInterface = (CounterInterface)Class.forName(configuration.common().counterInterfaceClassName()).newInstance();
            return counterInterface;
        } catch (final ReflectiveOperationException e) {
            return new NullCounterInterface();
        } catch (final ClassCastException e) {
            return new NullCounterInterface();
        }
    }

    private static AgentConfiguration<AkkaAgentConfiguration> getAgentConfiguration() {
        return AgentConfigurationFactory.getAgentCofiguration("akka", AkkaAgentConfigurationJapi.apply());
    }
}
