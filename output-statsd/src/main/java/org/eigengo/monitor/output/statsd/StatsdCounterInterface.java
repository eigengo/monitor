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

import org.apache.commons.lang3.StringUtils;
import org.eigengo.monitor.output.CounterInterface;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Submits the counters to the local statsd interface
 */
public class StatsdCounterInterface implements CounterInterface {
    private static final StatsDClient statsd = new NonBlockingStatsDClient("", "localhost", 8125, new String[]{"tag:value"});
    private static final ConcurrentHashMap<String, Metric> GAUGE_VALUES = new ConcurrentHashMap<>();

    public StatsdCounterInterface() {
        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        //Send an update to DataDog every 5 seconds
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (Metric metric : GAUGE_VALUES.values()) {
                    //System.out.println("metric = " + metric);
                    statsd.recordGaugeValue(metric.aspect, metric.value, metric.tags);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
        //Shutdown scheduler
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                scheduledExecutorService.shutdown();
            }
        });
    }

    @Override
    public void incrementCounter(String aspect, String... tags) {
        statsd.incrementCounter(aspect, tags);
    }

    @Override
    public void incrementCounter(String aspect, int delta, String... tags) {
        statsd.count(aspect, delta, tags);
    }

    @Override
    public void decrementCounter(String aspect, String... tags) {
        statsd.decrementCounter(aspect, tags);
    }

    @Override
    public void recordGaugeValue(String aspect, int value, String... tags) {
        statsd.recordGaugeValue(aspect, value, tags);
        GAUGE_VALUES.put(aspect + joinTags(tags), new Metric(aspect, value, tags));
    }

    @Override
    public void recordExecutionTime(String aspect, int duration, String... tags) {
        statsd.recordExecutionTime(aspect, duration, tags);
    }

    private static String joinTags(String[] tags) {
        return StringUtils.join(tags, ':');
    }

    private static class Metric {
        private String aspect;
        private int value;
        private String[] tags;

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
