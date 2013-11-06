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
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.LocalActorRef;

/**
 * Centralises the pointcuts
 */
abstract privileged aspect Pointcuts {

    /**
     * Pointcut for {@code ActorCell.receiveMessage(msg)}, extracting the {@code ActorCell} and the message being received
     */
    static pointcut actorCellReceiveMessage(ActorCell actorCell, Object msg) : target(actorCell) &&
            call(* akka.actor.ActorCell.receiveMessage(..)) && args(msg);

    /**
     * Pointcut for {@code ActorCell.handleInvokeFailure(_, failure)}, extracting the {@code ActorCell} and the
     * cause of the failure
     */
    static pointcut actorCellHandleInvokeFailure(ActorCell actorCell, Throwable failure) : target(actorCell) &&
            execution(* akka.actor.ActorCell.handleInvokeFailure(..)) && args(*, failure);

    /**
     * Pointcut for the {@code EventStream.publish(event)} method, extracting just the event
     */
    static pointcut eventStreamPublish(Object event) :
            execution(* akka.event.EventStream.publish(..)) && args(event);

    /**
     * Pointcut for the {@code actorOf} methods in {@code ActorCell} and {@code ActorSystem}. You would typically use
     * it in the {@code after returning()} advices.
     */
    static pointcut anyActorOf(Props props) : (execution(* akka.actor.ActorSystem.actorOf(..)) || execution(* akka.actor.ActorCell.actorOf(..))) && args(props);

    /**
     * Pointcut for {@code ActorCell.stop(actor)} method, extracting the {@code ActorRef}
     */
//    static pointcut actorCellStop(ActorRef actor) : execution(* akka.actor.ActorCell.stop(..)) && args(actor);

    static pointcut anotherKindOfStop(LocalActorRef localRef) : target(localRef) && execution(* akka.actor.LocalActorRef.stop());
}
