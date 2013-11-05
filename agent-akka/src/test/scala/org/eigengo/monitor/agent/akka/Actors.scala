package org.eigengo.monitor.agent.akka

import akka.actor.Actor

class SimpleActor extends Actor {

  def receive: Receive = {
    case s: String =>
    // do nothing
    case i: Int =>
      // for speed testing
      Thread.sleep(i)
    case 'stop =>
      context.stop(self)
    case false =>
      throw new RuntimeException("Bantha poodoo!")
  }

}

class WithUnhandledActor extends Actor {

  def receive: Receive = {
    case i: Int =>
  }

  override def unhandled(message: Any): Unit = {
    // eat my shorts
  }

}

class NullTestingActor1 extends Actor {

  def receive: Receive = {
    case i: Int =>
  }

  override def unhandled(message: Any): Unit = ()

}

class NullTestingActor2 extends Actor {

  def receive: Receive = {
    case i: Int =>
  }

  override def unhandled(message: Any): Unit = ()

}
