package org.eigengo.monitor.agent.akka;

import akka.actor.ActorCell;

abstract aspect Pointcuts {
    static pointcut receiveMessage(ActorCell actorCell, Object msg) : target(actorCell) &&
            call(* akka.actor.ActorCell.receiveMessage(..)) && args(msg);

    // static pointcut handleError(ActorCell actorCell)
}
