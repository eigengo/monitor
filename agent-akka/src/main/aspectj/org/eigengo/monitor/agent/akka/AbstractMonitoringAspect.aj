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

import com.typesafe.config.Config;
import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.agent.AgentConfigurationFactory;
import org.eigengo.monitor.agent.CommonAgentConfiguration;
import org.eigengo.monitor.output.CounterInterface;
import org.eigengo.monitor.output.NullCounterInterface;
import scala.Function1;

abstract aspect AbstractMonitoringAspect {

    protected final CounterInterface createCounterInterface(CommonAgentConfiguration configuration) {
        try {
            CounterInterface counterInterface = (CounterInterface)Class.forName(configuration.counterInterfaceClassName()).newInstance();
            return counterInterface;
        } catch (final ReflectiveOperationException e) {
            e.printStackTrace();
            return new NullCounterInterface();
        } catch (final ClassCastException e) {
            e.printStackTrace();
            return new NullCounterInterface();
        }
    }

    protected final <A> AgentConfiguration<A> getAgentConfiguration(String agentName, Function1<Config, A> agent) {
        return AgentConfigurationFactory.getAgentCofiguration(agentName, agent);
    }
}
