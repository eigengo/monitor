package org.eigengo.monitor.agent.akka;

import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.agent.AgentConfigurationFactory;
import org.eigengo.monitor.output.CounterInterface;
import org.eigengo.monitor.output.NullCounterInterface;

abstract aspect AbstractMonitoringAspect {
    // if true, the monitoring will include the /system actors
    protected boolean includeSystemActors = false;
    // if true, the monitoring will include the child actors created as routees
    protected boolean includeRoutees = getAgentConfiguration().config().getBoolean("includeRoutees");

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

    private static AgentConfiguration getAgentConfiguration() {
        return AgentConfigurationFactory.getAgentCofiguration("akka");
    }
}
