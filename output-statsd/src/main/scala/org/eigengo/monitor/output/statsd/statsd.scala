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

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Udp}
import java.net.InetSocketAddress
import akka.util.ByteString

object StatsdActor {

  trait StatsdStatisticMarshaller[-A] {
    def toByteString(statistic: A, prefix: String): ByteString
  }
  object StandardStatsdStatisticMarshaller extends StatsdStatisticMarshaller[StatsdStatistic] {
    private def format(prefix: String, aspect: String, value: Int, clazz: String): ByteString =
      ByteString("%s.%s:%d|%s".format(prefix, aspect, value, clazz))

    def toByteString(statistic: StatsdStatistic, prefix: String): ByteString = statistic match {
      case Counter(aspect, delta, _)        => format(prefix, aspect, delta, "c")
      case Gauge(aspect, value, _)          => format(prefix, aspect, value, "g")
      case ExecutionTime(aspect, timeMs, _) => format(prefix, aspect, timeMs, "ms")
    }
  }

  sealed trait StatsdStatistic

  case class Counter(aspect: String, delta: Int, tags: Seq[String]) extends StatsdStatistic
  case class Gauge(aspect: String, value: Int, tags: Seq[String]) extends StatsdStatistic
  case class ExecutionTime(aspect: String, timeMs: Int, tags: Seq[String]) extends StatsdStatistic

}

class StatsdActor(remote: InetSocketAddress, prefix: String) extends Actor {
  import context.system
  import StatsdActor._

  IO(Udp) ! Udp.SimpleSender

  def receive = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender))
  }

  def ready(send: ActorRef): Receive = {
    case stat: StatsdStatistic =>
      val payload = StandardStatsdStatisticMarshaller.toByteString(stat, prefix)
      send ! Udp.Send(payload, remote)
  }
}
