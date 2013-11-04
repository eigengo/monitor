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

import akka.actor.*;
import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.output.CounterInterface;
import scala.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Contains advices for monitoring behaviour of an actor; typically imprisoned in an {@code ActorCell}.
 */
public aspect ActorCellMonitoringAspect extends AbstractMonitoringAspect issingleton() {
    private AkkaAgentConfiguration agentConfiguration;
    private final CounterInterface counterInterface;
    private final Option<String> noActorClazz = Option.empty();
    private final HashMap<ActorFilter, AtomicLong> concurrentCounters;

    /**
     * Constructs this aspect
     */
    public ActorCellMonitoringAspect() {
        AgentConfiguration<AkkaAgentConfiguration> configuration = getAgentConfiguration("akka", AkkaAgentConfigurationJapi.apply());
        this.agentConfiguration = configuration.agent();
        this.counterInterface = createCounterInterface(configuration.common());
    // we initialise all the keys we'll be using for our sampling here, so that we can access the HashMap once per query
        this.concurrentCounters = getCounterKeys(configuration);
    }

    /**
     * Injects the new {@code AkkaAgentConfiguration} instance.
     *
     * @param agentConfiguration the new configuration
     */
    synchronized final void setAgentConfiguration(AkkaAgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
    }

    // decide whether to include this ActorCell in our measurements
    private boolean includeActorPath(final ActorPath actorPath, final Option<String> actorClassName) {
        if (!this.agentConfiguration.incuded().accept(actorPath, actorClassName)) return false;
        if (this.agentConfiguration.excluded().accept(actorPath, actorClassName)) return false;

        if (this.agentConfiguration.includeSystemAgents()) return true;

        String userOrSystem = actorPath.getElements().iterator().next();
        return "user".equals(userOrSystem);
    }

    private int getSampleRate(final ActorPath actorPath) {
        return agentConfiguration.sampling().getRate(actorPath);
    }

    private Option<ActorFilter> getFilterToSampleOver(final ActorPath actorPath) {
        return agentConfiguration.sampling().getFilterFor(actorPath);
    }

    private final boolean sampleMessage(final ActorPath actorPath) {
        int sampleRate = getSampleRate(actorPath);
        if (sampleRate == 1) {return true;}

        // We've already essentially checked that the actorFilter is not None (with `sampleRate == 1`) so this is legit
        ActorFilter actorFilter = getFilterToSampleOver(actorPath).get();
        // And we initialised the HashMap element with AtomicLong(0) in the constructor
        long timesSeenSoFar = concurrentCounters.get(actorFilter).incrementAndGet();
        return (timesSeenSoFar % sampleRate == 1); // == 1 to log first value (incrementAndGet returns updated value)
    }

    // get the tags for the cell
    private String[] getTags(final ActorPath actorPath, final Actor actor) {
        List<String> tags = new ArrayList<String>();

        // TODO: Improve detection of routed actors. This relies only on the naming :(.
        String lastPathElement = actorPath.elements().last();
        if (lastPathElement.startsWith("$")) {
            // this is routed actor.
            tags.add(actorPath.parent().toString());
            if (this.agentConfiguration.includeRoutees()) tags.add(actorPath.toString());
        } else {
            // there is no supervisor
            tags.add(actorPath.toString());
        }
        if (actor != null) {
            tags.add(String.format("akka:%s.%s", actor.context().system().name(), actor.getClass().getCanonicalName()));
        }

        return tags.toArray(new String[tags.size()]);
    }

    /**
     * Advises the {@code ActorCell.receiveMessage(message: Object): Unit}
     *
     * @param actorCell the ActorCell where the actor that receives the message "lives"
     * @param msg the incoming message
     */
    Object around(ActorCell actorCell, Object msg) : Pointcuts.actorCellReceiveMessage(actorCell, msg) {
        final ActorPath actorPath = actorCell.self().path();
        if (!includeActorPath(actorPath, Option.apply(actorCell.actor().getClass().getCanonicalName())) ||
                !sampleMessage(actorPath)) return proceed(actorCell, msg);

        // we tag by actor name
        final String[] tags = getTags(actorPath, actorCell.actor());

        // record the queue size
        this.counterInterface.recordGaugeValue("akka.queue.size", actorCell.numberOfMessages(), tags);
        // record the message, general and specific
        this.counterInterface.incrementCounter("akka.actor.delivered", tags);
        this.counterInterface.incrementCounter("akka.actor.delivered." + msg.getClass().getSimpleName(), tags);

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
        this.counterInterface.recordExecutionTime("akka.actor.duration", (int)duration, tags);

        // return null would do the trick, but we want to be _proper_.
        return result;
    }

    /**
     * Advises the {@code ActorCell.handleInvokeFailure(_, failure: Throwable): Unit}
     *
     * @param actorCell the ActorCell where the error spreads from
     * @param failure the exception that escaped from the {@code receive} method
     */
    before(ActorCell actorCell, Throwable failure) : Pointcuts.actorCellHandleInvokeFailure(actorCell, failure) {
        // record the error, general and specific
        String[] tags = getTags(actorCell.self().path(), actorCell.actor());
        this.counterInterface.incrementCounter("akka.actor.error", tags);
        this.counterInterface.incrementCounter(String.format("akka.actor.error.%s", failure.getMessage()), tags);
    }

    /**
     * Advises the {@code EventStream.publish(event: Any): Unit}
     *
     * @param event the received event
     */
    before(Object event) : Pointcuts.eventStreamPublish(event) {
        if (event instanceof UnhandledMessage) {
            UnhandledMessage unhandledMessage = (UnhandledMessage)event;
            String[] tags = getTags(unhandledMessage.recipient().path(), null);
            this.counterInterface.incrementCounter("akka.actor.undelivered", tags);
            this.counterInterface.incrementCounter("akka.actor.undelivered." + unhandledMessage.getMessage().getClass().getSimpleName(), tags);
        }
    }

    /**
     * Advises the {@code actorOf} method of {@code ActorCell} and {@code ActorSystem}
     *
     * @param actor the {@code ActorRef} returned from the call
     */
    after() returning (ActorRef actor): Pointcuts.anyActorOf() {
        if (!includeActorPath(actor.path(), this.noActorClazz)) return;

        final String tag = actor.path().root().toString();
        this.counterInterface.incrementCounter("akka.actor.count", tag);
    }

    /**
     * Advises the {@code ActorCell.stop} method
     *
     * @param actor the actor being stopped
     */
    after(ActorRef actor) : Pointcuts.actorCellStop(actor) {
        if (!includeActorPath(actor.path(), this.noActorClazz)) return;

        final String tag = actor.path().root().toString();
        this.counterInterface.decrementCounter("akka.actor.count", tag);
    }

}