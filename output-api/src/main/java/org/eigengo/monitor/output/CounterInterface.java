package org.eigengo.monitor.output;

/**
 * Implementations of this interface should communicate with the counter target to submit
 * the values. Ideally, the implementations will be non-blocking; if they are blocking,
 * they should complete as quickly as possible.
 */
public interface CounterInterface {

    /**
     * Increment the counter identified by {@code name} by one.
     *
     * @param name the counter to increment
     */
    void incrementCounter(String name);

    /**
     * Decrement the counter identified by {@code name} by one.
     *
     * @param name the counter to increment
     */
    void decrementCounter(String name);

}