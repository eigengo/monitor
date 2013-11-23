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

/**
 * Centralises the pointcuts
 */
abstract aspect Pointcuts {

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
     * Pointcut for the {@code actorOf} method in {@code ActorRefFactory} implementations. You would typically use
     * it in the {@code after returning()} advices.
     */
    private static pointcut unnamedActorOf(Props props) :
            execution(ActorRef akka.actor.ActorRefFactory+.actorOf(*)) && args(props);
    /**
     * Pointcut for the {@code actorOf} method in {@code ActorRefFactory} implementations where actor is named on creation
     */
    private static pointcut namedActorOf(Props props) :
            execution(ActorRef akka.actor.ActorRefFactory+.actorOf(*,*)) && args(props, *);
    /**
     * Public pointcut for retrieving Props instance used by {@code actorOf} method in {@code ActorRefFactory}
     */
    static pointcut anyActorOf(Props props) : namedActorOf(props) || unnamedActorOf(props);

    /**
     * Pointcut for {@code ActorCell.stop(actor)} method, extracting the {@code ActorRef}
     */
    static pointcut actorCellStop(ActorRef actor) : execution(* akka.actor.ActorCell.stop(..)) && args(actor);

    /**
     * Pointcut for {@code ActorCell.stop()} method, extracting the targeted {@code ActorCell}
     */
    static pointcut actorCellInternalStop(ActorCell actorCell) : target(actorCell) && execution(* akka.actor.ActorCell.stop());

    /**
     * Pointcut for {@code Creator.create()} method in akka's java api. We use `returning(Actor actor)` to extract the actor
     *
     * We specify the return type as 'Actor', to avoid catching the creators that return 'typed' actors (i.e. this pointcut
     * is strictly for catching those actors that would otherwise have anonymous type elsewhere in the runtime)
     * */
    static pointcut actorCreator() : call(Actor akka.japi.Creator+.create());
 }
