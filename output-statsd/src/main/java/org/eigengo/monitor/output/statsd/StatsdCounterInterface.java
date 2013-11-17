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

    /**
     * Constructs this instance by loading the {@code output.conf} configuration, and then looking
     * for the {@code org.eigengo.monitor.output.statsd} key.
     */
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
        this.statsd.incrementCounter(aspect, sanitize(tags));
    }

    @Override
    public void incrementCounter(String aspect, int delta, String... tags) {
        this.statsd.count(aspect, delta, sanitize(tags));
    }

    @Override
    public void decrementCounter(String aspect, String... tags) {
        this.statsd.decrementCounter(aspect, sanitize(tags));
    }

    @Override
    public void recordGaugeValue(String aspect, int value, String... tags) {
        final String[] sanitized = sanitize(tags);
        this.statsd.recordGaugeValue(aspect, value, sanitized);
        this.gaugeValues.put(aspect + Arrays.toString(sanitized), new Metric(aspect, value, sanitized));
    }

    @Override
    public void recordExecutionTime(String aspect, int duration, String... tags) {
        this.statsd.recordExecutionTime(aspect, duration, tags);
    }

    /**
     * Removes the non-statsd characters that made their way into the tags
     *
     * @param tags the raw tags.
     * @return the sanitized tags
     */
    private static String[] sanitize(String[] tags) {
        String[] sanitized = new String[tags.length];
        for (int i = 0; i < tags.length; i++) {
            sanitized[i] = tags[i].replace(' ', '_').replace(',', '_');
        }
        return sanitized;
    }

    /**
     * Simple gauge container holding the {@code aspect}, {@code value} and {@code tags}.
     */
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
