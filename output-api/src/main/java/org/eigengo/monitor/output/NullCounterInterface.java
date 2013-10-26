package org.eigengo.monitor.output;

/**
 * Implements a no-op version of the {@link CounterInterface}.
 */
public class NullCounterInterface implements CounterInterface {

    @Override
    public void incrementCounter(String aspect, String... tags) {
        // noop
    }

    @Override
    public void decrementCounter(String aspect, String... tags) {
        // noop
    }

    @Override
    public void recordGaugeValue(String aspect, int value, String... tags) {
        // noop
    }
}
