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
package org.eigengo.monitor.agent.akka;

import akka.dispatch.MessageDispatcher;

import java.util.concurrent.ForkJoinPool;

privileged aspect DispatcherMonitoringAspect extends AbstractMonitoringAspect issingleton() {

    before(MessageDispatcher dispatcher) : execution(* akka.dispatch.MessageDispatcher.dispatch(..)) && target(dispatcher) {
        System.out.println("*****Dispatching a task");
    }

    before(ForkJoinPool pool) : execution(* scala.concurrent.forkjoin.ForkJoinPool.execute(..)) && target(pool) {
        System.out.println("*****Active threads>> " + pool.getActiveThreadCount());
        System.out.println("*****Queue size>> " + pool.getQueuedTaskCount());
        System.out.println("*****Running thread count>> " + pool.getRunningThreadCount());
    }


}
