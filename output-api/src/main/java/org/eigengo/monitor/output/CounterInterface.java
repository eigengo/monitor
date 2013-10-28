package org.eigengo.monitor.output;

/**
 * Implementations of this interface should communicate with the counter target to submit
 * the values. Ideally, the implementations will be non-blocking; if they are blocking,
 * they should complete as quickly as possible.
 */
public interface CounterInterface {

    /**
     * Increment the counter identified by {@code aspect} by one.
     *
     * @param aspect the aspect to increment
     * @param tags optional tags
     */
    void incrementCounter(String aspect, String... tags);

    /**
     * Decrement the counter identified by {@code aspect} by one.
     *
     * @param aspect the aspect to decrement
     * @param tags optional tags
     */
    void decrementCounter(String aspect, String... tags);

    /**
     * Records gauge {@code value} for the given {@code aspect}, with optional {@code tags}
     *
     * @param aspect the aspect to record the value for
     * @param value the value
     * @param tags optional tags
     */
    void recordGaugeValue(String aspect, int value, String... tags);

    /**
     * Records the execution time of the given {@code aspect}, with optional {@code tags}
     *
     * @param aspect the aspect to record the execution time for
     * @param duration the execution time (most likely in ms)
     * @param tags optional tags
     */
    void recordExecutionTime(String aspect, int duration, String... tags);
}