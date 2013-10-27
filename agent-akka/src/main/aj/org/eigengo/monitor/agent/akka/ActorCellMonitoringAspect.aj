package org.eigengo.monitor.agent.akka;

import akka.actor.ActorCell;
import akka.actor.ActorPath;
import akka.actor.UnhandledMessage;

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
    private String[] getTags(final ActorPath actorPath) {
        List<String> tags = new ArrayList<String>();

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

    Object around(ActorCell actorCell, Object msg) : Pointcuts.receiveMessage(actorCell, msg) {
        if (!includeActorCell(actorCell)) return proceed(actorCell, msg);

        // we tag by actor name
        final String[] tags = getTags(actorCell.self().path());

        // record the queue size
        counterInterface.recordGaugeValue("akka.queue.size", actorCell.numberOfMessages(), tags);
        // record the message, general and specific
        counterInterface.incrementCounter("akka.actor.delivered", tags);
        counterInterface.incrementCounter("akka.actor.delivered." + msg.getClass().getSimpleName(), tags);

        // measure the time. we're using the ``nanoTime`` call to access the high-precision timer.
        // since we're not really interested in wall time, but just some increasing measure of
        // elapsed time
        Object result = null;
        final long start = System.nanoTime();
        // result will always be ``null``, because target returns ``Unit``
        result = proceed(actorCell, msg);
        // ns or µs is too fine. ms will do for now.
        final long duration = (System.nanoTime() - start) / 1000000;

        // record the actor duration
        counterInterface.recordExecutionTime("akka.actor.duration", (int)duration, tags);

        // return null would do the trick, but we want to be _proper_.
        return result;
    }

    before(ActorCell actorCell, Throwable failure) : Pointcuts.handleInvokeFailure(actorCell, failure) {
        // record the error, general and specific
        String[] tags = getTags(actorCell.self().path());
        counterInterface.incrementCounter("akka.actor.error", tags);
        counterInterface.incrementCounter(String.format("akka.actor.error.%s", failure.getMessage()), tags);
    }

    before(Object event) : Pointcuts.eventStreamPublish(event) {
        if (event instanceof UnhandledMessage) {
            UnhandledMessage unhandledMessage = (UnhandledMessage)event;
            String[] tags = getTags(unhandledMessage.recipient().path());
            counterInterface.incrementCounter("akka.actor.undelivered", tags);
            counterInterface.incrementCounter("akka.actor.undelivered." + unhandledMessage.getMessage().getClass().getSimpleName(), tags);
        }
    }

}