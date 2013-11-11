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

  sealed trait StatsdStatistic
  case class Counter(aspect: String, delta: Int, tags: Seq[String]) extends StatsdStatistic
  case class Gauge(aspect: String, value: Int, tags: Seq[String]) extends StatsdStatistic
  case class ExecutionTime(aspect: String, timeMs: Int, tags: Seq[String]) extends StatsdStatistic

}

trait StatisticMarshaller {
  import StatsdActor._

  def toByteString(statistic: StatsdStatistic, prefix: String): ByteString

}

trait DataDogStatisticMarshaller extends StatisticMarshaller {
  import StatsdActor._
  def constantTags: Seq[String]

  private def tagString(tags: Seq[String]): String =
    if (tags.isEmpty) "" else {
      // mutable, but we need speed
      val b = new StringBuilder("|#")
      tags.foreach { tag => if (b.length > 2) b.append(','); b.append(tag) }
      b.toString()
    }

  private def format(prefix: String, aspect: String, value: Int, clazz: String, tags: Seq[String]): ByteString =
    ByteString("%s%s:%d|%s%s".format(prefix, aspect, value, clazz, tagString(tags)))

  def toByteString(statistic: StatsdStatistic, prefix: String): ByteString = statistic match {
    case Counter(aspect, delta, tags)        => format(prefix, aspect, delta, "c", constantTags ++ tags)
    case Gauge(aspect, value, tags)          => format(prefix, aspect, value, "g", constantTags ++ tags)
    case ExecutionTime(aspect, timeMs, tags) => format(prefix, aspect, timeMs, "ms", constantTags ++ tags)
  }

}

class StatsdActor(remote: InetSocketAddress, prefix: String) extends Actor {
  this: StatisticMarshaller =>

  import context.system
  import StatsdActor._

  IO(Udp) ! Udp.SimpleSender

  def receive = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender))
  }

  def ready(send: ActorRef): Receive = {
    case stat: StatsdStatistic =>
      val payload = toByteString(stat, prefix)
      send ! Udp.Send(payload, remote)
  }
}
