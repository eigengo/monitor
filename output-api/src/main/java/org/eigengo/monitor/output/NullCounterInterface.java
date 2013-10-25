package org.eigengo.monitor.output;

/**
 * Implements a no-op version of the {@link CounterInterface}.
 */
public class NullCounterInterface implements CounterInterface {

    @Override
    public void incrementCounter(String name) {
        // noop
    }

    @Override
    public void decrementCounter(String name) {
        // noop
    }
}
