package org.eigengo.monitor.agent.akka

import akka.actor.Actor

class SimpleActor extends Actor {

  def receive: Receive = {
    case s: String =>
      // do nothing
    case i: Int =>
      // for speed testing
      Thread.sleep(i)
  }

}
