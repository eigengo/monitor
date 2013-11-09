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
package org.eigengo.monitor.output.statsd;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.eigengo.monitor.output.CounterInterface;
import org.eigengo.monitor.output.OutputConfigurationFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Submits the counters to the local statsd interface
 */
public class StatsdCounterInterface implements CounterInterface {
    private final StatsDClient statsd;
    private final Map<String, Metric> gaugeValues;

    public StatsdCounterInterface() {
        StatsdOutputConfiguration configuration =
                OutputConfigurationFactory.getAgentCofiguration("statsd", StatsdOutputConfigurationJapi.apply()).outputConfig();
        this.statsd = new NonBlockingStatsDClient(configuration.prefix(),
                configuration.remoteAddress(), configuration.remotePort(), configuration.constantTags());
        this.gaugeValues = new ConcurrentHashMap<>();

        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private final ThreadFactory delegate = Executors.defaultThreadFactory();

            /**
             * Set thread as daemon and with minimal priority using a delegate ThreadFactory
             * @param r Runnable
             * @return daemon thread
             */
            @Override
            public Thread newThread(Runnable r) {
                final Thread thread = delegate.newThread(r);
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            }
        });
        //Send an update to DataDog every X seconds
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (Metric metric : gaugeValues.values()) {
                    statsd.recordGaugeValue(metric.aspect, metric.value, metric.tags);
                }
            }
        }, configuration.refresh(), configuration.refresh(), TimeUnit.SECONDS);
        //Shutdown scheduler
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                scheduledExecutorService.shutdown();
            }
        });
    }

    @Override
    public void incrementCounter(String aspect, String... tags) {
        this.statsd.incrementCounter(aspect, tags);
    }

    @Override
    public void incrementCounter(String aspect, int delta, String... tags) {
        this.statsd.count(aspect, delta, tags);
    }

    @Override
    public void decrementCounter(String aspect, String... tags) {
        this.statsd.decrementCounter(aspect, tags);
    }

    @Override
    public void recordGaugeValue(String aspect, int value, String... tags) {
        this.statsd.recordGaugeValue(aspect, value, tags);
        this.gaugeValues.put(aspect + joinTags(tags), new Metric(aspect, value, tags));
    }

    @Override
    public void recordExecutionTime(String aspect, int duration, String... tags) {
        this.statsd.recordExecutionTime(aspect, duration, tags);
    }

    private static String joinTags(String[] tags) {
        StringBuilder builder = new StringBuilder(256);
        for (String tag : tags) {
            builder.append(tag).append('|');
        }
        return builder.toString();
    }

    private final static class Metric {
        private final String aspect;
        private final int value;
        private final String[] tags;

        private Metric(String aspect, int value, String[] tags) {
            this.aspect = aspect;
            this.value = value;
            this.tags = tags;
        }

        @Override
        public String toString() {
            return "Metric{aspect='" + aspect + '\'' + ", value=" + value + ", tags=" + Arrays.toString(tags) + '}';
        }
    }

}
