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

import akka.actor.ActorCell;

abstract aspect Pointcuts {
    static pointcut receiveMessage(ActorCell actorCell, Object msg) : target(actorCell) &&
            call(* akka.actor.ActorCell.receiveMessage(..)) && args(msg);

    static pointcut handleInvokeFailure(ActorCell actorCell, Throwable failure) : target(actorCell) &&
            execution(* akka.actor.ActorCell.handleInvokeFailure(..)) && args(*, failure);

    static pointcut eventStreamPublish(Object event) :
            execution(* akka.event.EventStream.publish(..)) && args(event);
}
