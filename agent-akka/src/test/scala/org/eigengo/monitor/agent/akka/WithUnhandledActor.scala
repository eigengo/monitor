package org.eigengo.monitor.agent.akka

import akka.actor.Actor

class WithUnhandledActor extends Actor {

  def receive: Receive = {
    case i: Int =>
  }

  override def unhandled(message: Any): Unit = {
    // eat my shorts
  }

}
