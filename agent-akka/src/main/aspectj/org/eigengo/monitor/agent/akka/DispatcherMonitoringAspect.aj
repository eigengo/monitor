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

import akka.actor.ActorCell;
import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.output.CounterInterface;
import scala.concurrent.forkjoin.ForkJoinPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitors the performance of the ``ForkJoinPool``. This will need much more tidying up, but it
 * outlines the strategy.
 *
 * The aspect contains advices that will--in the future--monitor the performance of all types of
 * thread pools, giving the users the insight into the threading in their actor systems.
 */
public aspect DispatcherMonitoringAspect extends AbstractMonitoringAspect {
    private final CounterInterface counterInterface;
    private final ActorPathTagger tagger;

    // this is really icky, but it replaces the failing cflowbelow pointcut
    private final Map<Long, String[]> actorCellCflowTags = new ConcurrentHashMap<Long, String[]>();

    /**
     * Constructs this aspects, loads its configuration and instantiates the {@code counterInterface}.
     */
    public DispatcherMonitoringAspect() {
        AgentConfiguration<AkkaAgentConfiguration> configuration = getAgentConfiguration("akka", AkkaAgentConfigurationJapi.apply());
        this.counterInterface = createCounterInterface(configuration.common());
        this.tagger = new ActorPathTagger(configuration.agent().includeRoutees());
    }

    pointcut messageDispatcherDispatch(ActorCell actorCell) : execution(* akka.dispatch.MessageDispatcher+.dispatch(..)) && args(actorCell, *);

    before(ActorCell actorCell) : messageDispatcherDispatch(actorCell) {
        final String[] tags = this.tagger.getTags(actorCell.self().path(), ActorPathTagger.ANONYMOUS_ACTOR_CLASS_NAME);
        final String[] allTags = new String[tags.length + 1];
        System.arraycopy(tags, 0, allTags, 1, tags.length);
        allTags[0] = String.format("akka.dispatcher:%s", actorCell.dispatcher().id());
        this.actorCellCflowTags.put(Thread.currentThread().getId(), allTags);
    }

    /**
     * Advises the ``execute`` method of an ``ExecutorService`` if that executor service is the
     * ``ForkJoinPool``.
     *
     * Ideally, we would like to use <code>cflowbelow</code> pointcut expression, but that fails
     * in the LTW stage with <code>NoSuchFieldError</code>
     */
    before(ForkJoinPool es) : call(* java.util.concurrent.ExecutorService+.execute(..)) && target(es) {
        final String[] tags = this.actorCellCflowTags.get(Thread.currentThread().getId());
        if (tags == null) {
            // this is execute without ActorCell.dispatch. We don't care about that.
            return;
        }
        this.counterInterface.recordGaugeValue(Aspects.activeThreadCount(), es.getActiveThreadCount(), tags);
        this.counterInterface.recordGaugeValue(Aspects.runningThreadCount(), es.getRunningThreadCount(), tags);

        this.counterInterface.recordGaugeValue(Aspects.poolSize(), es.getPoolSize(), tags);
        this.counterInterface.recordGaugeValue(Aspects.queuedTaskCount(), (int) es.getQueuedTaskCount(), tags);
    }

}
