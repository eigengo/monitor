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

import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.output.CounterInterface;
import scala.concurrent.forkjoin.ForkJoinPool;

import java.util.concurrent.ExecutorService;

/**
 * Monitors the performance of the ``ForkJoinPool``. This will need much more tidying up, but it
 * outlines the strategy.
 *
 * The aspect contains advices that will--in the future--monitor the performance of all types of
 * thread pools, giving the users the insight into the threading in their actor systems.
 */
public aspect DispatcherMonitoringAspect extends AbstractMonitoringAspect issingleton() {
    private final CounterInterface counterInterface;

    /**
     * Constructs this aspects, loads its configuration and instantiates the {@code counterInterface}.
     */
    public DispatcherMonitoringAspect() {
        AgentConfiguration<AkkaAgentConfiguration> configuration = getAgentConfiguration("akka", AkkaAgentConfigurationJapi.apply());
        this.counterInterface = createCounterInterface(configuration.common());
    }

    /**
     * Advises the methods of the {@link scala.concurrent.forkjoin.ForkJoinPool} to report on its
     * performance
     */
    before(ForkJoinPool es) : call(* java.util.concurrent.ExecutorService+.*(..)) && target(es) {
        this.counterInterface.recordGaugeValue(Aspects.activeThreadCount(), es.getActiveThreadCount(), getTags(es));
        this.counterInterface.recordGaugeValue(Aspects.runningThreadCount(), es.getRunningThreadCount(), getTags(es));

        this.counterInterface.recordGaugeValue(Aspects.poolSize(), es.getPoolSize(), getTags(es));
        this.counterInterface.recordGaugeValue(Aspects.queuedTaskCount(), (int) es.getQueuedTaskCount(), getTags(es));
    }

    /**
     * Computes the tags for the given the {@code es}.
     *
     * @param es the ExecutorService to get the tags for
     * @return the array of tags
     */
    private String[] getTags(ExecutorService es) {
        return new String[0];
    }

}
