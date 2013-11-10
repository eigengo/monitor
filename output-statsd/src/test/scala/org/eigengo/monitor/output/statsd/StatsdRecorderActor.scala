package org.eigengo.monitor.output.statsd

import akka.actor.Actor
import akka.io.{Udp, IO}
import java.net.InetSocketAddress

class StatsdRecorderActor[U](port: Int, sink: String => U) extends Actor {
  import context.system

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(port))

  def receive = {
    case Udp.Bound(localAddress) =>
      context.become(ready)
  }

  def ready: Receive = {
    case Udp.Received(data, _) => sink(data.utf8String)
  }
}
