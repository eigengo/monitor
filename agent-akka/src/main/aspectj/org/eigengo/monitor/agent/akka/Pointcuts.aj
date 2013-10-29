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
