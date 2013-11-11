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
package org.eigengo.monitor.output.statsd

import akka.actor.Actor
import akka.io.{Udp, IO}
import java.net.InetSocketAddress

/**
 * Starts a UDP listener that sends all received messages to ``sink``.
 *
 * @param port the port, e.g. 12345
 * @param sink the function that will be called on every message
 * @tparam U the return type of the ``sink`` function
 */
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
