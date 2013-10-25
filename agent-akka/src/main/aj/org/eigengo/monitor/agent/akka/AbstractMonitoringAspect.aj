package org.eigengo.monitor.agent.akka;

import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.agent.AgentConfigurationFactory;
import org.eigengo.monitor.output.CounterInterface;
import org.eigengo.monitor.output.NullCounterInterface;

abstract aspect AbstractMonitoringAspect {
    private static String agentName = "akka";
    protected static final CounterInterface counterInterface = createCounterInterface();

    private static CounterInterface createCounterInterface() throws ClassCastException{
        try {
            AgentConfiguration configuration = AgentConfigurationFactory.getAgentConfiguration(agentName);
            CounterInterface counterInterface = (CounterInterface)Class.forName(configuration.counterInterfaceClassName()).newInstance();
            return counterInterface;
        } catch (final ReflectiveOperationException e) {
            return new NullCounterInterface();
        }
    }
}
