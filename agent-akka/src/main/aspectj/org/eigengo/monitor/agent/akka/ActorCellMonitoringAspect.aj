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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains advices for monitoring behaviour of an actor; typically imprisoned in an {@code ActorCell}.
 */
public aspect ActorCellMonitoringAspect extends AbstractMonitoringAspect issingleton() {
    private AkkaAgentConfiguration agentConfiguration;
    private final CounterInterface counterInterface;
    private final Option<String> noActorClazz = Option.empty();
    private final ConcurrentHashMap<Option<String>, AtomicLong> samplingCounters;
    private final ConcurrentHashMap<String, AtomicLong> numberOfActors;


    /**
     * Constructs this aspect
     */
    public ActorCellMonitoringAspect() {
        AgentConfiguration<AkkaAgentConfiguration> configuration = getAgentConfiguration("akka", AkkaAgentConfigurationJapi.apply());
        this.agentConfiguration = configuration.agent();
        this.counterInterface = createCounterInterface(configuration.common());
        this.samplingCounters = new ConcurrentHashMap<Option<String>, AtomicLong>();
        this.numberOfActors = new ConcurrentHashMap<String, AtomicLong>();
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
    private boolean includeActorPath(final PathAndClass pathAndClass) {
        if (!this.agentConfiguration.incuded().accept(pathAndClass)) return false;
        if (this.agentConfiguration.excluded().accept(pathAndClass)) return false;

        if (this.agentConfiguration.includeSystemAgents()) return true;

        String userOrSystem = pathAndClass.actorPath().getElements().iterator().next();
        return "user".equals(userOrSystem);
    }

    // get the sample rate for an actor
    private int getSampleRate(final PathAndClass pathAndClass) {
        return this.agentConfiguration.sampling().getRate(pathAndClass);
    }

    // decide whether to sample an actor on a particular occasion
    private final boolean sampleMessage(final PathAndClass pathAndClass) {
        int sampleRate = getSampleRate(pathAndClass);
        if (sampleRate == 1) return true;

        this.samplingCounters.putIfAbsent(pathAndClass.actorClassName(), new AtomicLong(0));
        long timesSeenSoFar = this.samplingCounters.get(pathAndClass.actorClassName()).incrementAndGet();
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
     * We proceed with the pointcut if the actor is to be included in the monitoring *and* this is
     * the 'multiple-of-n'th time we've seen a message for an actor with a sample rate of n.
     *
     * Currently, we sample queue size, the fact that the message is delivered, the simple name of the class of the
     * message, and the time taken to complete the actor's reactive action.
     *
     * @param actorCell the ActorCell where the actor that receives the message "lives"
     * @param msg the incoming message
     */
    Object around(ActorCell actorCell, Object msg) : Pointcuts.actorCellReceiveMessage(actorCell, msg) {
        final ActorPath actorPath = actorCell.self().path();
        final PathAndClass pathAndClass = new PathAndClass(actorPath, Option.apply(actorCell.actor().getClass().getCanonicalName()));
        if (!includeActorPath(pathAndClass) || !sampleMessage(pathAndClass)) return proceed(actorCell, msg);

        int samplingRate = getSampleRate(pathAndClass);

        // we tag by actor name
        final String[] tags = getTags(actorPath, actorCell.actor());

        // record the queue size
        this.counterInterface.recordGaugeValue("akka.queue.size", actorCell.numberOfMessages(), tags);
        // record the message, general and specific
        this.counterInterface.incrementCounter("akka.actor.delivered", samplingRate, tags);
        this.counterInterface.incrementCounter("akka.actor.delivered." + msg.getClass().getSimpleName(), samplingRate, tags);

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

//    after() returning (ActorRef actor): execution(* akka.actor.ActorSystem.actorOf(..)) {
//        if (!includeActorPath(actor.path())) return;
//
//        final String tag = actor.path().root().toString();
//        counterInterface.incrementCounter("akka.actor.count", tag);
//    }
//
//    after() returning (ActorRef actor): execution(* akka.actor.ActorCell.actorOf(..)) {
//        if (!includeActorPath(actor.path())) return;
//
//        final String tag = actor.path().root().toString();
//        counterInterface.incrementCounter("akka.actor.count", tag);
//    }
//
//    after(ActorRef actor) : execution(* akka.actor.ActorCell.stop(..)) && args(actor) {
//        if (!includeActorPath(actor.path())) return;
//
//        final String tag = actor.path().root().toString();
//        counterInterface.decrementCounter("akka.actor.count", tag);
//    }
    /**
     * Advises the {@code actorOf} method of {@code ActorCell} and {@code ActorSystem}
     *
     * @param props the {@code Props} instance used in the call
     * @param actor the {@code ActorRef} returned from the call
     */
    after(Props props) returning (ActorRef actor): Pointcuts.anyActorOf(props) {
        if (!includeActorPath(new PathAndClass(actor.path(), this.noActorClazz))) return;
        final String className = props.actorClass().getCanonicalName();

        this.numberOfActors.putIfAbsent(className, new AtomicLong(0));
        // increment and get the current number of actors of this type (if the value was 0, then this returns 1 -- which is correct)
        final long value = this.numberOfActors.get(className).incrementAndGet();

        // record the current number of actors of this type
        this.counterInterface.recordGaugeValue("akka.actor.new.count", (int)value, className);
    }

    /**
     * Advises the {@code LocalActorRef.stop} method
     *
     * @param actorRef the {@code LocalActorRef} of the actor being stopped
     */
    after(LocalActorRef actorRef) : Pointcuts.localActorRefStop(actorRef) {
        if (!includeActorPath(new PathAndClass(actorRef.path(), this.noActorClazz))) return;
        final String className = actorRef.underlying().actor().getClass().getCanonicalName();

        this.numberOfActors.putIfAbsent(className, new AtomicLong(0));
        final long value = this.numberOfActors.get(className).decrementAndGet();

        this.counterInterface.recordGaugeValue("akka.actor.count", (int)value, className);
    }

}