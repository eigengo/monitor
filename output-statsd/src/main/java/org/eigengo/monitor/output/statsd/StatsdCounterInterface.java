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
    public void incrementCounter(String name) {
        statsd.incrementCounter(name);
    }

    @Override
    public void decrementCounter(String s) {
        statsd.decrementCounter(s);
    }



}
