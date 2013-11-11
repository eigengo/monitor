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

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import java.net.InetSocketAddress
import akka.util.ByteString

/**
 * Companion object for the ``StatsdActor``, containing the messages that it can handle
 */
object StatsdActor {

  /**
   * Common supertype for all ``StatsdStatistic``s
   */
  sealed trait StatsdStatistic

  /**
   * A counter increments or decrements the specified ``aspect`` by ``delta``, optionally containing
   * non-empty ``tags``.
   *
   * @param aspect the aspect identifying the counter
   * @param delta the delta
   * @param tags the optional tags (DD extension)
   */
  case class Counter(aspect: String, delta: Int, tags: Seq[String] = Nil) extends StatsdStatistic

  /**
   * A gauge records a ``value`` identified by ``aspect``, optionally containing
   * non-empty ``tags``.
   *
   * @param aspect the aspect identifying the gauge
   * @param value the gauge value
   * @param tags the optional tags (DD extension)
   */
  case class Gauge(aspect: String, value: Int, tags: Seq[String] = Nil) extends StatsdStatistic

  /**
   * An execution execution time records time in milliseconds for the given ``aspect``, with
   * optional ``tags``.
   *
   * @param aspect the aspect identifying the execution time
   * @param timeMs the time in milliseconds
   * @param tags the optional tags (DD extension)
   */
  case class ExecutionTime(aspect: String, timeMs: Int, tags: Seq[String] = Nil) extends StatsdStatistic

}

/**
 * Turns the ``StatsdStatistic`` and some ``prefix`` into a ``ByteString``
 */
trait StatisticMarshaller {
  import StatsdActor._

  /**
   * Formats the ``statistic`` into the appropriate ``ByteString``
   *
   * @param statistic the statistic to format
   * @param prefix the prefix, including the trailing ``.``
   * @return the formatted value that can be sent to the statsd server
   */
  def toByteString(statistic: StatsdStatistic, prefix: String): ByteString

}

/**
 * Implements the DataDog extensions
 */
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

  override def toByteString(statistic: StatsdStatistic, prefix: String): ByteString = statistic match {
    case Counter(aspect, delta, tags)        => format(prefix, aspect, delta, "c", constantTags ++ tags)
    case Gauge(aspect, value, tags)          => format(prefix, aspect, value, "g", constantTags ++ tags)
    case ExecutionTime(aspect, timeMs, tags) => format(prefix, aspect, timeMs, "ms", constantTags ++ tags)
  }

}

/**
 * Sends the received ``StatsdStatistic`` messages to the statsd server.
 *
 * @param remote the address of the statsd server
 * @param prefix the constant prefix for all messages. Must be empty or end with ``.``
 */
class StatsdActor(remote: InetSocketAddress, prefix: String) extends Actor {
  this: StatisticMarshaller =>

  require(prefix.isEmpty || prefix.endsWith("."), "Prefix must be empty or end with '.'")

  import context.system
  import StatsdActor._

  IO(Udp) ! Udp.SimpleSender

  def receive: Receive = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender))
  }

  def ready(send: ActorRef): Receive = {
    case stat: StatsdStatistic =>
      val payload = toByteString(stat, prefix)
      send ! Udp.Send(payload, remote)
  }
}
