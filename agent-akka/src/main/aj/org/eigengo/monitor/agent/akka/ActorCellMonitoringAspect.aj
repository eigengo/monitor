package org.eigengo.monitor.agent.akka;

import akka.actor.ActorCell;

public aspect ActorCellMonitoringAspect extends AbstractMonitoringAspect {

    Object around(ActorCell actorCell, Object msg): org.eigengo.monitor.agent.akka.Pointcuts.receiveMessage(actorCell, msg) {
        // we tag by actor name
        String[] tags = new String[] {
                actorCell.self().path().toString()
        };

        // record the queue size
        counterInterface.recordGaugeValue("akka.queue.size", actorCell.numberOfMessages(), tags);

        // record the message
        counterInterface.incrementCounter("akka.message." + msg.getClass().getSimpleName(), tags);

        long start = System.nanoTime();
        Object result = proceed(actorCell, msg);
        long duration = (System.nanoTime() - start) / 1000000;     // ms

        // record the actor duration
        counterInterface.recordGaugeValue("akka.actor.duration", (int)duration, tags);

        return result;
    }

}