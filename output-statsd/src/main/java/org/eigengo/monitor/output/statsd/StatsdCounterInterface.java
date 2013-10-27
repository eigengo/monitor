package org.eigengo.monitor.output.statsd;

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
