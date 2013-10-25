package org.eigengo.monitor.agent.akka;

import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.agent.AgentConfigurationFactory;
import org.eigengo.monitor.output.CounterInterface;
import org.eigengo.monitor.output.NullCounterInterface;

privileged aspect MessageMonitoringAspect {
    private static String agentName = "akka";
    private static final CounterInterface counterInterface = createCounterInterface();

    private static CounterInterface createCounterInterface() throws ClassCastException{
        try {
            AgentConfiguration configuration = AgentConfigurationFactory.getAgentConfiguration(agentName);
            CounterInterface counterInterface = (CounterInterface)Class.forName(configuration.counterInterfaceClassName()).newInstance();
            return counterInterface;
        } catch (final ReflectiveOperationException e) {
            return new NullCounterInterface();
        }
    }

    pointcut receiveMessage(akka.actor.ActorCell actorCell, Object msg) : target(actorCell) &&
        call(* akka.actor.ActorCell.receiveMessage(..)) && args(msg);

    before(akka.actor.ActorCell actorCell, Object msg): receiveMessage(actorCell, msg) {
        counterInterface.incrementCounter("message." + msg.getClass().getSimpleName());
    }

}