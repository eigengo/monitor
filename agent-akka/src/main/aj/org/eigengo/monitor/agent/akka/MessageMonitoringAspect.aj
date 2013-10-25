package org.eigengo.monitor.agent.akka;

privileged aspect MessageMonitoringAspect extends AbstractMonitoringAspect {

    pointcut receiveMessage(akka.actor.ActorCell actorCell, Object msg) : target(actorCell) &&
        call(* akka.actor.ActorCell.receiveMessage(..)) && args(msg);

    before(akka.actor.ActorCell actorCell, Object msg): receiveMessage(actorCell, msg) {
        counterInterface.incrementCounter("message." + msg.getClass().getSimpleName());
    }

}