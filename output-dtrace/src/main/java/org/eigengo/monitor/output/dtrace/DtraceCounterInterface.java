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
package org.eigengo.monitor.output.dtrace;

import com.sun.tracing.ProviderFactory;
import org.eigengo.monitor.output.CounterInterface;

public class DtraceCounterInterface implements CounterInterface {

    static DtraceCounterProvider provider;
    static {
        ProviderFactory factory = ProviderFactory.getDefaultFactory();
        System.out.println("***************** " + factory);
        provider = factory.createProvider(DtraceCounterProvider.class);
        provider.goobledygook();
    }

    public void x() {

    }

    @Override
    public void incrementCounter(String aspect, String... tags) {
        provider.foo("++ " + aspect);
    }

    @Override
    public void incrementCounter(String aspect, int delta, String... tags) {
        provider.foo("+= " + aspect);
    }

    @Override
    public void decrementCounter(String aspect, String... tags) {
        provider.foo("-- " + aspect);
    }

    @Override
    public void recordGaugeValue(String aspect, int value, String... tags) {
        provider.foo("== " + aspect);
    }

    @Override
    public void recordExecutionTime(String aspect, int duration, String... tags) {
        provider.foo(" t " + aspect);
    }
}