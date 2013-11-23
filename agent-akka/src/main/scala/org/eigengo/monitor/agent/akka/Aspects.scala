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
package org.eigengo.monitor.agent.akka

object Aspects {
  def delivered(x: Any): String        = String.format("%s.%s", delivered, x.getClass.getSimpleName)
  val delivered                        = "akka.actor.delivered"
  val undelivered                      = "akka.actor.undelivered"
  def undelivered(x: Any): String      = String.format("%s.%s", undelivered, x.getClass.getSimpleName)
  val queueSize                        = "akka.actor.queue.size"
  val actorDuration                    = "akka.actor.duration"
  val actorError                       = "akka.actor.error"
  def actorError(x: Throwable): String = String.format("%s.%s", actorError, x.getMessage)
  val actorCount                       = "akka.actor.count"

  val activeThreadCount                = "akka.pool.thread.count"
  val runningThreadCount               = "akka.pool.running.thread.count"
  val queuedTaskCount                  = "akka.pool.queued.task.count"
  val poolSize                         = "akka.pool.size"

}
