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

import com.sun.tracing.ProviderName;
import com.sun.tracing.dtrace.FunctionName;
import com.sun.tracing.dtrace.ModuleName;

@ProviderName("akka")
public interface DtraceCounterProvider extends com.sun.tracing.Provider {
    @FunctionName("execution-time") void executionTime(String name, int length, int duration);
    @FunctionName("counter") void counter(String name, int length, int delta);
    @FunctionName("gauge") void gauge(String name, int length, int value);
}