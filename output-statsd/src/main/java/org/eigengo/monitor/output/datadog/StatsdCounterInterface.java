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
package org.eigengo.monitor.output.datadog;

import org.eigengo.monitor.output.CounterInterface;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

/**
 * Submits the counters to the local statsd interface
 */
public class StatsdCounterInterface implements CounterInterface {
    private static final StatsDClient statsd = new NonBlockingStatsDClient("", "localhost", 8125, new String[]{"tag:value"});

    @Override
    public void incrementCounter(String aspect, String... tags) {
        statsd.incrementCounter(aspect, tags);
    }

    @Override
    public void decrementCounter(String aspect, String... tags) {
        statsd.decrementCounter(aspect, tags);
    }

    @Override
    public void recordGaugeValue(String aspect, int value, String... tags) {
        statsd.recordGaugeValue(aspect, value, tags);
    }

    @Override
    public void recordExecutionTime(String aspect, int duration, String... tags) {
        statsd.recordExecutionTime(aspect, duration, tags);
    }


}
