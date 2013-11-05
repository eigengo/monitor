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
     * Increment the counter identified by {@code aspect} by a given number.
     *
     * @param aspect the aspect to increment
     * @param delta the amount to increment the counter by
     * @param tags optional tags
     */
    void incrementCounter(String aspect, int delta, String... tags);

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