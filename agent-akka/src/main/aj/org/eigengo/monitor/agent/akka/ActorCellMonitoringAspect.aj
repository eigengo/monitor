package org.eigengo.monitor.agent.akka;

import akka.actor.ActorCell;
import akka.actor.ActorPath;

import java.util.ArrayList;
import java.util.List;

public aspect ActorCellMonitoringAspect extends AbstractMonitoringAspect {

    // decide whether to include this ActorCell in our measurements
    private boolean includeActorCell(ActorCell actorCell) {
        if (this.includeSystemActors) return true;

        String userOrSystem = actorCell.self().path().getElements().iterator().next();
        return "user".equals(userOrSystem);
    }

    // get the tags for the cell
    private String[] getTags(ActorCell actorCell) {
        List<String> tags = new ArrayList<String>();

        final ActorPath actorPath = actorCell.self().path();

        // TODO: Improve detection of routed actors. This relies only on the naming :(.
        String lastPathElement = actorPath.elements().last();
        if (lastPathElement.startsWith("$")) {
            // this is routed actor.
            tags.add(actorPath.parent().toString());
            if (this.includeRoutees) tags.add(actorPath.toString());
        } else {
            // there is no supervisor
            tags.add(actorPath.toString());
        }

        return tags.toArray(new String[tags.size()]);
    }

    Object around(ActorCell actorCell, Object msg) throws Throwable : org.eigengo.monitor.agent.akka.Pointcuts.receiveMessage(actorCell, msg) {
        if (!includeActorCell(actorCell)) return proceed(actorCell, msg);

        // we tag by actor name
        final String[] tags = getTags(actorCell);

        // record the queue size
        counterInterface.recordGaugeValue("akka.queue.size", actorCell.numberOfMessages(), tags);
        // record the message, general and specific
        counterInterface.incrementCounter("akka.message", tags);
        counterInterface.incrementCounter("akka.message." + msg.getClass().getSimpleName(), tags);

        // measure the time. we're using the ``nanoTime`` call to access the high-precision timer.
        // since we're not really interested in wall time, but just some increasing measure of
        // elapsed time
        Object result = null;
        // non-null error means that the target has hard-failed
        Throwable error = null;
        final long start = System.nanoTime();
        try {
            // result will always be ``null``, because target returns ``Unit``
            result = proceed(actorCell, msg);
        } catch (final Throwable t) {
            // record the error, general and specific
            counterInterface.incrementCounter("akka.actor.error", tags);
            counterInterface.incrementCounter(String.format("akka.actor.error.%s", t.getMessage()), tags);
            error = t;
        }
        // ns or µs is too fine. ms will do for now.
        final long duration = (System.nanoTime() - start) / 1000000;

        // record the actor duration
        counterInterface.recordExecutionTime("akka.actor.duration", (int)duration, tags);

        if (error != null) throw error;

        // return null would do the trick, but we want to be _proper_.
        return result;
    }

}