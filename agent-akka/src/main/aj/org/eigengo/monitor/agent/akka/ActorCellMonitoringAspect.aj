package org.eigengo.monitor.agent.akka;

import akka.actor.ActorCell;

public aspect ActorCellMonitoringAspect extends AbstractMonitoringAspect {

    before(ActorCell actorCell, Object msg): org.eigengo.monitor.agent.akka.Pointcuts.receiveMessage(actorCell, msg) {
        // we tag by actor name
        String[] tags = new String[] {
                actorCell.self().path().toString()
        };

        // record the queue size
        counterInterface.recordGaugeValue("queue.size", actorCell.numberOfMessages(), tags);

        // record the message
        counterInterface.incrementCounter("message." + msg.getClass().getSimpleName(), tags);
    }

}